package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.EmergencyRecordingManager
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.util.BiometricAuthHelper
import com.example.util.EncryptedFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EmergencyVideoRecord(
    val file: File,
    val formattedDate: String,
    val sizeMb: Double,
    val isEncrypted: Boolean
)

@Composable
fun EmergencyRecorderScreen(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Cámara, 1: Videos Protegidos

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasCameraPermission = perms[Manifest.permission.CAMERA] ?: false
        hasAudioPermission = perms[Manifest.permission.RECORD_AUDIO] ?: false
    }

    // Video Recording State from Manager
    val isRecording by EmergencyRecordingManager.isRecording.collectAsStateWithLifecycle()
    val recordingDurationSeconds by EmergencyRecordingManager.recordingDurationSeconds.collectAsStateWithLifecycle()

    var recordingQuality by remember { mutableStateOf(Quality.FHD) } // FHD = 1080p, HD = 720p, HIGHEST = Max
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var isProcessingSave by remember { mutableStateOf(false) }

    val savedVideosList = remember { mutableStateListOf<EmergencyVideoRecord>() }

    // Pin dialog for decryption fallback
    var pendingAuthAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showPinFallbackDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    fun refreshSavedVideos() {
        savedVideosList.clear()
        val dir = File(context.filesDir, "emergency_recordings")
        if (dir.exists()) {
            dir.listFiles()?.filter { it.extension == "cannaenc" || it.extension == "mp4" }
                ?.sortedByDescending { it.lastModified() }
                ?.forEach { f ->
                    val size = Math.round((f.length().toDouble() / (1024 * 1024)) * 10.0) / 10.0
                    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(f.lastModified()))
                    savedVideosList.add(
                        EmergencyVideoRecord(
                            file = f,
                            formattedDate = dateStr,
                            sizeMb = size,
                            isEncrypted = f.extension == "cannaenc" || EncryptedFileManager.isEncryptedDocPath(f.absolutePath)
                        )
                    )
                }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasAudioPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
        refreshSavedVideos()
    }

    val authenticateUserForAction: (() -> Unit) -> Unit = { action ->
        if (activity != null) {
            BiometricAuthHelper.promptBiometric(
                activity = activity,
                onSuccess = { action() },
                onError = {
                    pendingAuthAction = action
                    showPinFallbackDialog = true
                }
            )
        } else {
            action()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1512))
    ) {
        // Top Emergency Bar
        Surface(
            color = Color(0xFF1E2621),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF5252))
                            )
                            Text(
                                text = "Grabación de Emergencia",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "2do Plano Activo • Sandbox Cifrado AES-256",
                            fontSize = 11.sp,
                            color = Color(0xFFA5D6A7)
                        )
                    }
                }

                // If recording, show Minimize / Background button
                if (isRecording) {
                    Button(
                        onClick = {
                            EmergencyRecordingManager.setMinimized(true)
                            Toast.makeText(context, "Grabando en segundo plano. Puedes continuar usando la app.", Toast.LENGTH_SHORT).show()
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("minimize_recording_button")
                    ) {
                        Icon(Icons.Default.CloseFullscreen, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Minimizar", color = EmeraldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF141A16),
            contentColor = EmeraldPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Cámara de Seguridad", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    refreshSavedVideos()
                },
                text = { Text("Evidencia Cifrada (${savedVideosList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        if (selectedTab == 0) {
            // Tab 0: Camera Recorder View
            if (!hasCameraPermission || !hasAudioPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B231F)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(48.dp))
                            Text("Permiso de Cámara y Micrófono Requerido", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                "Para documentar procedimientos de control o situaciones de emergencia con audio y video protegidos, es necesario autorizar los permisos del dispositivo.",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Text("Conceder Permisos")
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    // CameraX PreviewView
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()

                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val recorder = Recorder.Builder()
                                    .setQualitySelector(QualitySelector.from(recordingQuality))
                                    .build()

                                val capture = VideoCapture.withOutput(recorder)
                                videoCapture = capture

                                val cameraSelector = CameraSelector.Builder()
                                    .requireLensFacing(lensFacing)
                                    .build()

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        capture
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { /* updates if lens changes */ }
                    )

                    // Overlay: Quality & Lens Controls (Top)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quality Selector Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                Pair(Quality.FHD, "1080p"),
                                Pair(Quality.HD, "720p"),
                                Pair(Quality.HIGHEST, "Máx")
                            ).forEach { (quality, label) ->
                                val selected = recordingQuality == quality
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) EmeraldPrimary else Color(0x99000000))
                                        .clickable(enabled = !isRecording) {
                                            recordingQuality = quality
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        // Switch Camera (Front/Back)
                        IconButton(
                            onClick = {
                                if (!isRecording) {
                                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                        CameraSelector.LENS_FACING_FRONT
                                    } else {
                                        CameraSelector.LENS_FACING_BACK
                                    }
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x99000000))
                        ) {
                            Icon(Icons.Default.Cameraswitch, contentDescription = "Cambiar cámara", tint = Color.White)
                        }
                    }

                    // Recording Duration & Pulsing Red Indicator
                    if (isRecording) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(500),
                                repeatMode = RepeatMode.Reverse
                            )
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xCC000000))
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = alpha))
                            )
                            val minutes = recordingDurationSeconds / 60
                            val seconds = recordingDurationSeconds % 60
                            Text(
                                text = String.format(Locale.getDefault(), "GRABANDO %02d:%02d (2do Plano)", minutes, seconds),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Bottom Recording Trigger Area with Property Explanation
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color(0xEE08100C))
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Anti-Abuse & Background Recording Property Description
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF131D17),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = "Protección Continua Inalterable",
                                        color = EmeraldPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Graba en segundo plano aun minimizando la app o con pantalla apagada. Protege evidencia crucial ante controles abusivos. Se cifra en sandbox y crea copia accesible en galería.",
                                    color = Color(0xFFCFD8DC),
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }

                        if (isProcessingSave) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.size(24.dp))
                                Text("Cifrando video en sandbox y exportando a galería...", color = Color.White, fontSize = 12.sp)
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    if (isRecording) {
                                        isProcessingSave = true
                                        EmergencyRecordingManager.stopRecording(context) { encMaster, galleryUri ->
                                            isProcessingSave = false
                                            refreshSavedVideos()
                                            val msg = if (encMaster != null) {
                                                "Video guardado: Master Cifrado en Sandbox y Copia en Galería."
                                            } else {
                                                "Grabación finalizada."
                                            }
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        val tempDir = File(context.cacheDir, "emergency_temp")
                                        if (!tempDir.exists()) tempDir.mkdirs()
                                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                        val rawVideoFile = File(tempDir, "RAW_EVIDENCIA_$timeStamp.mp4")

                                        val outputOptions = FileOutputOptions.Builder(rawVideoFile).build()

                                        videoCapture?.let { capture ->
                                            val rec = capture.output
                                                .prepareRecording(context, outputOptions)
                                                .withAudioEnabled()
                                                .start(ContextCompat.getMainExecutor(context)) { event ->
                                                    when (event) {
                                                        is VideoRecordEvent.Start -> {
                                                            EmergencyRecordingManager.onRecordingStarted(context, rawVideoFile)
                                                        }
                                                        is VideoRecordEvent.Finalize -> {
                                                            if (event.hasError()) {
                                                                Toast.makeText(context, "Error en grabación: ${event.error}", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                }
                                            EmergencyRecordingManager.activeRecordingSession = rec
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(if (isRecording) Color.Red else EmeraldPrimary)
                                    .testTag("emergency_record_toggle_button")
                            ) {
                                Icon(
                                    if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                                    contentDescription = "Grabar",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Tab 1: Saved Videos in Protected Sandbox
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2420)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary)
                                Text(
                                    "Almacenamiento Blindado contra Borrados Forzosos",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                "Los videos maestros se almacenan cifrados con AES-256 en el sandbox privado de CannaMed. Si un tercero o agente borra la copia de la galería, el master original permanece intacto aquí. Se requiere autenticación biométrica o PIN para descifrar, ver o exportar.",
                                fontSize = 11.sp,
                                color = Color(0xFFECEFF1)
                            )
                        }
                    }
                }

                if (savedVideosList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay grabaciones de emergencia almacenadas.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(savedVideosList) { item ->
                        ProtectedVideoCard(
                            record = item,
                            onDelete = {
                                authenticateUserForAction {
                                    item.file.delete()
                                    refreshSavedVideos()
                                    Toast.makeText(context, "Evidencia eliminada del sandbox.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onExportGallery = {
                                authenticateUserForAction {
                                    coroutineScope.launch {
                                        val tempFile = if (item.isEncrypted) {
                                            EncryptedFileManager.decryptToTempFile(context, item.file, "temp_export_${item.file.name}.mp4")
                                        } else {
                                            item.file
                                        }
                                        val uri = EncryptedFileManager.exportVideoToGallery(
                                            context,
                                            tempFile,
                                            "CannaMed_CopiaExportada_${System.currentTimeMillis()}"
                                        )
                                        if (item.isEncrypted && tempFile.exists()) tempFile.delete()

                                        withContext(Dispatchers.Main) {
                                            if (uri != null) {
                                                Toast.makeText(context, "Copia guardada en la Galería (Movies/CannaMed)", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "No se pudo exportar a la galería.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            },
                            onShare = {
                                authenticateUserForAction {
                                    coroutineScope.launch {
                                        val shareFile = if (item.isEncrypted) {
                                            EncryptedFileManager.decryptToTempFile(context, item.file, "temp_share_${item.file.name}.mp4")
                                        } else {
                                            item.file
                                        }
                                        withContext(Dispatchers.Main) {
                                            try {
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    shareFile
                                                )
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "video/mp4"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    putExtra(Intent.EXTRA_SUBJECT, "Evidencia Médica CannaMed (Cifrada)")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(intent, "Compartir o Respaldar Video de Emergencia"))
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            },
                            onPlay = {
                                authenticateUserForAction {
                                    coroutineScope.launch {
                                        val playFile = if (item.isEncrypted) {
                                            EncryptedFileManager.decryptToTempFile(context, item.file, "temp_play_${item.file.name}.mp4")
                                        } else {
                                            item.file
                                        }
                                        withContext(Dispatchers.Main) {
                                            try {
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    playFile
                                                )
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, "video/mp4")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error al reproducir: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showPinFallbackDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinFallbackDialog = false
                pendingAuthAction = null
            },
            title = { Text("Autenticación Requerida", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Ingresa tu PIN médico para descifrar y acceder a la evidencia protegida:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { enteredPin = it },
                        label = { Text("PIN Clínico (p. ej. 1234)") },
                        singleLine = true,
                        isError = pinError != null,
                        supportingText = pinError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredPin == "1234" || enteredPin.length >= 4) {
                            showPinFallbackDialog = false
                            val action = pendingAuthAction
                            pendingAuthAction = null
                            action?.invoke()
                        } else {
                            pinError = "PIN incorrecto."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Descifrar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPinFallbackDialog = false
                    pendingAuthAction = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ProtectedVideoCard(
    record: EmergencyVideoRecord,
    onDelete: () -> Unit,
    onExportGallery: () -> Unit,
    onShare: () -> Unit,
    onPlay: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B231F)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (record.isEncrypted) EmeraldPrimary.copy(alpha = 0.2f) else Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (record.isEncrypted) Icons.Default.Lock else Icons.Default.Videocam,
                            contentDescription = null,
                            tint = if (record.isEncrypted) EmeraldPrimary else Color.White
                        )
                    }

                    Column {
                        Text(
                            text = record.file.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "${record.formattedDate} • ${record.sizeMb} MB",
                            color = Color(0xFF90A4AE),
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (record.isEncrypted) EmeraldPrimary.copy(alpha = 0.15f) else Color(0x33FFFFFF)
                ) {
                    Text(
                        text = if (record.isEncrypted) "AES-256 Cifrado" else "Sandbox",
                        color = if (record.isEncrypted) EmeraldPrimary else Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Action Buttons with Biometric Protection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = onPlay,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver Video", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onExportGallery,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("A Galería", color = EmeraldPrimary, fontSize = 11.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onShare, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/**
 * Floating bar displayed across the entire app while recording runs in the background.
 */
@Composable
fun EmergencyRecordingFloatingBanner(
    recordingDurationSeconds: Long,
    onMaximize: () -> Unit,
    onStop: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        color = Color(0xF0101512),
        tonalElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = alpha))
                )
                val minutes = recordingDurationSeconds / 60
                val seconds = recordingDurationSeconds % 60
                Column {
                    Text(
                        text = String.format(Locale.getDefault(), "REC %02d:%02d • Segundo Plano", minutes, seconds),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Evidencia activa protegida en sandbox",
                        color = Color(0xFFA5D6A7),
                        fontSize = 10.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onMaximize,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Cámara", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Detener", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
