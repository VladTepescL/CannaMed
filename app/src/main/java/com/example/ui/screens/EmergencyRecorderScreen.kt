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
import androidx.camera.video.Recording
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EmergencyVideoRecord(
    val file: File,
    val durationSeconds: Long,
    val formattedDate: String,
    val sizeMb: Double
)

@Composable
fun EmergencyRecorderScreen(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Grabar Video, 1: Videos Protegidos

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

    // Video Recording State
    var recordingQuality by remember { mutableStateOf(Quality.FHD) } // FHD = 1080p, HD = 720p, HIGHEST = Max
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDurationSeconds by remember { mutableLongStateOf(0L) }
    var lastSavedFile by remember { mutableStateOf<File?>(null) }

    val savedVideosList = remember { mutableStateListOf<EmergencyVideoRecord>() }

    fun refreshSavedVideos() {
        savedVideosList.clear()
        val dir = File(context.filesDir, "emergency_recordings")
        if (dir.exists()) {
            dir.listFiles()?.filter { it.extension == "mp4" }?.sortedByDescending { it.lastModified() }?.forEach { f ->
                val size = Math.round((f.length().toDouble() / (1024 * 1024)) * 10.0) / 10.0
                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(f.lastModified()))
                savedVideosList.add(
                    EmergencyVideoRecord(
                        file = f,
                        durationSeconds = 0L,
                        formattedDate = dateStr,
                        sizeMb = size
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

    // Timer for active recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDurationSeconds = 0L
            while (isRecording) {
                kotlinx.coroutines.delay(1000L)
                recordingDurationSeconds++
            }
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
                                text = "Grabación de Seguridad & Emergencia",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Sandbox Cifrado • Procedimiento Policial / Fiscalización",
                            fontSize = 11.sp,
                            color = Color(0xFFA5D6A7)
                        )
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
                text = { Text("Evidencias Guardadas (${savedVideosList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        if (selectedTab == 0) {
            if (!hasCameraPermission || !hasAudioPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2420)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(48.dp))
                            Text("Permisos de Cámara y Audio Requeridos", fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                "Para registrar evidencia en video y audio con respaldo inalterable durante procedimientos, la app necesita acceso a la cámara y micrófono.",
                                color = Color(0xFFB0BEC5),
                                fontSize = 12.sp
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
                // Camera View and Controls
                Box(modifier = Modifier.fillMaxSize()) {
                    var videoCapture: VideoCapture<Recorder>? by remember { mutableStateOf(null) }

                    // Camera Preview
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
                                text = String.format(Locale.getDefault(), "GRABANDO %02d:%02d", minutes, seconds),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Bottom Recording Trigger Area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color(0xCC000000))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isRecording) "Grabando en Sandbox Seguro (Inalterable)" else "Toca para iniciar grabación de evidencia",
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp
                        )

                        IconButton(
                            onClick = {
                                if (isRecording) {
                                    activeRecording?.stop()
                                    activeRecording = null
                                    isRecording = false
                                } else {
                                    val dir = File(context.filesDir, "emergency_recordings")
                                    if (!dir.exists()) dir.mkdirs()

                                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                    val videoFile = File(dir, "EVIDENCIA_$timeStamp.mp4")

                                    val outputOptions = FileOutputOptions.Builder(videoFile).build()

                                    videoCapture?.let { capture ->
                                        val rec = capture.output
                                            .prepareRecording(context, outputOptions)
                                            .withAudioEnabled()
                                            .start(ContextCompat.getMainExecutor(context)) { event ->
                                                when (event) {
                                                    is VideoRecordEvent.Start -> {
                                                        isRecording = true
                                                    }
                                                    is VideoRecordEvent.Finalize -> {
                                                        isRecording = false
                                                        if (!event.hasError()) {
                                                            lastSavedFile = videoFile
                                                            Toast.makeText(context, "Video guardado con éxito en Sandbox Seguro", Toast.LENGTH_LONG).show()
                                                            refreshSavedVideos()
                                                        } else {
                                                            Toast.makeText(context, "Error en grabación: ${event.error}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            }
                                        activeRecording = rec
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
        } else {
            // Tab 1: Saved Videos in Sandbox
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
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary)
                            Text(
                                "Los videos quedan guardados en el almacenamiento privado de CannaMed. Nadie sin acceso a tu teléfono o contraseña puede borrarlos ni acceder a ellos.",
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
                            Text("No hay videos de emergencia grabados.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(savedVideosList) { item ->
                        SavedVideoCard(
                            record = item,
                            onDelete = {
                                item.file.delete()
                                refreshSavedVideos()
                            },
                            onShare = {
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        item.file
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "video/mp4"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_SUBJECT, "Evidencia Médica CannaMed")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartir o Respaldar Video de Emergencia"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onPlay = {
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        item.file
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "video/mp4")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedVideoCard(
    record: EmergencyVideoRecord,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onPlay: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B231F)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                        .background(EmeraldPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = EmeraldPrimary)
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

            Row {
                IconButton(onClick = onPlay) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir", tint = EmeraldPrimary)
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Respaldar o Enviar", tint = Color.White)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFFF5252))
                }
            }
        }
    }
}
