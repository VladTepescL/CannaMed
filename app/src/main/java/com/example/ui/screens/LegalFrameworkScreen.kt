package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LegalExtract
import com.example.data.model.Prescription
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldSecondaryContainer
import com.example.ui.theme.EmeraldTertiary

@Composable
fun LegalFrameworkScreen(
    legalExtracts: List<LegalExtract>,
    activePrescriptions: List<Prescription>,
    onSaveLegalExtract: (LegalExtract) -> Unit,
    onDeleteLegalExtract: (LegalExtract) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEmergencyInspectionMode by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("Todos") }
    var extractToDelete by remember { mutableStateOf<LegalExtract?>(null) }

    val categories = listOf("Todos", "Ley", "Jurisprudencia", "Fiscalización")

    val filteredExtracts = remember(legalExtracts, selectedCategoryFilter) {
        if (selectedCategoryFilter == "Todos") legalExtracts
        else legalExtracts.filter { it.category.contains(selectedCategoryFilter, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            // Title & Add
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Marco Jurídico & Fiscalización",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ley 20.000 Chile, Jurisprudencia y Respaldo Legal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_legal_extract_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir")
                }
            }
        }

        // EMERGENCY POLICE INSPECTION BUTTON (MODO FISCALIZACIÓN)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, EmeraldTertiary, RoundedCornerShape(16.dp))
                    .clickable { showEmergencyInspectionMode = true }
                    .testTag("emergency_police_mode_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(EmeraldTertiary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocalPolice,
                                contentDescription = null,
                                tint = EmeraldTertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "MODO FISCALIZACIÓN POLICIAL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldTertiary
                            )
                            Text(
                                text = "Pantalla de emergencia para exhibir receta vigente y artículos eximentes (Arts. 4, 8 y 50)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSel = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        // Extracts list
        items(filteredExtracts, key = { it.id }) { extract ->
            LegalExtractCard(
                extract = extract,
                onDelete = { extractToDelete = extract }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showAddDialog) {
        AddLegalExtractDialog(
            onDismiss = { showAddDialog = false },
            onSave = {
                onSaveLegalExtract(it)
                showAddDialog = false
            }
        )
    }

    if (showEmergencyInspectionMode) {
        EmergencyPoliceInspectionDialog(
            activePrescriptions = activePrescriptions,
            legalExtracts = legalExtracts,
            onDismiss = { showEmergencyInspectionMode = false }
        )
    }

    extractToDelete?.let { ext ->
        AlertDialog(
            onDismissRequest = { extractToDelete = null },
            title = { Text("Eliminar Extracto Legal") },
            text = { Text("¿Deseas eliminar '${ext.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteLegalExtract(ext)
                        extractToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { extractToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun LegalExtractCard(
    extract: LegalExtract,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text(
                            text = extract.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${extract.category} • ${extract.country}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSecondary
                        )
                    }
                }

                if (!extract.isOfficialPreset) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Practical summary
            Text(
                text = extract.practicalSummary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Relevance for patient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Amparo Clínico: ${extract.relevanceForPatient}",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldPrimary,
                    fontSize = 11.sp
                )
            }

            // Expandable full legal verbatim text
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Texto Legal Literal:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = extract.fullLegalText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (extract.customNotes.isNotBlank()) {
                        Text(
                            text = "Anotaciones Personales: ${extract.customNotes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldTertiary
                        )
                    }
                }
            }

            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (expanded) "Ocultar Texto Literal" else "Ver Texto Legal Completo")
            }
        }
    }
}

// Emergency Inspection Modal - High contrast presentation for police inspection
@Composable
fun EmergencyPoliceInspectionDialog(
    activePrescriptions: List<Prescription>,
    legalExtracts: List<LegalExtract>,
    onDismiss: () -> Unit
) {
    val activePres = activePrescriptions.firstOrNull()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF05100B)),
            color = Color(0xFF05100B)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldTertiary, modifier = Modifier.size(28.dp))
                        Text(
                            text = "FISCALIZACIÓN / CONTROL MÉDICO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = EmeraldTertiary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                // Patient Identification Statement
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF132B21)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "PACIENTE EN TRATAMIENTO MÉDICO AUTORIZADO",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                        Text(
                            text = "El portador o usuario de este dispositivo cuenta con prescripción médica formal para uso medicinal de cannabis bajo el amparo de la legislación chilena (Ley 20.000 Artículos 4, 8 y 50).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }

                // Active Prescription Summary
                if (activePres != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3328)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("DATOS DE LA RECETA MÉDICA VIGENTE:", style = MaterialTheme.typography.labelMedium, color = EmeraldTertiary, fontWeight = FontWeight.Bold)
                            Text("Médico Tratante: Dr(a). ${activePres.doctorName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            if (activePres.licenseNumber.isNotBlank()) {
                                Text("Matrícula / Registro Colegio Médico: ${activePres.licenseNumber}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                            }
                            if (activePres.medicalCenter.isNotBlank()) {
                                Text("Institución: ${activePres.medicalCenter}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                            }
                            Text("Dosis Prescrita: ${activePres.prescribedDailyDosage} (${activePres.prescribedDailyGrams} g/día)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldSecondary)
                            Text("Diagnóstico Clínico: ${activePres.clinicalDiagnosis}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        }
                    }
                }

                // Key Legal Exemption Articles (Arts. 4, 8, 50)
                Text(
                    text = "FUNDAMENTOS JURÍDICOS (LEY 20.000 CHILE):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )

                legalExtracts.take(4).forEach { ext ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1F18)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(ext.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldSecondary)
                            Text(ext.practicalSummary, style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Volver a CannaMed", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddLegalExtractDialog(
    onDismiss: () -> Unit,
    onSave: (LegalExtract) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Ley") }
    var fullLegalText by remember { mutableStateOf("") }
    var practicalSummary by remember { mutableStateOf("") }
    var relevanceForPatient by remember { mutableStateOf("") }
    var customNotes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Añadir Cuerpo Jurídico o Extracto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título (ej. Ley 20.000 Art. 4, Fallo Corte)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría (Ley, Jurisprudencia, Decreto)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = practicalSummary,
                    onValueChange = { practicalSummary = it },
                    label = { Text("Resumen práctico para el paciente") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fullLegalText,
                    onValueChange = { fullLegalText = it },
                    label = { Text("Texto legal completo o extracto") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = relevanceForPatient,
                    onValueChange = { relevanceForPatient = it },
                    label = { Text("Relevancia / Amparo ante fiscalización") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = customNotes,
                    onValueChange = { customNotes = it },
                    label = { Text("Notas adicionales (Causa RUC/RIT, ordenanza)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(
                                LegalExtract(
                                    country = "Chile",
                                    title = title,
                                    category = category,
                                    fullLegalText = fullLegalText,
                                    practicalSummary = practicalSummary,
                                    relevanceForPatient = relevanceForPatient,
                                    isOfficialPreset = false,
                                    customNotes = customNotes
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Guardar Extracto Legal")
                }
            }
        }
    }
}
