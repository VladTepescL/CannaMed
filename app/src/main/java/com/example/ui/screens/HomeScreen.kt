package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CannabisFormat
import com.example.data.model.Cartridge
import com.example.data.model.DosageLog
import com.example.data.model.DoseEquivalenceCalculator
import com.example.data.model.KnownTerpenes
import com.example.data.model.PuffDuration
import com.example.data.model.StrainInfo
import com.example.data.model.TerpeneEntry
import com.example.ui.DailyGramsProgress
import com.example.ui.QuickStats
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.StrainHybridColor
import com.example.ui.theme.StrainIndicaColor
import com.example.ui.theme.StrainSativaColor
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class QuickDosePreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val format: CannabisFormat,
    val dosageValue: Double,
    val dosageUnit: String,
    val puffsCount: Int,
    val puffIntensity: String,
    val equivalentGrams: Double,
    val icon: ImageVector,
    val badge: String
)

@Composable
fun HomeScreen(
    latestLog: DosageLog?,
    activeCartridge: Cartridge?,
    quickStats: QuickStats,
    dailyGramsProgress: DailyGramsProgress,
    onStartDoseWizard: () -> Unit,
    onOpenEmergencyRecorder: () -> Unit = {},
    allLogs: List<DosageLog> = emptyList(),
    activeCartridges: List<Cartridge> = emptyList(),
    strains: List<StrainInfo> = emptyList(),
    onQuickLogDose: (DosageLog) -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedViewMode by rememberSaveable { mutableIntStateOf(0) } // 0: Seguimiento, 1: Biblioteca
    var selectedPresetForConfirm by remember { mutableStateOf<QuickDosePreset?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Filter today's logs from allLogs for the daily summary
    val todayLogs = remember(allLogs) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis
        allLogs.filter { it.timestamp >= startOfToday }
    }

    // Dynamic Quick Presets (adapts to active cartridge if available)
    val quickPresets = remember(activeCartridge) {
        listOf(
            QuickDosePreset(
                id = "vape_flower_2",
                title = "Vape Flor Herbal",
                subtitle = "2 caladas estándar",
                format = CannabisFormat.VAPORIZED_FLOWER,
                dosageValue = 2.0,
                dosageUnit = "Caladas",
                puffsCount = 2,
                puffIntensity = "Estándar (3-4s)",
                equivalentGrams = 0.05,
                icon = Icons.Default.Air,
                badge = "0.05g"
            ),
            QuickDosePreset(
                id = "pod_vape_1",
                title = activeCartridge?.brandName ?: "Clear Pod Activo",
                subtitle = if (activeCartridge != null) "1 calada (${activeCartridge.varietyName})" else "1 calada concentrado",
                format = CannabisFormat.CONCENTRATE_VAPE,
                dosageValue = 1.0,
                dosageUnit = "Caladas",
                puffsCount = 1,
                puffIntensity = "Estándar (3-4s)",
                equivalentGrams = 0.06,
                icon = Icons.Default.ElectricBolt,
                badge = "0.06g"
            ),
            QuickDosePreset(
                id = "tincture_drops_3",
                title = "Gotas Sublinguales",
                subtitle = "3 gotas tintura oral",
                format = CannabisFormat.DROPS_TINCTURE,
                dosageValue = 3.0,
                dosageUnit = "Gotas",
                puffsCount = 0,
                puffIntensity = "No aplica",
                equivalentGrams = 0.03,
                icon = Icons.Default.WaterDrop,
                badge = "0.03g"
            ),
            QuickDosePreset(
                id = "tincture_drops_5",
                title = "Gotas Sublinguales",
                subtitle = "5 gotas tintura oral",
                format = CannabisFormat.DROPS_TINCTURE,
                dosageValue = 5.0,
                dosageUnit = "Gotas",
                puffsCount = 0,
                puffIntensity = "No aplica",
                equivalentGrams = 0.05,
                icon = Icons.Default.WaterDrop,
                badge = "0.05g"
            ),
            QuickDosePreset(
                id = "microdose_rescue",
                title = "Calada Corta Rescate",
                subtitle = "Microdosis rápida (1-2s)",
                format = CannabisFormat.VAPORIZED_FLOWER,
                dosageValue = 1.0,
                dosageUnit = "Caladas",
                puffsCount = 1,
                puffIntensity = "Corta (1-2s)",
                equivalentGrams = 0.02,
                icon = Icons.Default.Speed,
                badge = "0.02g"
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // TOP APP HEADER & EMERGENCY SHORTCUT
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CANNA MED • CONTROL CLÍNICO",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (selectedViewMode == 0) "Panel de Seguimiento" else "Biblioteca Terapéutica",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Emergency recording button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2E1313))
                            .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .clickable { onOpenEmergencyRecorder() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("home_emergency_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF5252))
                            )
                            Text(
                                text = "S.O.S. Grabar",
                                color = Color(0xFFFF8A80),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // PRIMARY VIEW MODE SELECTOR (SEGUIMIENTO vs BIBLIOTECA)
                TabRow(
                    selectedTabIndex = selectedViewMode,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = EmeraldPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .testTag("home_view_mode_selector")
                ) {
                    Tab(
                        selected = selectedViewMode == 0,
                        onClick = { selectedViewMode = 0 },
                        modifier = Modifier.testTag("tab_seguimiento"),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Analytics,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedViewMode == 0) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Seguimiento Diario",
                                    fontWeight = if (selectedViewMode == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedViewMode == 1,
                        onClick = { selectedViewMode = 1 },
                        modifier = Modifier.testTag("tab_biblioteca"),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedViewMode == 1) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Biblioteca Médica",
                                    fontWeight = if (selectedViewMode == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    )
                }
            }
        }

        // VIEW BODY BASED ON SELECTOR
        if (selectedViewMode == 0) {
            TrackingViewContent(
                latestLog = latestLog,
                todayLogs = todayLogs,
                activeCartridge = activeCartridge,
                quickStats = quickStats,
                dailyGramsProgress = dailyGramsProgress,
                pulseScale = pulseScale,
                quickPresets = quickPresets,
                onPresetClick = { selectedPresetForConfirm = it },
                onStartDoseWizard = onStartDoseWizard,
                onNavigateToTab = onNavigateToTab
            )
        } else {
            LibraryViewContent(
                strains = strains,
                onNavigateToTab = onNavigateToTab
            )
        }
    }

    // Quick Dose Confirmation Dialog
    selectedPresetForConfirm?.let { preset ->
        QuickDoseConfirmDialog(
            preset = preset,
            activeCartridge = activeCartridge,
            onDismiss = { selectedPresetForConfirm = null },
            onConfirm = { reliefScore, notes ->
                val calculatedGrams = preset.equivalentGrams
                val newLog = DosageLog(
                    timestamp = System.currentTimeMillis(),
                    format = preset.format,
                    strainName = activeCartridge?.varietyName ?: latestLog?.strainName ?: "Cepa Prescrita",
                    strainType = activeCartridge?.varietyName?.let {
                        if (it.contains("Indica", true)) "Indica"
                        else if (it.contains("Sativa", true)) "Sativa"
                        else "Híbrida"
                    } ?: "Híbrida",
                    dosageValue = preset.dosageValue,
                    dosageUnit = preset.dosageUnit,
                    puffsCount = preset.puffsCount,
                    puffIntensity = preset.puffIntensity,
                    equivalentGramsDriedFlower = calculatedGrams,
                    cartridgeId = if (preset.format == CannabisFormat.CONCENTRATE_VAPE) activeCartridge?.id else null,
                    reliefScore = reliefScore,
                    treatedSymptoms = "Dosis rápida de control terapéutico",
                    subjectiveNotes = notes
                )
                onQuickLogDose(newLog)
                selectedPresetForConfirm = null
                Toast.makeText(
                    context,
                    "¡Dosis registrada! -${String.format(Locale.US, "%.2f", calculatedGrams)}g flor equivalente",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

// =========================================================================
// 1. SEGUIMIENTO VIEW (Tracking & Daily Consumption Summary + Quick Actions)
// =========================================================================

@Composable
private fun TrackingViewContent(
    latestLog: DosageLog?,
    todayLogs: List<DosageLog>,
    activeCartridge: Cartridge?,
    quickStats: QuickStats,
    dailyGramsProgress: DailyGramsProgress,
    pulseScale: Float,
    quickPresets: List<QuickDosePreset>,
    onPresetClick: (QuickDosePreset) -> Unit,
    onStartDoseWizard: () -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
        }

        // SECTION: RESUMEN DEL CONSUMO DIARIO (COUNTDOWN PROGRESS GAUGE)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_grams_countdown_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    EmeraldPrimary.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Scale,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Resumen de Consumo Diario",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Flor seca equivalente según prescripción médica",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (dailyGramsProgress.remainingGrams > 0.5) EmeraldPrimary.copy(alpha = 0.2f)
                                    else EmeraldTertiary.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.2f", dailyGramsProgress.remainingGrams)}g libres",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (dailyGramsProgress.remainingGrams > 0.5) EmeraldPrimary else EmeraldTertiary
                            )
                        }
                    }

                    // Progress bar showing consumed vs prescribed
                    val progressFraction = remember(dailyGramsProgress) {
                        (dailyGramsProgress.consumedGrams / dailyGramsProgress.prescribedGrams.coerceAtLeast(0.1))
                            .toFloat()
                            .coerceIn(0f, 1f)
                    }

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (progressFraction > 0.9f) EmeraldTertiary else EmeraldPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainer
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Consumido: ${String.format(Locale.US, "%.2f", dailyGramsProgress.consumedGrams)}g / ${String.format(Locale.US, "%.2f", dailyGramsProgress.prescribedGrams)}g",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(progressFraction * 100).toInt()}% de la cuota",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (progressFraction > 0.9f) EmeraldTertiary else EmeraldSecondary
                        )
                    }
                }
            }
        }

        // 4 KPI CARDS: DOSES, PUFFS, AVERAGE RELIEF, FORMAT
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Tomas Hoy",
                    value = "${quickStats.dosesToday}",
                    subtitle = "Registros",
                    icon = Icons.Default.MedicalServices,
                    tint = EmeraldPrimary
                )
                MetricKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Caladas",
                    value = "${quickStats.puffsToday}",
                    subtitle = "Acumuladas",
                    icon = Icons.Default.Speed,
                    tint = EmeraldTertiary
                )
                MetricKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Alivio",
                    value = if (quickStats.averageReliefToday > 0) String.format(Locale.US, "%.1f", quickStats.averageReliefToday) else "-",
                    subtitle = "Escala 1-10",
                    icon = Icons.Default.CheckCircle,
                    tint = EmeraldSecondary
                )
                MetricKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Formato",
                    value = quickStats.mostUsedFormat.split(" ").firstOrNull() ?: "Vape",
                    subtitle = "Predilecto",
                    icon = Icons.Default.Spa,
                    tint = EmeraldPrimary
                )
            }
        }

        // SECTION: ACCESOS RÁPIDOS PARA REGISTRAR NUEVAS DOSIS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Accesos Rápidos de Dosificación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Toca para registrar tomas frecuentes en 1 toque",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Rápido",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickPresets, key = { it.id }) { preset ->
                        QuickPresetChipCard(
                            preset = preset,
                            onClick = { onPresetClick(preset) }
                        )
                    }
                }
            }
        }

        // GIANT BUTTON: REGISTRO GUIADO COMPLETO
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartDoseWizard() }
                    .testTag("giant_dose_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(EmeraldPrimary, Color(0xFF059669))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Registro Guiado Completo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Abre el asistente paso a paso con temperatura, cepa personalizada, fotos y notas clínicas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldPrimary)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Iniciar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // LATEST INTAKE & ACTIVE POD STATUS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Latest Intake
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                            Text("Última Toma", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (latestLog != null) {
                            Text(
                                text = latestLog.strainName.ifBlank { latestLog.format.displayName },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${dateFormat.format(Date(latestLog.timestamp))} • Alivio ${latestLog.reliefScore}/10",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldSecondary,
                                fontSize = 11.sp
                            )
                        } else {
                            Text("Sin tomas previas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Active Pod (if any)
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = EmeraldTertiary, modifier = Modifier.size(16.dp))
                            Text("Pod en Uso", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (activeCartridge != null) {
                            Text(
                                text = activeCartridge.varietyName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${activeCartridge.totalPuffsLogged} caladas acumuladas",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldTertiary,
                                fontSize = 11.sp
                            )
                        } else {
                            Text("Sin pod abierto", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // RECENT TODAY'S DOSES LIST
        if (todayLogs.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tomas de Hoy (${todayLogs.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ver diario completo",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToTab(1) }
                    )
                }
            }

            items(todayLogs.take(3), key = { it.id }) { log ->
                TodayDoseRowCard(log = log, dateFormat = dateFormat)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun QuickPresetChipCard(
    preset: QuickDosePreset,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, EmeraldPrimary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("quick_preset_${preset.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(preset.icon, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(preset.badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(preset.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(preset.subtitle, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text("+ Registrar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
            }
        }
    }
}

@Composable
private fun TodayDoseRowCard(
    log: DosageLog,
    dateFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(log.format.icon, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                }
                Column {
                    Text(
                        text = "${log.format.displayName} (${log.strainName.ifBlank { "Cepa Prescrita" }})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${dateFormat.format(Date(log.timestamp))} • ≈${String.format(Locale.US, "%.2f", log.equivalentGramsDriedFlower)}g flor",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmeraldSecondary.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Alivio ${log.reliefScore}/10",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldSecondary
                )
            }
        }
    }
}

// Quick confirmation modal before committing quick dose
@Composable
private fun QuickDoseConfirmDialog(
    preset: QuickDosePreset,
    activeCartridge: Cartridge?,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var reliefScore by remember { mutableIntStateOf(8) }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            Icon(preset.icon, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Confirmar Dosis Rápida", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(preset.title, style = MaterialTheme.typography.bodySmall, color = EmeraldSecondary)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Detalles de la toma:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text("• Formato: ${preset.format.displayName}", fontSize = 12.sp)
                        Text("• Dosis: ${preset.subtitle}", fontSize = 12.sp)
                        Text("• Equivalencia: -${String.format(Locale.US, "%.2f", preset.equivalentGrams)}g de flor seca", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                        if (preset.format == CannabisFormat.CONCENTRATE_VAPE && activeCartridge != null) {
                            Text("• Cartucho: ${activeCartridge.brandName} (${activeCartridge.varietyName})", fontSize = 12.sp, color = EmeraldTertiary)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Alivio Sintomático Obtenido:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("$reliefScore / 10", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    }
                    Slider(
                        value = reliefScore.toFloat(),
                        onValueChange = { reliefScore = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Nota breve opcional (ej: dolor lumbar)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { onConfirm(reliefScore, notes) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. BIBLIOTECA VIEW (Strains, Terpenes, Temperatures, Equivalences)
// =========================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LibraryViewContent(
    strains: List<StrainInfo>,
    onNavigateToTab: (Int) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableIntStateOf(0) } // 0: Todas, 1: Cepas, 2: Terpenos, 3: Temperaturas, 4: Equivalencias

    val categories = listOf("Todas", "🌿 Cepas", "🧪 Terpenos", "🌡️ Temperaturas", "⚖️ Equivalencias")

    // Filtered strains
    val filteredStrains = remember(strains, searchQuery) {
        val baseList = if (strains.isNotEmpty()) strains else getFallbackStrains()
        if (searchQuery.isBlank()) baseList
        else baseList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.type.contains(searchQuery, ignoreCase = true) ||
                    it.medicalBenefits.contains(searchQuery, ignoreCase = true) ||
                    it.dominantTerpenes.contains(searchQuery, ignoreCase = true)
        }
    }

    // Filtered terpenes
    val filteredTerpenes = remember(searchQuery) {
        if (searchQuery.isBlank()) KnownTerpenes.ALL
        else KnownTerpenes.ALL.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.aroma.contains(searchQuery, ignoreCase = true) ||
                    it.therapeuticBenefit.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
        }

        // SEARCH BAR
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar cepa, terpeno, dolor, insomnio...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_search_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary),
                singleLine = true
            )
        }

        // CATEGORY FILTER CHIPS
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories.indices.toList()) { index ->
                    FilterChip(
                        selected = selectedCategoryFilter == index,
                        onClick = { selectedCategoryFilter = index },
                        label = { Text(categories[index]) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // 1. CEPAS MEDICINALES
        if (selectedCategoryFilter == 0 || selectedCategoryFilter == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cepas Medicinales (${filteredStrains.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "Ver catálogo Leafly ↗",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToTab(3) }
                    )
                }
            }

            items(filteredStrains.take(4), key = { it.name }) { strain ->
                LibraryStrainCard(strain = strain, context = context)
            }
        }

        // 2. ENCICLOPEDIA DE TERPENOS
        if (selectedCategoryFilter == 0 || selectedCategoryFilter == 2) {
            item {
                Text(
                    text = "Enciclopedia de Terpenos Medicinales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            }

            items(filteredTerpenes, key = { it.name }) { terpene ->
                LibraryTerpeneCard(terpene = terpene)
            }
        }

        // 3. GUÍA DE TEMPERATURAS DE VAPORIZACIÓN
        if (selectedCategoryFilter == 0 || selectedCategoryFilter == 3) {
            item {
                Text(
                    text = "Guía de Temperaturas de Vaporización",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            }

            item {
                VaporizationTemperatureGuideCard()
            }
        }

        // 4. TABLA DE EQUIVALENCIAS DE FARMACOPEA
        if (selectedCategoryFilter == 0 || selectedCategoryFilter == 4) {
            item {
                Text(
                    text = "Equivalencias Farmacopea (Health Canada)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            }

            item {
                PharmacopeiaEquivalenceCard()
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LibraryStrainCard(strain: StrainInfo, context: Context) {
    val typeColor = when {
        strain.type.contains("Indica", true) -> StrainIndicaColor
        strain.type.contains("Sativa", true) -> StrainSativaColor
        strain.type.contains("Alto CBD", true) -> EmeraldSecondary
        else -> StrainHybridColor
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strain.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(typeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(strain.type, color = typeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(
                    onClick = {
                        try {
                            val slug = strain.leaflySlug.ifBlank { strain.name.lowercase().trim().replace(" ", "-") }
                            val url = "https://www.leafly.com/strains/$slug"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                addCategory(Intent.CATEGORY_BROWSABLE)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = "Ver Perfil Botánico", tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("THC: ${strain.thcRange}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = EmeraldTertiary)
                Text("CBD: ${strain.cbdRange}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = EmeraldPrimary)
            }

            Text("Beneficios: ${strain.medicalBenefits}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Terpenos: ${strain.dominantTerpenes}", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = EmeraldSecondary)
        }
    }
}

@Composable
private fun LibraryTerpeneCard(terpene: TerpeneEntry) {
    val boilingTemp = when (terpene.name) {
        "Mirceno" -> "168°C (334°F)"
        "Limoneno" -> "176°C (349°F)"
        "Beta-Cariofileno" -> "130°C - 160°C"
        "Alfa-Pineno" -> "155°C (311°F)"
        "Linalool" -> "198°C (388°F)"
        "Terpinoleno" -> "186°C (367°F)"
        "Humuleno" -> "198°C (388°F)"
        else -> "160°C - 200°C"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(terpene.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = terpene.name.take(2).uppercase(),
                    color = terpene.color,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(terpene.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(boilingTemp, fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                }
                Text("Efecto: ${terpene.therapeuticBenefit}", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Aroma: ${terpene.aroma}", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = terpene.color)
            }
        }
    }
}

@Composable
private fun VaporizationTemperatureGuideCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Thermostat, contentDescription = null, tint = EmeraldPrimary)
                Text("Escala Térmica de Inhalación Médica", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }

            TempRangeRow(
                range = "160°C - 180°C",
                title = "Baja Temperatura (Sabor & Lucidez)",
                detail = "Vaporización de Pineno y Mirceno. THC leve sin efecto sedante. Ideal para actividades diurnas."
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            TempRangeRow(
                range = "180°C - 200°C",
                title = "Temperatura Media (Rango Óptimo Terapéutico)",
                detail = "Extracción balanceada de CBD, THC y Limoneno. Máximo alivio analgésico y ansiolítico."
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            TempRangeRow(
                range = "200°C - 220°C",
                title = "Alta Temperatura (Sedación Intensiva)",
                detail = "Liberación de CBN, CBC y Linalool. Potente para insomnio, dolor crónico severo y espasmos."
            )
        }
    }
}

@Composable
private fun TempRangeRow(range: String, title: String, detail: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text(range, fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 11.sp)
        }
        Text(detail, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PharmacopeiaEquivalenceCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.LocalPharmacy, contentDescription = null, tint = EmeraldSecondary)
                Text("Factores de Equivalencia Farmacopea", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }

            Text(
                text = "Basado en Health Canada Cannabis Act (Schedule 3) para control de cuota diaria:",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            EquivalenceRow("1g Flor Seca", "≈ 0.25g Concentrado sólido / Vape")
            EquivalenceRow("1g Flor Seca", "≈ 5 mL Aceite oral (100 gotas)")
            EquivalenceRow("1 Toma Vaporizador", "≈ 2 caladas estándar = 0.05g flor")
            EquivalenceRow("1 Gota Sublingual", "≈ 0.010g flor seca equivalente")
            EquivalenceRow("10 mg Comestible", "≈ 0.070g flor seca equivalente")
        }
    }
}

@Composable
private fun EquivalenceRow(left: String, right: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(left, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrimary)
        Text(right, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun getFallbackStrains(): List<StrainInfo> {
    return listOf(
        StrainInfo(
            name = "Charlotte's Web",
            type = "Alto CBD",
            thcRange = "< 1%",
            cbdRange = "15-20%",
            dominantTerpenes = "Mirceno, Cariofileno, Bisabolol",
            medicalBenefits = "Epilepsia, dolor neuropático, inflamación",
            flavorAroma = "Pino, terroso, floral",
            description = "Famosa variedad medicinal desarrollada para afecciones convulsivas y dolor crónico.",
            leaflySlug = "charlottes-web"
        ),
        StrainInfo(
            name = "Harlequin",
            type = "Alto CBD",
            thcRange = "4-7%",
            cbdRange = "8-16%",
            dominantTerpenes = "Mirceno, Alfa-Pineno, Cariofileno",
            medicalBenefits = "Dolor, ansiedad, fatiga, estrés",
            flavorAroma = "Mango, terroso, madera",
            description = "Ratio 5:2 CBD a THC, proporciona un alivio claro sin sedación excesiva.",
            leaflySlug = "harlequin"
        ),
        StrainInfo(
            name = "ACDC",
            type = "Alto CBD",
            thcRange = "1%",
            cbdRange = "14-20%",
            dominantTerpenes = "Mirceno, Pineno, Cariofileno",
            medicalBenefits = "Ansiedad severa, espasmos, dolor crónico",
            flavorAroma = "Cítrico, pino, terroso",
            description = "Ratio 20:1 CBD a THC, referencia mundial en analgesia limpia.",
            leaflySlug = "acdc"
        ),
        StrainInfo(
            name = "Granddaddy Purple",
            type = "Indica",
            thcRange = "17-23%",
            cbdRange = "< 1%",
            dominantTerpenes = "Mirceno, Cariofileno, Pineno",
            medicalBenefits = "Insomnio severo, dolor agudo, estrés",
            flavorAroma = "Uva, bayas dulces, terroso",
            description = "Cepa clásica para relajación nocturna profunda y supresión de dolor somático.",
            leaflySlug = "granddaddy-purple"
        ),
        StrainInfo(
            name = "Blue Dream",
            type = "Híbrida",
            thcRange = "17-24%",
            cbdRange = "< 1%",
            dominantTerpenes = "Mirceno, Pineno, Cariofileno",
            medicalBenefits = "Depresión, náuseas, dolor moderado",
            flavorAroma = "Arándanos dulces, hierbas",
            description = "Balance ideal entre relajación corporal y estimulación cerebral lúcida.",
            leaflySlug = "blue-dream"
        )
    )
}
