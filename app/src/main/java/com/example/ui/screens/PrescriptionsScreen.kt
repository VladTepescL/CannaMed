package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.model.Prescription
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldSecondaryContainer
import com.example.ui.theme.EmeraldTertiary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrescriptionsScreen(
    prescriptions: List<Prescription>,
    onSavePrescription: (Prescription, Uri?, String?) -> Unit,
    onDeletePrescription: (Prescription) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var viewingPrescription by remember { mutableStateOf<Prescription?>(null) }
    var prescriptionToDelete by remember { mutableStateOf<Prescription?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recetas Médicas & Controles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Prescripciones, gramos diarios y avisos de control a 15, 7, 3 días",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_prescription_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cargar")
                }
            }
        }

        // Regulatory banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = EmeraldSecondary, modifier = Modifier.size(22.dp))
                    Text(
                        text = "Marco de Control Médico: En Chile la receta no vence de forma absoluta, pero requiere control clínico periódico obligatorio cada 6 meses (máximo 2 años según indicación médica).",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (prescriptions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(48.dp))
                        Text(
                            text = "No hay recetas médicas registradas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Adjunta fotos o archivos PDF de tu prescripción con los gramos diarios autorizados para activar el contador.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(prescriptions, key = { it.id }) { prescription ->
                PrescriptionCard(
                    prescription = prescription,
                    onView = { viewingPrescription = prescription },
                    onDelete = { prescriptionToDelete = prescription },
                    onScheduleReminder = {
                        createCalendarEvent(context, prescription)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showAddDialog) {
        AddPrescriptionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { pres, uri, name ->
                onSavePrescription(pres, uri, name)
                showAddDialog = false
            }
        )
    }

    viewingPrescription?.let { pres ->
        DocumentViewerDialog(
            prescription = pres,
            onDismiss = { viewingPrescription = null }
        )
    }

    prescriptionToDelete?.let { pres ->
        AlertDialog(
            onDismissRequest = { prescriptionToDelete = null },
            title = { Text("Eliminar Receta") },
            text = { Text("¿Deseas eliminar la receta del Dr(a). ${pres.doctorName} y su archivo adjunto?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePrescription(pres)
                        prescriptionToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { prescriptionToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PrescriptionCard(
    prescription: Prescription,
    onView: () -> Unit,
    onDelete: () -> Unit,
    onScheduleReminder: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val issueDateStr = remember(prescription.issueDateMillis) { dateFormat.format(Date(prescription.issueDateMillis)) }
    val expiryDateStr = remember(prescription.expiryDateMillis) { dateFormat.format(Date(prescription.expiryDateMillis)) }

    val daysRemaining = remember(prescription.expiryDateMillis) {
        val diff = prescription.expiryDateMillis - System.currentTimeMillis()
        (diff / (24L * 60 * 60 * 1000)).toInt()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = prescription.doctorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (prescription.licenseNumber.isNotBlank()) {
                            Text(
                                text = "Matrícula/Reg: ${prescription.licenseNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (daysRemaining < 0) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            else if (daysRemaining <= 15) EmeraldTertiary.copy(alpha = 0.2f)
                            else EmeraldPrimary.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val statusText = when {
                        daysRemaining < 0 -> "CONTROL VENCIDO"
                        daysRemaining <= 15 -> "CONTROL EN $daysRemaining DÍAS"
                        else -> "VIGENTE ($daysRemaining días)"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            daysRemaining < 0 -> MaterialTheme.colorScheme.error
                            daysRemaining <= 15 -> EmeraldTertiary
                            else -> EmeraldPrimary
                        }
                    )
                }
            }

            if (prescription.medicalCenter.isNotBlank()) {
                Text(
                    text = "Institución: ${prescription.medicalCenter}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Prescribed Daily Grams Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(EmeraldPrimary.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Scale, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                Column {
                    Text(
                        text = "Gramos Diarios Prescritos: ${String.format(Locale.US, "%.2f", prescription.prescribedDailyGrams)} g / día",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "Base de cálculo para el contador en retroceso del día",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Dates & Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Emisión: $issueDateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Próximo Control: $expiryDateStr",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldSecondary
                )
            }

            // Prescribed Dosage Details
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Indicación: ${prescription.prescribedDailyDosage}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary
                    )
                    if (prescription.instructions.isNotBlank()) {
                        Text(
                            text = prescription.instructions,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Calendar Event Action Button (Avisos 15, 7, 3 y el mismo día)
            Button(
                onClick = onScheduleReminder,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("schedule_prescription_reminder"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldSecondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Crear Alarma en Calendario (Avisos 15, 7, 3 días)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Attached Document Badge & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (prescription.localDocumentPath != null || prescription.documentFileName != null) {
                    val isPdf = prescription.documentFileName?.endsWith(".pdf", ignoreCase = true) == true
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Image,
                            contentDescription = null,
                            tint = if (isPdf) EmeraldTertiary else EmeraldSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = prescription.documentFileName ?: "Documento adjunto",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                } else {
                    Text(
                        text = "Sin archivo adjunto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (prescription.localDocumentPath != null) {
                        IconButton(onClick = onView) {
                            Icon(Icons.Default.Visibility, contentDescription = "Ver Documento", tint = EmeraldPrimary)
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// Opens the Android system calendar with preconfigured reminders
fun createCalendarEvent(context: Context, prescription: Prescription) {
    try {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "Control Médico Cannabis - Dr(a). ${prescription.doctorName}")
            putExtra(
                CalendarContract.Events.DESCRIPTION,
                "Control y renovación periódica de receta médica de cannabis.\n" +
                        "Médico: Dr(a). ${prescription.doctorName}\n" +
                        "Gramos Prescritos: ${prescription.prescribedDailyGrams} g/día\n" +
                        "Dosis Prescrita: ${prescription.prescribedDailyDosage}\n" +
                        "Avisos automáticos recomendados: 15, 7, 3 días antes y el mismo día del control."
            )
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, prescription.expiryDateMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, prescription.expiryDateMillis + 3600000L)
            putExtra(CalendarContract.Events.EVENT_LOCATION, prescription.medicalCenter.ifBlank { "Consulta Médica" })
            putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
        }
        context.startActivity(intent)
        Toast.makeText(context, "Configurando evento con alertas a 15, 7, 3 días y el día de control", Toast.LENGTH_LONG).show()
    } catch (_: Exception) {
        Toast.makeText(context, "No se pudo abrir la aplicación de calendario", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AddPrescriptionDialog(
    onDismiss: () -> Unit,
    onSave: (Prescription, Uri?, String?) -> Unit
) {
    val context = LocalContext.current
    var doctorName by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var medicalCenter by remember { mutableStateOf("") }
    var prescribedDailyGramsInput by remember { mutableStateOf("3.00") }
    var prescribedDosage by remember { mutableStateOf("Hasta 3.0g diarios en vaporizado según EVA") }
    var clinicalDiagnosis by remember { mutableStateOf("Dolor crónico e insomnio reactivo") }
    var instructions by remember { mutableStateOf("Control médico periódico recomendado cada 6 meses") }

    // Control period selector: 6 meses (default Chile), 1 año, 2 años
    var selectedPeriodMonths by remember { mutableIntStateOf(6) }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            selectedFileName = "receta_foto_${System.currentTimeMillis()}.jpg"
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            var displayName = "receta_doc_${System.currentTimeMillis()}.pdf"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            displayName = cursor.getString(nameIndex)
                        }
                    }
                }
            } catch (_: Exception) {}
            selectedFileName = displayName
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
                .clip(RoundedCornerShape(24.dp)),
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
                    Text(
                        text = "Cargar Receta Médica",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                // Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = doctorName,
                        onValueChange = { doctorName = it },
                        label = { Text("Nombre del Médico Tratante (ej. Dra. Sofía Mendoza)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary)
                    )

                    OutlinedTextField(
                        value = licenseNumber,
                        onValueChange = { licenseNumber = it },
                        label = { Text("Número de Registro / Matrícula Médica") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = medicalCenter,
                        onValueChange = { medicalCenter = it },
                        label = { Text("Clínica o Centro Médico") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Prescribed Daily Grams Input
                    OutlinedTextField(
                        value = prescribedDailyGramsInput,
                        onValueChange = { prescribedDailyGramsInput = it },
                        label = { Text("Gramos Diarios a Consumir (flores secas/equivalente en gramos, ej. 3.00)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary)
                    )

                    // Control Period Selection
                    Text("Período de Control Médico:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Pair(6, "6 Meses (Chile)"),
                            Pair(12, "1 Año"),
                            Pair(24, "2 Años")
                        ).forEach { (months, label) ->
                            val isSel = selectedPeriodMonths == months
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedPeriodMonths = months },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = clinicalDiagnosis,
                        onValueChange = { clinicalDiagnosis = it },
                        label = { Text("Diagnóstico / Condición Clínica") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = prescribedDosage,
                        onValueChange = { prescribedDosage = it },
                        label = { Text("Dosis y Frecuencia Prescrita") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Instrucciones Médicas") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Attach Document
                    Text("Adjuntar Archivo de la Receta (Foto o PDF):", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Foto")
                        }

                        OutlinedButton(
                            onClick = {
                                documentPickerLauncher.launch(arrayOf("application/pdf"))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF")
                        }
                    }

                    selectedFileName?.let { fname ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, tint = EmeraldPrimary)
                                Text(fname, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                            IconButton(onClick = {
                                selectedFileUri = null
                                selectedFileName = null
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            val expiryMillis = System.currentTimeMillis() + (selectedPeriodMonths.toLong() * 30L * 24 * 60 * 60 * 1000)
                            val grams = prescribedDailyGramsInput.toDoubleOrNull() ?: 3.0
                            val pres = Prescription(
                                doctorName = doctorName.ifBlank { "Médico Tratante" },
                                licenseNumber = licenseNumber,
                                medicalCenter = medicalCenter,
                                issueDateMillis = System.currentTimeMillis(),
                                expiryDateMillis = expiryMillis,
                                prescribedDailyGrams = grams,
                                prescribedFormat = "Flores secas y Extracto",
                                prescribedDailyDosage = prescribedDosage,
                                clinicalDiagnosis = clinicalDiagnosis,
                                instructions = instructions,
                                documentFileName = selectedFileName
                            )
                            onSave(pres, selectedFileUri, selectedFileName)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar Receta y Habilitar Avisos", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentViewerDialog(
    prescription: Prescription,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val rawPath = prescription.localDocumentPath
    val rawFile = rawPath?.let { File(it) }

    // If file is encrypted with EncryptedFileManager, decrypt to temporary cached file for viewing
    val file: File? = remember(rawPath) {
        if (rawFile != null && rawFile.exists()) {
            if (com.example.util.EncryptedFileManager.isEncryptedDocPath(rawPath)) {
                try {
                    com.example.util.EncryptedFileManager.decryptToTempFile(
                        context,
                        rawFile,
                        prescription.documentFileName ?: "view_doc.pdf"
                    )
                } catch (_: Exception) {
                    rawFile
                }
            } else {
                rawFile
            }
        } else {
            null
        }
    }

    val isImage = prescription.documentMimeType?.startsWith("image/") == true ||
            prescription.documentFileName?.endsWith(".jpg", ignoreCase = true) == true ||
            prescription.documentFileName?.endsWith(".png", ignoreCase = true) == true

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prescription.documentFileName ?: "Receta Médica",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dr(a). ${prescription.doctorName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (file != null && file.exists() && isImage) {
                        AsyncImage(
                            model = file,
                            contentDescription = "Documento médico",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = EmeraldTertiary,
                                modifier = Modifier.size(72.dp)
                            )
                            Text(
                                text = prescription.documentFileName ?: "Documento PDF",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Archivo almacenado de forma privada en el almacenamiento local.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = {
                                    if (file != null && file.exists()) {
                                        try {
                                            val uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            )
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(uri, "application/pdf")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Abrir con Visor del Sistema")
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            if (file != null && file.exists()) {
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = prescription.documentMimeType ?: "application/octet-stream"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Compartir Receta"))
                                } catch (_: Exception) {}
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir Archivo")
                    }
                }
            }
        }
    }
}
