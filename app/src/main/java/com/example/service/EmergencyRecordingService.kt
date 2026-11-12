package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.util.EncryptedFileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Foreground Service that keeps CPU awake and emergency recording active
 * when the app is minimized, user navigates other screens, or phone screen turns off.
 */
class EmergencyRecordingService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var timerJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForegroundServiceWithNotification()
                acquireWakeLock()
                startTimer()
            }
            ACTION_STOP -> {
                stopTimer()
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "CannaMed:EmergencyRecordingWakeLock"
                ).apply {
                    setReferenceCounted(false)
                    acquire(60 * 60 * 1000L) // Max 1 hour
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
            wakeLock = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startForegroundServiceWithNotification() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CannaMed Seguridad • Grabación en 2do Plano")
            .setContentText("Evidencia activa en sandbox cifrado. Protección ante controles o incidentes.")
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            }
            startForeground(NOTIFICATION_ID, notification, type)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (EmergencyRecordingManager.isRecording.value) {
                delay(1000L)
                EmergencyRecordingManager.incrementTimer()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "CannaMed Grabación de Emergencia",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificación persistente de grabación segura en segundo plano"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopTimer()
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "cannamed_emergency_channel"
        const val NOTIFICATION_ID = 9110
        const val ACTION_START = "ACTION_START_EMERGENCY_RECORDING"
        const val ACTION_STOP = "ACTION_STOP_EMERGENCY_RECORDING"

        fun startService(context: Context) {
            val intent = Intent(context, EmergencyRecordingService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, EmergencyRecordingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}

/**
 * Singleton state manager for Emergency Recording:
 * Coordinates recording state, background service, floating status banner, and encryption/export.
 */
object EmergencyRecordingManager {

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _recordingDurationSeconds = MutableStateFlow(0L)
    val recordingDurationSeconds = _recordingDurationSeconds.asStateFlow()

    private val _isMinimized = MutableStateFlow(false)
    val isMinimized = _isMinimized.asStateFlow()

    private val _lastSavedEncryptedFile = MutableStateFlow<File?>(null)
    val lastSavedEncryptedFile = _lastSavedEncryptedFile.asStateFlow()

    private val _lastSavedGalleryUri = MutableStateFlow<Uri?>(null)
    val lastSavedGalleryUri = _lastSavedGalleryUri.asStateFlow()

    var activeRecordingSession: androidx.camera.video.Recording? = null
    var activeRawFile: File? = null

    fun onRecordingStarted(context: Context, rawFile: File) {
        activeRawFile = rawFile
        _isRecording.value = true
        _recordingDurationSeconds.value = 0L
        _isMinimized.value = false
        EmergencyRecordingService.startService(context)
    }

    fun incrementTimer() {
        _recordingDurationSeconds.value += 1L
    }

    fun setMinimized(minimized: Boolean) {
        _isMinimized.value = minimized
    }

    fun stopRecording(
        context: Context,
        onFinalized: ((encryptedMaster: File?, galleryUri: Uri?) -> Unit)? = null
    ) {
        activeRecordingSession?.stop()
        activeRecordingSession = null
        _isRecording.value = false
        _isMinimized.value = false
        EmergencyRecordingService.stopService(context)

        // Process file encryption and gallery backup
        val raw = activeRawFile
        if (raw != null && raw.exists() && raw.length() > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val encFileName = "EVIDENCIA_${timeStamp}.cannaenc"

                    // 1. Encrypt Master copy into secure internal sandbox
                    val encryptedMaster = raw.inputStream().use { input ->
                        EncryptedFileManager.saveEncryptedFileToDir(
                            context = context,
                            inputStream = input,
                            targetDirName = "emergency_recordings",
                            fileName = encFileName
                        )
                    }

                    // 2. Export standard/lower-quality copy to Public Gallery (Movies/CannaMed)
                    val galleryUri = EncryptedFileManager.exportVideoToGallery(
                        context = context,
                        sourceVideoFile = raw,
                        title = "CannaMed_Evidencia_$timeStamp"
                    )

                    // 3. Delete raw unencrypted temp file
                    raw.delete()

                    _lastSavedEncryptedFile.value = encryptedMaster
                    _lastSavedGalleryUri.value = galleryUri

                    CoroutineScope(Dispatchers.Main).launch {
                        onFinalized?.invoke(encryptedMaster, galleryUri)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    CoroutineScope(Dispatchers.Main).launch {
                        onFinalized?.invoke(null, null)
                    }
                }
            }
        } else {
            onFinalized?.invoke(null, null)
        }
    }
}
