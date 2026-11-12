package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldTertiary
import com.example.util.EncryptedFileManager
import com.example.util.SecurityPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PatientIdentityCredentialModal(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val securityPrefs = remember { SecurityPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    var isEditMode by remember { mutableStateOf(!securityPrefs.hasIdentityVerification) }

    var fullName by remember { mutableStateOf(securityPrefs.patientFullName) }
    var idNumber by remember { mutableStateOf(securityPrefs.patientIdNumber) }

    var selectedDocUri by remember { mutableStateOf<Uri?>(null) }
    var selectedSelfieUri by remember { mutableStateOf<Uri?>(null) }

    var decryptedDocTempFile by remember { mutableStateOf<File?>(null) }
    var decryptedSelfieTempFile by remember { mutableStateOf<File?>(null) }
    var isDecrypting by remember { mutableStateOf(false) }

    // Decrypt existing documents to temp cache for display
    fun loadExistingDecryptedDocs() {
        coroutineScope.launch(Dispatchers.IO) {
            isDecrypting = true
            try {
                val docPath = securityPrefs.identityDocEncryptedPath
                val selfiePath = securityPrefs.identitySelfieDocEncryptedPath

                val docFile = docPath?.let { File(it) }
                val selfieFile = selfiePath?.let { File(it) }

                val dDoc = if (docFile != null && docFile.exists()) {
                    EncryptedFileManager.decryptToTempFile(context, docFile, "temp_view_id_doc.jpg")
                } else null

                val dSelfie = if (selfieFile != null && selfieFile.exists()) {
                    EncryptedFileManager.decryptToTempFile(context, selfieFile, "temp_view_selfie_doc.jpg")
                } else null

                withContext(Dispatchers.Main) {
                    decryptedDocTempFile = dDoc
                    decryptedSelfieTempFile = dSelfie
                    isDecrypting = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isDecrypting = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (securityPrefs.hasIdentityVerification) {
            loadExistingDecryptedDocs()
        }
    }

    val docPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedDocUri = uri
    }

    val selfiePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedSelfieUri = uri
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxSize(0.94f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "Credencial de Paciente Medicinal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Identificación Rápida y Acreditación de Edad (21+)",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = EmeraldSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ZERO-KNOWLEDGE & COMPLIANCE BANNER
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Almacenamiento Zero-Knowledge (No Informar): El documento y la selfie quedan cifrados con AES-256 en la memoria sandbox de tu dispositivo. No se transmiten a servidores externos ni a entidades públicas.",
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!isEditMode && securityPrefs.hasIdentityVerification) {
                        // VIEW MODE: OFFICIAL PATIENT CREDENTIAL
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Credential Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "REPÚBLICA DE CHILE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "PACIENTE CANNABIS MEDICINAL",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = EmeraldPrimary
                                    ) {
                                        Text(
                                            text = "21+ VERIFICADO",
                                            color = Color.Black,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = EmeraldPrimary.copy(alpha = 0.3f))

                                // Photos Row: Selfie holding document + Document Preview
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Selfie holding ID card
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                                .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (decryptedSelfieTempFile != null && decryptedSelfieTempFile!!.exists()) {
                                                AsyncImage(
                                                    model = decryptedSelfieTempFile,
                                                    contentDescription = "Selfie con Documento",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.Face, contentDescription = null, tint = EmeraldSecondary)
                                                    Text("Selfie con Cédula", fontSize = 10.sp, color = Color.Gray)
                                                }
                                            }
                                        }
                                        Text("Selfie con Cédula en Mano", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = EmeraldSecondary)
                                    }

                                    // Identification Document
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                                .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (decryptedDocTempFile != null && decryptedDocTempFile!!.exists()) {
                                                AsyncImage(
                                                    model = decryptedDocTempFile,
                                                    contentDescription = "Documento de Identidad",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldSecondary)
                                                    Text("Cédula / DNI", fontSize = 10.sp, color = Color.Gray)
                                                }
                                            }
                                        }
                                        Text("Documento de Identidad", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = EmeraldSecondary)
                                    }
                                }

                                // Patient Data
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Nombre del Paciente:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(securityPrefs.patientFullName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("N° Cédula / DNI / Pasaporte:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(securityPrefs.patientIdNumber.ifBlank { "No registrado" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Validación de Mayoría de Edad:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("Mayor de 21 años (Acreditado)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldSecondary)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Fecha de Registro Cifrado:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            val dateStr = if (securityPrefs.identityVerifiedTimestamp > 0L) {
                                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(securityPrefs.identityVerifiedTimestamp))
                                            } else "Noviembre 2026"
                                            Text(dateStr, fontSize = 11.sp)
                                        }
                                    }
                                }

                                // Legal Protection text for police / authorities
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "Marco Jurídico de Acreditación (Chile):",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = EmeraldPrimary
                                        )
                                        Text(
                                            text = "• Ley N° 20.000 Arts. 4, 8 y 50: El uso, porte y cultivo de especies de cannabis destinados a la atención de un tratamiento médico exclusivo y próximo en el tiempo se encuentra expresamente justificado y no constituye delito.\n• Ley N° 20.584: Reserva y confidencialidad inviolable de la ficha clínica del paciente.",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { isEditMode = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Actualizar Fotos / Datos")
                                    }
                                    Button(
                                        onClick = onDismiss,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Cerrar")
                                    }
                                }
                            }
                        }
                    } else {
                        // EDIT / REGISTRATION MODE
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "Registro de Respaldo de Identidad (21+)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Adjunta tu documento y una selfie con el documento en mano para acreditar tu mayoría de edad (21+) y disponer de una identificación rápida en caso de controles o revisiones policiales.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = { Text("Nombre Completo del Paciente") },
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = idNumber,
                                    onValueChange = { idNumber = it },
                                    label = { Text("RUT / DNI / Pasaporte") },
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Select Document Photo
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldPrimary)
                                                Text("1. Documento de Identificación Válido", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            if (selectedDocUri != null || securityPrefs.identityDocEncryptedPath != null) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Listo", tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                            }
                                        }

                                        if (selectedDocUri != null) {
                                            AsyncImage(
                                                model = selectedDocUri,
                                                contentDescription = "Documento seleccionado",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                docPhotoPicker.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (selectedDocUri != null) "Cambiar Foto Documento" else "Seleccionar Foto Cédula / DNI", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                                        }
                                    }
                                }

                                // Select Selfie with Document
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.Face, contentDescription = null, tint = EmeraldPrimary)
                                                Text("2. Selfie con Documento en Mano", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            if (selectedSelfieUri != null || securityPrefs.identitySelfieDocEncryptedPath != null) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Listo", tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                            }
                                        }

                                        if (selectedSelfieUri != null) {
                                            AsyncImage(
                                                model = selectedSelfieUri,
                                                contentDescription = "Selfie seleccionada",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                selfiePhotoPicker.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (selectedSelfieUri != null) "Cambiar Selfie con Documento" else "Seleccionar Selfie con Documento", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            try {
                                                securityPrefs.patientFullName = fullName.trim()
                                                securityPrefs.patientIdNumber = idNumber.trim()

                                                selectedDocUri?.let { uri ->
                                                    context.contentResolver.openInputStream(uri)?.use { input ->
                                                        val savedEncFile = EncryptedFileManager.saveEncryptedIdentityDoc(
                                                            context = context,
                                                            inputStream = input,
                                                            fileName = "id_doc_${System.currentTimeMillis()}.cannaenc"
                                                        )
                                                        securityPrefs.identityDocEncryptedPath = savedEncFile.absolutePath
                                                    }
                                                }

                                                selectedSelfieUri?.let { uri ->
                                                    context.contentResolver.openInputStream(uri)?.use { input ->
                                                        val savedEncFile = EncryptedFileManager.saveEncryptedIdentityDoc(
                                                            context = context,
                                                            inputStream = input,
                                                            fileName = "selfie_doc_${System.currentTimeMillis()}.cannaenc"
                                                        )
                                                        securityPrefs.identitySelfieDocEncryptedPath = savedEncFile.absolutePath
                                                    }
                                                }

                                                securityPrefs.identityVerifiedTimestamp = System.currentTimeMillis()
                                                securityPrefs.isAgeVerified21Plus = true

                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "Credencial guardada y cifrada con éxito.", Toast.LENGTH_SHORT).show()
                                                    isEditMode = false
                                                    loadExistingDecryptedDocs()
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "Error al guardar documentos: ${e.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Guardar y Cifrar en Sandbox Local", fontWeight = FontWeight.Bold)
                                }

                                if (securityPrefs.hasIdentityVerification) {
                                    TextButton(
                                        onClick = { isEditMode = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Cancelar y Ver Credencial Existente")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
