package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CannabisFormat
import com.example.data.model.Cartridge
import com.example.data.model.DosageLog
import com.example.data.model.StrainInfo
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddDosageDialog(
    strains: List<StrainInfo>,
    activeCartridges: List<Cartridge>,
    onDismiss: () -> Unit,
    onSave: (DosageLog) -> Unit
) {
    var selectedFormat by remember { mutableStateOf(CannabisFormat.VAPORIZED_FLOWER) }
    var strainName by remember {
        mutableStateOf(strains.firstOrNull()?.name ?: "Harlequin 1:1")
    }
    var strainType by remember {
        mutableStateOf(strains.firstOrNull()?.type ?: "Híbrida")
    }
    var selectedCartridge by remember {
        mutableStateOf(activeCartridges.firstOrNull())
    }

    var puffsCount by remember { mutableIntStateOf(3) }
    var dosageValue by remember { mutableDoubleStateOf(3.0) }
    var dosageUnit by remember { mutableStateOf("Caladas") }
    var temperatureCelsius by remember { mutableIntStateOf(185) }

    var reliefScore by remember { mutableIntStateOf(8) }
    var selectedSymptoms by remember {
        mutableStateOf(setOf("Dolor crónico"))
    }
    var customSymptom by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("Relajado") }
    var selectedSideEffects by remember { mutableStateOf(setOf("Ninguno")) }
    var subjectiveNotes by remember { mutableStateOf("") }

    val commonSymptoms = listOf(
        "Dolor crónico", "Insomnio", "Ansiedad", "Náuseas",
        "Espasmos musculares", "Migraña", "Estrés", "Inflamación"
    )

    val commonMoods = listOf(
        "Relajado", "Calmado", "Enfocado", "Aliviado", "Eufórico"
    )

    val commonSideEffects = listOf(
        "Ninguno", "Boca seca", "Ojos rojos", "Somnolencia", "Apetito (munchies)", "Mareo leve"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
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
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Registrar Toma Medicinal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = SimpleDateFormat("EEEE, dd MMM • HH:mm", Locale.getDefault()).format(Date()),
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_dosage")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. FORMAT SELECTOR
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "1. Formato de Administración",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CannabisFormat.values().forEach { format ->
                                val isSelected = selectedFormat == format
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedFormat = format
                                        dosageUnit = format.defaultUnit
                                        if (format.allowsPuffs && puffsCount == 0) {
                                            puffsCount = 3
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            format.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    label = { Text(format.displayName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldPrimary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    // 2. CARTRIDGE / POD LINK (if applicable)
                    if (selectedFormat == CannabisFormat.CONCENTRATE_VAPE || selectedFormat == CannabisFormat.CLEAR_OIL) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ElectricBolt,
                                        contentDescription = null,
                                        tint = EmeraldTertiary
                                    )
                                    Text(
                                        text = "Vincular a Cartucho / Aceite Clear Activo",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (activeCartridges.isNotEmpty()) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        activeCartridges.forEach { cart ->
                                            val isSelected = selectedCartridge?.id == cart.id
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    selectedCartridge = cart
                                                    strainName = cart.varietyName
                                                },
                                                label = {
                                                    Text("${cart.brandName} - ${cart.varietyName}")
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = EmeraldTertiary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.surface
                                                )
                                            )
                                        }
                                    }
                                    selectedCartridge?.let { cart ->
                                        Text(
                                            text = "THC ${cart.thcPercentage}% | CBD ${cart.cbdPercentage}% • Terpenos: ${cart.terpenesSummary}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EmeraldSecondary
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "No tienes cartuchos activos registrados. Puedes agregarlo desde la pestaña de Cartuchos.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 3. STRAIN SELECTION
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "2. Cepa o Variedad Medicinal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedTextField(
                            value = strainName,
                            onValueChange = { strainName = it },
                            label = { Text("Nombre de la variedad") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("strain_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        // Strains quick chips
                        Text(
                            text = "Sugerencias de biblioteca:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            strains.take(6).forEach { st ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .clickable {
                                            strainName = st.name
                                            strainType = st.type
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = st.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // 4. DOSAGE & PUFFS
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "3. Cantidad y Dosis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (selectedFormat.allowsPuffs) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Cantidad de Caladas (Puffs)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Inhalaciones efectuadas en esta toma",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = { if (puffsCount > 1) puffsCount-- },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.surface,
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Menos")
                                        }
                                        Text(
                                            text = "$puffsCount",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                        IconButton(
                                            onClick = { puffsCount++ },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.surface,
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Más")
                                        }
                                    }
                                }
                            }
                        } else {
                            // Non-puff formats (oil, drops, edibles, capsules)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = if (dosageValue % 1.0 == 0.0) dosageValue.toInt().toString() else dosageValue.toString(),
                                    onValueChange = {
                                        dosageValue = it.toDoubleOrNull() ?: 1.0
                                    },
                                    label = { Text("Valor de dosis") },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldPrimary
                                    )
                                )
                                OutlinedTextField(
                                    value = dosageUnit,
                                    onValueChange = { dosageUnit = it },
                                    label = { Text("Unidad") },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldPrimary
                                    )
                                )
                            }
                        }

                        // Temperature for Herbal Vaporizer / Dab
                        if (selectedFormat.allowsTemperature) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Thermostat,
                                        contentDescription = null,
                                        tint = EmeraldTertiary
                                    )
                                    Text(
                                        text = "Temperatura de extracción:",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Text(
                                    text = "$temperatureCelsius °C",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldTertiary
                                )
                            }
                            Slider(
                                value = temperatureCelsius.toFloat(),
                                onValueChange = { temperatureCelsius = it.toInt() },
                                valueRange = 160f..225f,
                                steps = 12,
                                colors = SliderDefaults.colors(
                                    thumbColor = EmeraldTertiary,
                                    activeTrackColor = EmeraldTertiary
                                )
                            )
                        }
                    }

                    // 5. RELIEF RATING (1 - 10)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "4. Nivel de Alivio Terapéutico",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$reliefScore / 10",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (reliefScore >= 7) EmeraldPrimary else EmeraldTertiary
                            )
                        }
                        Slider(
                            value = reliefScore.toFloat(),
                            onValueChange = { reliefScore = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = EmeraldPrimary,
                                activeTrackColor = EmeraldPrimary
                            )
                        )
                        val reliefLabel = when (reliefScore) {
                            in 1..3 -> "Alivio leve o mínimo"
                            in 4..6 -> "Alivio moderado tolerable"
                            in 7..8 -> "Muy buen alivio sintomático"
                            else -> "Alivio completo y excelente confort"
                        }
                        Text(
                            text = "Evaluación: $reliefLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 6. SYMPTOMS & EFFECTS
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "5. Síntomas Tratados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            commonSymptoms.forEach { symptom ->
                                val isSelected = selectedSymptoms.contains(symptom)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedSymptoms = if (isSelected) {
                                            selectedSymptoms - symptom
                                        } else {
                                            selectedSymptoms + symptom
                                        }
                                    },
                                    label = { Text(symptom) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldPrimary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    // 7. MOOD EFFECT
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "6. Estado de Ánimo y Mente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            commonMoods.forEach { mood ->
                                val isSelected = selectedMood == mood
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedMood = mood },
                                    label = { Text(mood) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldSecondary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    // 8. SIDE EFFECTS
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "7. Efectos Secundarios / Adversos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            commonSideEffects.forEach { effect ->
                                val isSelected = selectedSideEffects.contains(effect)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedSideEffects = if (effect == "Ninguno") {
                                            setOf("Ninguno")
                                        } else {
                                            val newSet = selectedSideEffects - "Ninguno"
                                            if (isSelected) newSet - effect else newSet + effect
                                        }
                                    },
                                    label = { Text(effect) }
                                )
                            }
                        }
                    }

                    // 9. SUBJECTIVE NOTES
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "8. Notas Subjetivas sobre el Efecto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedTextField(
                            value = subjectiveNotes,
                            onValueChange = { subjectiveNotes = it },
                            placeholder = { Text("Describe sensaciones corporales, rapidez de inicio, duración, pensamientos o recomendaciones para tu médico...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .testTag("subjective_notes_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary
                            )
                        )
                    }
                }

                // Bottom Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            val log = DosageLog(
                                timestamp = System.currentTimeMillis(),
                                format = selectedFormat,
                                strainName = strainName.ifBlank { "Variedad General" },
                                strainType = strainType,
                                dosageValue = if (selectedFormat.allowsPuffs) puffsCount.toDouble() else dosageValue,
                                dosageUnit = if (selectedFormat.allowsPuffs) "Caladas" else dosageUnit,
                                puffsCount = if (selectedFormat.allowsPuffs) puffsCount else 0,
                                temperatureCelsius = if (selectedFormat.allowsTemperature) temperatureCelsius else null,
                                cartridgeId = selectedCartridge?.id,
                                thcPercentage = selectedCartridge?.thcPercentage ?: 0f,
                                cbdPercentage = selectedCartridge?.cbdPercentage ?: 0f,
                                terpenesNote = selectedCartridge?.terpenesSummary ?: "",
                                reliefScore = reliefScore,
                                treatedSymptoms = selectedSymptoms.joinToString(", ").ifBlank { "Mantenimiento general" },
                                moodEffect = selectedMood,
                                sideEffects = selectedSideEffects.joinToString(", ").ifBlank { "Ninguno" },
                                subjectiveNotes = subjectiveNotes.trim()
                            )
                            onSave(log)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_dosage_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Guardar Toma en el Diario",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
