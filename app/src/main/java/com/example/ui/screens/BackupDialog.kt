package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldSecondaryContainer
import com.example.util.CryptoSecurityUtil
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupDialog(
    onDismiss: () -> Unit,
    onExportBackup: suspend () -> String,
    onRestoreBackup: suspend (String) -> Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }
    var isReminderEnabled by remember { mutableStateOf(true) }
    var lastBackupStatusMessage by remember { mutableStateOf<String?>(null) }

    // Advanced security encryption state
    var exportModeTab by remember { mutableIntStateOf(0) } // 0: Cifrado Militar AES-256 (Recomendado), 1: JSON Estándar
    var exportPassword by remember { mutableStateOf("") }
    var showExportPassword by remember { mutableStateOf(false) }

    var restorePassword by remember { mutableStateOf("") }
    var showRestorePassword by remember { mutableStateOf(false) }
    var pendingEncryptedPayload by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isLoading = true
                try {
                    val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader().use { it.readText() }
                    }
                    if (!content.isNullOrBlank()) {
                        if (CryptoSecurityUtil.isEncryptedBackup(content)) {
                            // Needs password decryption
                            pendingEncryptedPayload = content
                            lastBackupStatusMessage = "Archivo cifrado detectado (AES-256-GCM). Introduce la contraseña abajo para descifrarlo."
                        } else {
                            // Standard plain JSON backup
                            val success = onRestoreBackup(content)
                            if (success) {
                                Toast.makeText(context, "Respaldo restaurado con éxito", Toast.LENGTH_LONG).show()
                                lastBackupStatusMessage = "¡Base de datos y documentos médicos restaurados con éxito!"
                            } else {
                                Toast.makeText(context, "El archivo de respaldo no es válido", Toast.LENGTH_LONG).show()
                                lastBackupStatusMessage = "Error: formato de respaldo inválido."
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al leer archivo: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "Respaldo de Datos Médicos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Normativa HIPAA & GDPR / Cifrado AES-256",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                // Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Security Privacy Banner
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(22.dp))
                                Text(
                                    text = "Privacidad Zero-Knowledge y Soberanía de Datos",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = EmeraldPrimary
                                )
                            }
                            Text(
                                text = "Tus recetas, tomas, dolencias y documentos médicos residen 100% de forma local en tu dispositivo. Para exportaciones a la nube (Google Drive) o correo, puedes aplicar cifrado de grado médico AES-256-GCM con contraseña personal.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Section 1: Export
                    Text("1. Generar Respaldo de Seguridad:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    TabRow(
                        selectedTabIndex = exportModeTab,
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = EmeraldPrimary
                    ) {
                        Tab(
                            selected = exportModeTab == 0,
                            onClick = { exportModeTab = 0 },
                            text = { Text("Cifrado AES-256 (Recomendado)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = exportModeTab == 1,
                            onClick = { exportModeTab = 1 },
                            text = { Text("JSON Estándar", fontSize = 12.sp) }
                        )
                    }

                    if (exportModeTab == 0) {
                        OutlinedTextField(
                            value = exportPassword,
                            onValueChange = { exportPassword = it },
                            label = { Text("Contraseña de cifrado para el respaldo") },
                            placeholder = { Text("Mínimo 6 caracteres para proteger tus datos") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showExportPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary) },
                            trailingIcon = {
                                IconButton(onClick = { showExportPassword = !showExportPassword }) {
                                    Icon(
                                        if (showExportPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Mostrar contraseña"
                                    )
                                }
                            },
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = {
                            if (exportModeTab == 0 && exportPassword.length < 6) {
                                Toast.makeText(context, "Ingresa una contraseña de al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            coroutineScope.launch {
                                isLoading = true
                                try {
                                    val rawJson = onExportBackup()
                                    val finalPayload = if (exportModeTab == 0) {
                                        CryptoSecurityUtil.encryptPayload(rawJson, exportPassword.toCharArray())
                                    } else {
                                        rawJson
                                    }

                                    val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                    val filePrefix = if (exportModeTab == 0) "CannaMed_EncryptedBackup_" else "CannaMed_Backup_"
                                    val fileName = "$filePrefix$dateStr.json"

                                    val backupDir = File(context.cacheDir, "backups")
                                    if (!backupDir.exists()) backupDir.mkdirs()
                                    val backupFile = File(backupDir, fileName)
                                    FileOutputStream(backupFile).use { it.write(finalPayload.toByteArray(Charsets.UTF_8)) }

                                    val fileUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        backupFile
                                    )

                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_STREAM, fileUri)
                                        putExtra(Intent.EXTRA_SUBJECT, "Copia de Seguridad CannaMed ($fileName)")
                                        putExtra(Intent.EXTRA_TEXT, "Archivo de respaldo médico seguro CannaMed.")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Guardar en Google Drive, Archivos o Enviar"))
                                    lastBackupStatusMessage = if (exportModeTab == 0) {
                                        "Respaldo cifrado con éxito con AES-256. Recuerda tu contraseña para restaurarlo."
                                    } else {
                                        "Respaldo generado exitosamente."
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("export_backup_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(if (exportModeTab == 0) Icons.Default.Lock else Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (exportModeTab == 0) "Exportar Respaldo Cifrado AES-256" else "Exportar JSON Estándar",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Backup Reminder Toggle
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = EmeraldSecondary)
                                Column {
                                    Text("Recordatorio Periódico", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("Aviso cada 15 días para mantener tu seguimiento médico al día.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = isReminderEnabled,
                                onCheckedChange = { isReminderEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = EmeraldPrimary,
                                    checkedTrackColor = EmeraldPrimary.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }

                    // Section 2: Restore
                    Text("2. Restaurar Copia de Seguridad:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch(arrayOf("application/json", "text/*"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("import_backup_file_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Seleccionar Archivo JSON de Respaldo")
                    }

                    // If encrypted payload detected, prompt for passphrase
                    pendingEncryptedPayload?.let { encryptedContent ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.border(1.dp, EmeraldPrimary, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = EmeraldPrimary)
                                    Text("Desbloquear Respaldo Cifrado AES-256", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                }
                                OutlinedTextField(
                                    value = restorePassword,
                                    onValueChange = { restorePassword = it },
                                    label = { Text("Contraseña utilizada al exportar") },
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = if (showRestorePassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    trailingIcon = {
                                        IconButton(onClick = { showRestorePassword = !showRestorePassword }) {
                                            Icon(if (showRestorePassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                                        }
                                    },
                                    singleLine = true
                                )
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isLoading = true
                                            try {
                                                val decryptedPlainJson = CryptoSecurityUtil.decryptPayload(
                                                    encryptedContent,
                                                    restorePassword.toCharArray()
                                                )
                                                val success = onRestoreBackup(decryptedPlainJson)
                                                if (success) {
                                                    Toast.makeText(context, "Respaldo descifrado y restaurado con éxito", Toast.LENGTH_LONG).show()
                                                    lastBackupStatusMessage = "¡Datos y recetas restaurados con éxito tras descifrar!"
                                                    pendingEncryptedPayload = null
                                                    restorePassword = ""
                                                } else {
                                                    Toast.makeText(context, "El archivo interno no tiene formato CannaMed válido", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Contraseña incorrecta o archivo dañado", Toast.LENGTH_LONG).show()
                                                lastBackupStatusMessage = "Error al descifrar: contraseña incorrecta o firma de integridad fallida."
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                ) {
                                    Text("Descifrar y Restaurar")
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        label = { Text("O pega aquí el texto JSON del respaldo") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4
                    )

                    if (restoreJsonText.isNotBlank()) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    try {
                                        if (CryptoSecurityUtil.isEncryptedBackup(restoreJsonText)) {
                                            pendingEncryptedPayload = restoreJsonText
                                            lastBackupStatusMessage = "Texto cifrado detectado. Introduce la contraseña arriba para descifrarlo."
                                        } else {
                                            val success = onRestoreBackup(restoreJsonText)
                                            if (success) {
                                                Toast.makeText(context, "Respaldo restaurado con éxito", Toast.LENGTH_LONG).show()
                                                lastBackupStatusMessage = "Respaldo restaurado con éxito."
                                                restoreJsonText = ""
                                            } else {
                                                Toast.makeText(context, "JSON inválido", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondaryContainer),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Restaurar desde Texto Pegado")
                        }
                    }

                    lastBackupStatusMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.15f))
                                .padding(12.dp)
                        ) {
                            Text(msg, style = MaterialTheme.typography.bodySmall, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
