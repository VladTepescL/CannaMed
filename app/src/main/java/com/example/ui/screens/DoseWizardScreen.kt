package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.CannabisFormat
import com.example.data.model.Cartridge
import com.example.data.model.DosageLog
import com.example.data.model.DoseEquivalenceCalculator
import com.example.data.model.PuffDuration
import com.example.data.model.StrainInfo
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldSecondaryContainer
import com.example.ui.theme.EmeraldTertiary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DoseWizardScreen(
    activeCartridges: List<Cartridge>,
    strains: List<StrainInfo>,
    dailyGramsProgress: com.example.ui.DailyGramsProgress? = null,
    onClose: () -> Unit,
    onSaveLog: (DosageLog) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var showEquivalenceDialog by remember { mutableStateOf(false) }

    // Step 1: Format
    var selectedFormat by remember { mutableStateOf(CannabisFormat.CONCENTRATE_VAPE) }

    // Inhalation / Puff duration selector: Corta (1-2s), Estándar (3-4s), Larga (5s+)
    var selectedPuffDuration by remember { mutableStateOf(PuffDuration.STANDARD) }

    // Step 2 variables according to format:
    // Clear Pod specific
    var selectedCartridge by remember { mutableStateOf(activeCartridges.firstOrNull()) }
    var podPuffs by remember { mutableIntStateOf(3) }

    // Pyrolytic (Fumado) specific
    var pyrolyticSubtype by remember { mutableStateOf("Papel / Joint") }
    var pyrolyticInputMode by remember { mutableStateOf("Gramos") } // "Gramos" or "Caladas"
    var pyrolyticGrams by remember { mutableDoubleStateOf(0.25) }
    var pyrolyticPuffs by remember { mutableIntStateOf(4) }

    // Drops specific
    var dropsCount by remember { mutableIntStateOf(5) }
    var dropsStrainName by remember { mutableStateOf("Aceite Sublingual CBD / THC") }

    // Edibles specific (Comible vs Bebible)
    var edibleType by remember { mutableStateOf("Comible") } // "Comible" or "Bebible"
    var edibleMg by remember { mutableDoubleStateOf(10.0) }
    var edibleUnitName by remember { mutableStateOf("Gomita(s) medicinal") }

    // Vaporized flower
    var vapeInputMode by remember { mutableStateOf("Caladas") } // "Caladas" or "Gramos en cámara"
    var vapeTemperature by remember { mutableIntStateOf(185) }
    var vapePuffs by remember { mutableIntStateOf(4) }
    var vapeChamberGrams by remember { mutableDoubleStateOf(0.18) }
    var vapeStrainName by remember { mutableStateOf(strains.firstOrNull()?.name ?: "Harlequin 1:1") }

    // General Strain
    var generalStrainName by remember { mutableStateOf(strains.firstOrNull()?.name ?: "Harlequin 1:1") }
    var generalStrainType by remember { mutableStateOf(strains.firstOrNull()?.type ?: "Híbrida") }

    // Step 3: Subjective effects & evaluation
    var reliefScore by remember { mutableIntStateOf(8) }
    var selectedSymptoms by remember { mutableStateOf(setOf("Dolor crónico")) }
    var selectedMood by remember { mutableStateOf("Relajado") }
    var selectedSideEffects by remember { mutableStateOf(setOf("Ninguno")) }
    var subjectiveNotes by remember { mutableStateOf("") }

    val commonSymptoms = listOf(
        "Dolor crónico", "Insomnio", "Ansiedad", "Náuseas",
        "Espasmos musculares", "Migraña", "Estrés", "Inflamación"
    )
    val commonMoods = listOf("Relajado", "Calmado", "Enfocado", "Aliviado", "Eufórico")
    val commonSideEffects = listOf("Ninguno", "Boca seca", "Ojos rojos", "Somnolencia", "Apetito (munchies)", "Mareo leve")

    // Dynamic Dried Flower Equivalent calculation
    val calculatedEquivalentGrams by remember(
        selectedFormat, selectedPuffDuration, podPuffs,
        pyrolyticInputMode, pyrolyticGrams, pyrolyticPuffs,
        dropsCount, edibleType, edibleMg,
        vapeInputMode, vapePuffs, vapeChamberGrams
    ) {
        derivedStateOf {
            when (selectedFormat) {
                CannabisFormat.CONCENTRATE_VAPE, CannabisFormat.CLEAR_OIL, CannabisFormat.DABBING -> {
                    DoseEquivalenceCalculator.calculateEquivalentDriedFlowerGrams(
                        format = selectedFormat,
                        dosageValue = podPuffs.toDouble(),
                        puffsCount = podPuffs,
                        puffDuration = selectedPuffDuration
                    )
                }

                CannabisFormat.SMOKED_JOINT, CannabisFormat.SMOKED_PIPE, CannabisFormat.SMOKED_BONG -> {
                    if (pyrolyticInputMode == "Gramos") {
                        pyrolyticGrams
                    } else {
                        DoseEquivalenceCalculator.calculateEquivalentDriedFlowerGrams(
                            format = selectedFormat,
                            dosageValue = 0.0,
                            puffsCount = pyrolyticPuffs,
                            puffDuration = selectedPuffDuration
                        )
                    }
                }

                CannabisFormat.DROPS_TINCTURE -> {
                    DoseEquivalenceCalculator.calculateEquivalentDriedFlowerGrams(
                        format = selectedFormat,
                        dosageValue = dropsCount.toDouble(),
                        puffsCount = 0
                    )
                }

                CannabisFormat.EDIBLE, CannabisFormat.CAPSULES -> {
                    DoseEquivalenceCalculator.calculateEquivalentDriedFlowerGrams(
                        format = selectedFormat,
                        dosageValue = edibleMg,
                        puffsCount = 0,
                        isEdibleBeverage = (edibleType == "Bebible")
                    )
                }

                CannabisFormat.VAPORIZED_FLOWER -> {
                    if (vapeInputMode == "Gramos en cámara") {
                        vapeChamberGrams
                    } else {
                        DoseEquivalenceCalculator.calculateEquivalentDriedFlowerGrams(
                            format = selectedFormat,
                            dosageValue = 0.0,
                            puffsCount = vapePuffs,
                            puffDuration = selectedPuffDuration
                        )
                    }
                }

                else -> {
                    0.050
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar for Wizard
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
                    IconButton(
                        onClick = {
                            if (currentStep > 1) currentStep-- else onClose()
                        },
                        modifier = Modifier.testTag("wizard_back_button")
                    ) {
                        Icon(
                            if (currentStep > 1) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column {
                        Text(
                            text = "Paso $currentStep de 4",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (currentStep) {
                                1 -> "Selecciona el Formato"
                                2 -> "Dosificación y Equivalencias"
                                3 -> "Efecto Terapéutico y Notas"
                                else -> "Confirmar y Descontar Gramos"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { currentStep / 4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = EmeraldPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )

            // Step Content Animated
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "WizardStepAnimation"
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (step) {
                            // ==================== PASO 1: SELECCIONAR FORMATO ====================
                            1 -> {
                                Text(
                                    text = "¿Cómo vas a administrar tu dosis medicinal?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Elige el método. Cada formato cuenta con opciones y mediciones estandarizadas a flor seca:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val wizardFormats = listOf(
                                    Pair(CannabisFormat.CONCENTRATE_VAPE, "Clear Pod / Cartucho 510\n(Pods abiertos, tipo de calada y equivalencia en flor)"),
                                    Pair(CannabisFormat.SMOKED_JOINT, "Pirolítico / Combustión\n(Papel, pipa o bong en gramos 0.00g o caladas)"),
                                    Pair(CannabisFormat.DROPS_TINCTURE, "Gotas / Tintura Sublingual\n(Administración en gotas o ml con descuento en flor)"),
                                    Pair(CannabisFormat.EDIBLE, "Edibles / Comestibles\n(Comible o bebible en mg calculados a flor)"),
                                    Pair(CannabisFormat.VAPORIZED_FLOWER, "Vaporizado Herbal / Flor\n(Temperatura °C, caladas cortas/estándar/largas)"),
                                    Pair(CannabisFormat.TOPICAL, "Tópico / Crema\n(Aplicación cutánea local)"),
                                    Pair(CannabisFormat.DABBING, "Dabbing / Concentrado\n(Extracción sólida wax/rosin)")
                                )

                                wizardFormats.forEach { (format, description) ->
                                    val isSelected = selectedFormat == format
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { selectedFormat = format }
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(16.dp)
                                            ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surface),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    format.icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else EmeraldPrimary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = format.displayName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = "Seleccionado", tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // ==================== PASO 2: DOSIFICACIÓN Y EQUIVALENCIAS ====================
                            2 -> {
                                when (selectedFormat) {
                                    // FORMATO 1: CLEAR POD (Cápsulas abiertas + Caladas + Intensidad de Calada)
                                    CannabisFormat.CONCENTRATE_VAPE, CannabisFormat.CLEAR_OIL, CannabisFormat.DABBING -> {
                                        Text(
                                            text = "Control de Clear Pod / Concentrado",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )
                                        Text(
                                            text = "Selecciona el pod abierto, la cantidad y duración de las caladas:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        // Open Pods List
                                        if (activeCartridges.isNotEmpty()) {
                                            activeCartridges.forEach { cart ->
                                                val isSelected = selectedCartridge?.id == cart.id
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .clickable {
                                                            selectedCartridge = cart
                                                            generalStrainName = cart.varietyName
                                                        }
                                                        .border(
                                                            width = if (isSelected) 2.dp else 1.dp,
                                                            color = if (isSelected) EmeraldTertiary else MaterialTheme.colorScheme.outlineVariant,
                                                            shape = RoundedCornerShape(14.dp)
                                                        ),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(14.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = "${cart.brandName} - ${cart.varietyName}",
                                                                style = MaterialTheme.typography.titleSmall,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = "THC ${cart.thcPercentage}% | CBD ${cart.cbdPercentage}% • ${cart.totalPuffsLogged} caladas acumuladas",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = EmeraldSecondary,
                                                                fontSize = 11.sp
                                                            )
                                                        }
                                                        if (isSelected) {
                                                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldTertiary)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Selector de Duración / Intensidad de Calada (Corta, Estándar, Larga)
                                        PuffDurationSelector(
                                            selectedDuration = selectedPuffDuration,
                                            onSelectDuration = { selectedPuffDuration = it }
                                        )

                                        // Puffs Counter Stepper
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Text(
                                                    text = "Cantidad de Caladas en esta toma:",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButton(
                                                        onClick = { if (podPuffs > 1) podPuffs-- },
                                                        modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.surface, CircleShape)
                                                    ) {
                                                        Icon(Icons.Default.Remove, contentDescription = "Menos")
                                                    }
                                                    Text(
                                                        text = "$podPuffs caladas",
                                                        style = MaterialTheme.typography.headlineMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = EmeraldPrimary
                                                    )
                                                    IconButton(
                                                        onClick = { podPuffs++ },
                                                        modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.surface, CircleShape)
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = "Más")
                                                    }
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    listOf(1, 2, 3, 5, 8).forEach { qVal ->
                                                        OutlinedButton(
                                                            onClick = { podPuffs = qVal },
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text("$qVal")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // FORMATO 2: PIROLÍTICO / COMBUSTIÓN (Gramos formato 0.00 o Caladas)
                                    CannabisFormat.SMOKED_JOINT, CannabisFormat.SMOKED_PIPE, CannabisFormat.SMOKED_BONG -> {
                                        Text(
                                            text = "Administración Pirolítica (Combustión)",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )

                                        // Subtype: Papel, Pipa o Bong
                                        Text("Método pirolítico:", style = MaterialTheme.typography.labelMedium)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("Papel / Joint", "Pipa de vidrio", "Bong de agua").forEach { sub ->
                                                val isSel = pyrolyticSubtype == sub
                                                FilterChip(
                                                    selected = isSel,
                                                    onClick = { pyrolyticSubtype = sub },
                                                    label = { Text(sub) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = EmeraldPrimary,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                )
                                            }
                                        }

                                        // Switch entre Gramos directos y Caladas
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            FilterChip(
                                                selected = pyrolyticInputMode == "Gramos",
                                                onClick = { pyrolyticInputMode = "Gramos" },
                                                label = { Text("Por Peso en Gramos (0.00g)") },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary)
                                            )
                                            FilterChip(
                                                selected = pyrolyticInputMode == "Caladas",
                                                onClick = { pyrolyticInputMode = "Caladas" },
                                                label = { Text("Por Caladas") },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary)
                                            )
                                        }

                                        if (pyrolyticInputMode == "Gramos") {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    Text(
                                                        text = "Cantidad en Gramos (0.00 formato):",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        IconButton(
                                                            onClick = {
                                                                pyrolyticGrams = ((pyrolyticGrams - 0.05).coerceAtLeast(0.05) * 100).toInt() / 100.0
                                                            },
                                                            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, CircleShape)
                                                        ) {
                                                            Icon(Icons.Default.Remove, contentDescription = "Menos")
                                                        }
                                                        Text(
                                                            text = String.format(Locale.US, "%.2f g", pyrolyticGrams),
                                                            style = MaterialTheme.typography.headlineMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = EmeraldTertiary
                                                        )
                                                        IconButton(
                                                            onClick = {
                                                                pyrolyticGrams = ((pyrolyticGrams + 0.05) * 100).toInt() / 100.0
                                                            },
                                                            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, CircleShape)
                                                        ) {
                                                            Icon(Icons.Default.Add, contentDescription = "Más")
                                                        }
                                                    }

                                                    FlowRow(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        listOf(0.10, 0.20, 0.25, 0.35, 0.50, 0.75, 1.00).forEach { gVal ->
                                                            OutlinedButton(
                                                                onClick = { pyrolyticGrams = gVal },
                                                                shape = RoundedCornerShape(8.dp)
                                                            ) {
                                                                Text(String.format(Locale.US, "%.2f g", gVal))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            PuffDurationSelector(
                                                selectedDuration = selectedPuffDuration,
                                                onSelectDuration = { selectedPuffDuration = it }
                                            )
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    Text("Caladas fumadas:", fontWeight = FontWeight.Bold)
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        IconButton(onClick = { if (pyrolyticPuffs > 1) pyrolyticPuffs-- }) {
                                                            Icon(Icons.Default.Remove, contentDescription = null)
                                                        }
                                                        Text("$pyrolyticPuffs caladas", fontWeight = FontWeight.Bold, color = EmeraldPrimary, style = MaterialTheme.typography.titleLarge)
                                                        IconButton(onClick = { pyrolyticPuffs++ }) {
                                                            Icon(Icons.Default.Add, contentDescription = null)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = generalStrainName,
                                            onValueChange = { generalStrainName = it },
                                            label = { Text("Variedad / Cepa fumada") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    // FORMATO 3: GOTAS / TINTURA SUBLINGUAL
                                    CannabisFormat.DROPS_TINCTURE -> {
                                        Text(
                                            text = "Administración en Gotas Sublinguales",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Text(
                                                    text = "Cantidad de Gotas administradas:",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButton(
                                                        onClick = { if (dropsCount > 1) dropsCount-- },
                                                        modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.surface, CircleShape)
                                                    ) {
                                                        Icon(Icons.Default.Remove, contentDescription = null)
                                                    }
                                                    Text(
                                                        text = "$dropsCount gotas",
                                                        style = MaterialTheme.typography.headlineMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = EmeraldPrimary
                                                    )
                                                    IconButton(
                                                        onClick = { dropsCount++ },
                                                        modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.surface, CircleShape)
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = null)
                                                    }
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    listOf(2, 3, 5, 8, 10, 15, 20).forEach { d ->
                                                        OutlinedButton(
                                                            onClick = { dropsCount = d },
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text("$d")
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = dropsStrainName,
                                            onValueChange = { dropsStrainName = it },
                                            label = { Text("Nombre o ratio del aceite (ej. CBD 20:1 o THC 1:1)") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    // FORMATO 4: EDIBLES / COMESTIBLES Y BEBIBLES
                                    CannabisFormat.EDIBLE, CannabisFormat.CAPSULES -> {
                                        Text(
                                            text = "Administración de Edibles / Ingestión",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )

                                        Text("Subformato de Edible:", style = MaterialTheme.typography.labelMedium)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { edibleType = "Comible" }
                                                    .border(
                                                        if (edibleType == "Comible") 2.dp else 1.dp,
                                                        if (edibleType == "Comible") EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant,
                                                        RoundedCornerShape(12.dp)
                                                    ),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (edibleType == "Comible") MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(14.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = EmeraldPrimary)
                                                    Column {
                                                        Text("Comible", fontWeight = FontWeight.Bold)
                                                        Text("Gomitas, galletas, cápsulas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }

                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { edibleType = "Bebible" }
                                                    .border(
                                                        if (edibleType == "Bebible") 2.dp else 1.dp,
                                                        if (edibleType == "Bebible") EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant,
                                                        RoundedCornerShape(12.dp)
                                                    ),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (edibleType == "Bebible") MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(14.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(Icons.Default.LocalDrink, contentDescription = null, tint = EmeraldSecondary)
                                                    Column {
                                                        Text("Bebible", fontWeight = FontWeight.Bold)
                                                        Text("Infusiones, gotas solubles", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Text(
                                                    text = "Cantidad de Cannabinoides (mg):",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButton(onClick = { if (edibleMg > 2.5) edibleMg -= 2.5 }) {
                                                        Icon(Icons.Default.Remove, contentDescription = null)
                                                    }
                                                    Text(
                                                        text = "${edibleMg.toInt()} mg",
                                                        style = MaterialTheme.typography.headlineMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = EmeraldPrimary
                                                    )
                                                    IconButton(onClick = { edibleMg += 2.5 }) {
                                                        Icon(Icons.Default.Add, contentDescription = null)
                                                    }
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    listOf(5.0, 10.0, 15.0, 20.0, 25.0, 50.0).forEach { mg ->
                                                        OutlinedButton(
                                                            onClick = { edibleMg = mg },
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text("${mg.toInt()}mg")
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = edibleUnitName,
                                            onValueChange = { edibleUnitName = it },
                                            label = { Text("Descripción de la porción (ej. 1 Gomita sabor frutos rojos)") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    // FORMATO 5: VAPORIZADO HERBAL DE FLOR SECA
                                    else -> {
                                        Text(
                                            text = "Vaporizado Herbal de Flor Seca",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )

                                        // Temperature Slider
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("Temperatura de vaporización:", fontWeight = FontWeight.Bold)
                                                    Text("$vapeTemperature °C", fontWeight = FontWeight.Bold, color = EmeraldTertiary)
                                                }
                                                Slider(
                                                    value = vapeTemperature.toFloat(),
                                                    onValueChange = { vapeTemperature = it.toInt() },
                                                    valueRange = 160f..225f,
                                                    steps = 12,
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = EmeraldTertiary,
                                                        activeTrackColor = EmeraldTertiary
                                                    )
                                                )
                                            }
                                        }

                                        // Calada duration selector
                                        PuffDurationSelector(
                                            selectedDuration = selectedPuffDuration,
                                            onSelectDuration = { selectedPuffDuration = it }
                                        )

                                        // Puffs Stepper
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Caladas inhaladas:", fontWeight = FontWeight.Bold)
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                IconButton(onClick = { if (vapePuffs > 1) vapePuffs-- }) {
                                                    Icon(Icons.Default.Remove, contentDescription = null)
                                                }
                                                Text("$vapePuffs", fontWeight = FontWeight.Bold, color = EmeraldPrimary, style = MaterialTheme.typography.titleLarge)
                                                IconButton(onClick = { vapePuffs++ }) {
                                                    Icon(Icons.Default.Add, contentDescription = null)
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = vapeStrainName,
                                            onValueChange = { vapeStrainName = it },
                                            label = { Text("Variedad / Flor utilizada") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                // LIVE CLINICAL EQUIVALENCE CARD (Calculado a Flor Seca Estándar)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.Scale, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                                Text(
                                                    text = "Equivalencia en Flor Seca Estándar",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldPrimary
                                                )
                                            }
                                            IconButton(
                                                onClick = { showEquivalenceDialog = true },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Info, contentDescription = "Ver tabla de equivalencias", tint = EmeraldSecondary)
                                            }
                                        }

                                         Text(
                                            text = "~${String.format(Locale.US, "%.3f", calculatedEquivalentGrams)} g de flor seca",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EmeraldPrimary
                                        )

                                        // Real-time stock reduction calculation & preview
                                        dailyGramsProgress?.let { prog ->
                                            val remainingAfterDose = (prog.remainingGrams - calculatedEquivalentGrams).coerceAtLeast(0.0)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surface)
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Saldo diario restante:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    text = "${String.format(Locale.US, "%.2f", prog.remainingGrams)}g  ➔  ${String.format(Locale.US, "%.2f", remainingAfterDose)}g",
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (remainingAfterDose > 0.0) EmeraldPrimary else Color(0xFFFFB74D),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Se descontará automáticamente ${String.format(Locale.US, "%.3f", calculatedEquivalentGrams)} g de tu saldo prescrito diario según el estándar clínico oficial.",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // ==================== PASO 3: NOTAS SUBJETIVAS Y EFECTO ====================
                            3 -> {
                                Text(
                                    text = "Evaluación Terapéutica y Efecto",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = "Registra el nivel de alivio y añade notas subjetivas sobre cómo respondió tu organismo:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Relief Score (1 to 10)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Nivel de Alivio:", fontWeight = FontWeight.Bold)
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
                                    }
                                }

                                // Symptoms treated
                                Text("Síntomas aliviados en esta toma:", style = MaterialTheme.typography.labelMedium)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    commonSymptoms.forEach { sym ->
                                        val isSel = selectedSymptoms.contains(sym)
                                        FilterChip(
                                            selected = isSel,
                                            onClick = {
                                                selectedSymptoms = if (isSel) selectedSymptoms - sym else selectedSymptoms + sym
                                            },
                                            label = { Text(sym) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EmeraldPrimary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }

                                // Mood
                                Text("Estado anímico posterior:", style = MaterialTheme.typography.labelMedium)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    commonMoods.forEach { mood ->
                                        val isSel = selectedMood == mood
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { selectedMood = mood },
                                            label = { Text(mood) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EmeraldSecondary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }

                                // Side effects
                                Text("Efectos secundarios (si hubiese):", style = MaterialTheme.typography.labelMedium)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    commonSideEffects.forEach { eff ->
                                        val isSel = selectedSideEffects.contains(eff)
                                        FilterChip(
                                            selected = isSel,
                                            onClick = {
                                                selectedSideEffects = if (eff == "Ninguno") setOf("Ninguno")
                                                else {
                                                    val s = selectedSideEffects - "Ninguno"
                                                    if (isSel) s - eff else s + eff
                                                }
                                            },
                                            label = { Text(eff) }
                                        )
                                    }
                                }

                                // Subjective Notes
                                Text("Notas subjetivas sobre el efecto en cada toma:", style = MaterialTheme.typography.labelMedium)
                                OutlinedTextField(
                                    value = subjectiveNotes,
                                    onValueChange = { subjectiveNotes = it },
                                    placeholder = { Text("Escribe cómo te sientes: tiempo de inicio, sensaciones corporales, lucidez o notas para llevar al médico...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .testTag("wizard_subjective_notes"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary)
                                )
                            }

                            // ==================== PASO 4: CONFIRMAR Y GUARDAR ====================
                            4 -> {
                                Text(
                                    text = "Confirmación de la Toma",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = "Revisa el cálculo de equivalencia antes de incorporarlo al diario médico:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "Resumen de Registro y Descuento",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Método de Administración:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(selectedFormat.displayName, fontWeight = FontWeight.Bold)
                                        }

                                        when (selectedFormat) {
                                            CannabisFormat.CONCENTRATE_VAPE, CannabisFormat.CLEAR_OIL, CannabisFormat.DABBING -> {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Cápsula / Pod:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text(selectedCartridge?.varietyName ?: "Clear Pod General", fontWeight = FontWeight.Bold)
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Caladas e Intensidad:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text("$podPuffs caladas (${selectedPuffDuration.displayName})", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                                }
                                            }
                                            CannabisFormat.SMOKED_JOINT, CannabisFormat.SMOKED_PIPE, CannabisFormat.SMOKED_BONG -> {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Método Pirolítico:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text(pyrolyticSubtype, fontWeight = FontWeight.Bold)
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Cantidad:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    val txt = if (pyrolyticInputMode == "Gramos") String.format(Locale.US, "%.2f gramos", pyrolyticGrams) else "$pyrolyticPuffs caladas (${selectedPuffDuration.displayName})"
                                                    Text(txt, fontWeight = FontWeight.Bold, color = EmeraldTertiary)
                                                }
                                            }
                                            CannabisFormat.DROPS_TINCTURE -> {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Gotas administradas:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text("$dropsCount gotas", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                                }
                                            }
                                            CannabisFormat.EDIBLE, CannabisFormat.CAPSULES -> {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Tipo y Dosis:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text("$edibleType (${edibleMg.toInt()} mg)", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                                }
                                            }
                                            else -> {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Caladas e Inhalación:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text("$vapePuffs caladas (${selectedPuffDuration.displayName})", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                                }
                                            }
                                        }

                                        // Highlighted Discount row
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(EmeraldPrimary.copy(alpha = 0.15f))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Descuento en Cupo Diario:",
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldPrimary
                                            )
                                            Text(
                                                text = "-${String.format(Locale.US, "%.3f", calculatedEquivalentGrams)} g flor seca",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = EmeraldPrimary
                                            )
                                        }

                                        dailyGramsProgress?.let { prog ->
                                            val remainingAfterDose = (prog.remainingGrams - calculatedEquivalentGrams).coerceAtLeast(0.0)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Saldo Restante tras Registrar:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    text = "${String.format(Locale.US, "%.2f", remainingAfterDose)} g restantes hoy",
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldPrimary
                                                )
                                            }
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Nivel de Alivio:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("$reliefScore / 10", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                        }

                                        if (subjectiveNotes.isNotBlank()) {
                                            Text("Nota subjetiva:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                            Text("\"$subjectiveNotes\"", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Anterior")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (currentStep < 4) {
                    Button(
                        onClick = { currentStep++ },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.height(48.dp).testTag("wizard_next_button")
                    ) {
                        Text("Siguiente Paso")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Button(
                        onClick = {
                            val finalDoseValue = when (selectedFormat) {
                                CannabisFormat.CONCENTRATE_VAPE, CannabisFormat.CLEAR_OIL, CannabisFormat.DABBING -> podPuffs.toDouble()
                                CannabisFormat.SMOKED_JOINT, CannabisFormat.SMOKED_PIPE, CannabisFormat.SMOKED_BONG -> {
                                    if (pyrolyticInputMode == "Gramos") pyrolyticGrams else pyrolyticPuffs.toDouble()
                                }
                                CannabisFormat.DROPS_TINCTURE -> dropsCount.toDouble()
                                CannabisFormat.EDIBLE, CannabisFormat.CAPSULES -> edibleMg
                                else -> vapePuffs.toDouble()
                            }
                            val finalUnit = when (selectedFormat) {
                                CannabisFormat.CONCENTRATE_VAPE, CannabisFormat.CLEAR_OIL, CannabisFormat.DABBING -> "Caladas"
                                CannabisFormat.SMOKED_JOINT, CannabisFormat.SMOKED_PIPE, CannabisFormat.SMOKED_BONG -> {
                                    if (pyrolyticInputMode == "Gramos") "g" else "Caladas"
                                }
                                CannabisFormat.DROPS_TINCTURE -> "Gotas"
                                CannabisFormat.EDIBLE, CannabisFormat.CAPSULES -> "mg"
                                else -> "Caladas"
                            }
                            val finalStrainName = when (selectedFormat) {
                                CannabisFormat.CONCENTRATE_VAPE, CannabisFormat.CLEAR_OIL, CannabisFormat.DABBING -> selectedCartridge?.varietyName ?: generalStrainName
                                CannabisFormat.DROPS_TINCTURE -> dropsStrainName
                                CannabisFormat.VAPORIZED_FLOWER -> vapeStrainName
                                else -> generalStrainName
                            }
                            val finalPuffs = when (selectedFormat) {
                                CannabisFormat.CONCENTRATE_VAPE, CannabisFormat.CLEAR_OIL, CannabisFormat.DABBING -> podPuffs
                                CannabisFormat.SMOKED_JOINT, CannabisFormat.SMOKED_PIPE, CannabisFormat.SMOKED_BONG -> {
                                    if (pyrolyticInputMode == "Caladas") pyrolyticPuffs else 0
                                }
                                CannabisFormat.VAPORIZED_FLOWER -> vapePuffs
                                else -> 0
                            }

                            val log = DosageLog(
                                timestamp = System.currentTimeMillis(),
                                format = selectedFormat,
                                strainName = finalStrainName.ifBlank { "Variedad Medicinal" },
                                strainType = generalStrainType,
                                dosageValue = finalDoseValue,
                                dosageUnit = finalUnit,
                                puffsCount = finalPuffs,
                                puffIntensity = selectedPuffDuration.displayName,
                                equivalentGramsDriedFlower = calculatedEquivalentGrams,
                                temperatureCelsius = if (selectedFormat == CannabisFormat.VAPORIZED_FLOWER) vapeTemperature else null,
                                cartridgeId = if (selectedFormat == CannabisFormat.CONCENTRATE_VAPE || selectedFormat == CannabisFormat.CLEAR_OIL) selectedCartridge?.id else null,
                                thcPercentage = selectedCartridge?.thcPercentage ?: 0f,
                                cbdPercentage = selectedCartridge?.cbdPercentage ?: 0f,
                                terpenesNote = selectedCartridge?.terpenesSummary ?: "",
                                reliefScore = reliefScore,
                                treatedSymptoms = selectedSymptoms.joinToString(", ").ifBlank { "Mantenimiento general" },
                                moodEffect = selectedMood,
                                sideEffects = selectedSideEffects.joinToString(", ").ifBlank { "Ninguno" },
                                subjectiveNotes = subjectiveNotes.trim()
                            )
                            onSaveLog(log)
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.height(48.dp).testTag("wizard_confirm_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirmar y Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showEquivalenceDialog) {
        EquivalenceScaleDialog(onDismiss = { showEquivalenceDialog = false })
    }
}

// Puff Duration & Intensity Selector (Corta, Estándar, Larga)
@Composable
fun PuffDurationSelector(
    selectedDuration: PuffDuration,
    onSelectDuration: (PuffDuration) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Tipo e Intensidad de Calada:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PuffDuration.values().forEach { dur ->
                val isSel = selectedDuration == dur
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectDuration(dur) }
                        .border(
                            if (isSel) 2.dp else 1.dp,
                            if (isSel) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = dur.displayName.replace("Calada ", ""),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dur.secondsRange,
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSecondary
                        )
                        Text(
                            text = dur.description.split("/").first().trim(),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 9.sp,
                            maxLines = 2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Informative dialog explaining the dried cannabis flower equivalence scale
@Composable
fun EquivalenceScaleDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Escala de Equivalencias Oficial",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Estándar de Flor Seca (~15% Cannabinoides)",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Marco de Referencia:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Para permitir que el médico controle con precisión el cupo diario prescrito (habitualmente indicado en gramos de flores secas), CannaMed utiliza la escala promedio internacional basada en el Schedule 3 del Cannabis Act de Health Canada y farmacocinética clínica.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    EquivalenceInfoRow(
                        title = "1. Clear Pods / Concentrados (Cartuchos 510)",
                        detail = "• Calada Corta (1-2s): ~0.035 g flor seca\n• Calada Estándar (3-4s): ~0.060 g flor seca\n• Calada Larga (5s+): ~0.110 g flor seca\n(Factor base: 1g extracto = 4g a 5g de flor seca)"
                    )

                    EquivalenceInfoRow(
                        title = "2. Gotas Sublinguales / Aceite",
                        detail = "• 1 gota (0.05 ml): ~0.010 g flor seca\n• 10 gotas (0.5 ml): ~0.100 g flor seca\n• 20 gotas (1.0 ml): ~0.200 g flor seca"
                    )

                    EquivalenceInfoRow(
                        title = "3. Edibles / Ingestión Oral",
                        detail = "• 10 mg cannabinoides comible: ~0.070 g flor seca\n• 10 mg bebible / infusión: ~0.080 g flor seca"
                    )

                    EquivalenceInfoRow(
                        title = "4. Vaporizador Herbal de Flor",
                        detail = "• Calada Corta: ~0.015 g flor seca\n• Calada Estándar: ~0.025 g flor seca\n• Calada Larga: ~0.040 g flor seca\n• Cámara pesada: 1:1 en gramos directos"
                    )

                    EquivalenceInfoRow(
                        title = "5. Pirolítico (Porro, Pipa, Bong)",
                        detail = "• Pesaje en gramos: 1:1 en gramos directos (formato 0.00g)\n• Calada corta/estándar/larga: 0.020g / 0.035g / 0.055g"
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(14.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Entendido")
                    }
                }
            }
        }
    }
}

@Composable
fun EquivalenceInfoRow(title: String, detail: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
