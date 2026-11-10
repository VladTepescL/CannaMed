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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CannabisFormat
import com.example.data.model.DosageLog
import com.example.ui.DailyGramsProgress
import com.example.ui.QuickStats
import com.example.ui.components.D3DosageEvolutionChart
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.StrainHybridColor
import com.example.ui.theme.StrainIndicaColor
import com.example.ui.theme.StrainSativaColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DosageJournalScreen(
    logs: List<DosageLog>,
    quickStats: QuickStats,
    dailyGramsProgress: DailyGramsProgress,
    onAddLogClick: () -> Unit,
    onDeleteLog: (DosageLog) -> Unit
) {
    var selectedFilterIndex by remember { mutableStateOf(0) }
    var logToDelete by remember { mutableStateOf<DosageLog?>(null) }

    val filterOptions = listOf(
        "Todos",
        "Vaporizado",
        "Gotas / Tintura",
        "Cartucho / Pod",
        "Aceite Clear",
        "Comestibles",
        "Fumado"
    )

    val filteredLogs = remember(logs, selectedFilterIndex) {
        when (selectedFilterIndex) {
            0 -> logs
            1 -> logs.filter { it.format == CannabisFormat.VAPORIZED_FLOWER }
            2 -> logs.filter { it.format == CannabisFormat.DROPS_TINCTURE }
            3 -> logs.filter { it.format == CannabisFormat.CONCENTRATE_VAPE }
            4 -> logs.filter { it.format == CannabisFormat.CLEAR_OIL }
            5 -> logs.filter { it.format == CannabisFormat.EDIBLE || it.format == CannabisFormat.CAPSULES }
            6 -> logs.filter {
                it.format == CannabisFormat.SMOKED_PIPE ||
                        it.format == CannabisFormat.SMOKED_JOINT ||
                        it.format == CannabisFormat.SMOKED_BONG ||
                        it.format == CannabisFormat.DABBING
            }
            else -> logs
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                // Top Header Summary Cards (First card shows Gramos Restantes en formato retroceso)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Gramos Restantes",
                        value = "${String.format(Locale.US, "%.2f", dailyGramsProgress.remainingGrams)}g",
                        subtitle = "de ${String.format(Locale.US, "%.2f", dailyGramsProgress.prescribedGrams)}g hoy",
                        accentColor = EmeraldPrimary
                    )
                    SummaryMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Alivio Promedio",
                        value = if (quickStats.averageReliefToday > 0) String.format(Locale.US, "%.1f", quickStats.averageReliefToday) else "-",
                        subtitle = "Escala de 1 a 10",
                        accentColor = EmeraldSecondary
                    )
                    SummaryMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Preferido",
                        value = quickStats.mostUsedFormat.split(" ").firstOrNull() ?: "Vape",
                        subtitle = "${quickStats.puffsToday} caladas hoy",
                        accentColor = EmeraldTertiary
                    )
                }
            }

            // D3 & Compose Dosage Evolution & History Chart
            item {
                D3DosageEvolutionChart(logs = logs)
            }

            // Simulated AdMob Banner in history view (hidden if isPremium)
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                val secPrefs = remember { com.example.util.SecurityPreferences(context) }
                com.example.ui.components.SimulatedAdMobBanner(isPremium = secPrefs.isPremium)
            }

            // Filter Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedFilterIndex,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    contentColor = EmeraldPrimary,
                    divider = {}
                ) {
                    filterOptions.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedFilterIndex == index,
                            onClick = { selectedFilterIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedFilterIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedFilterIndex == index) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }

            // Timeline Items
            if (filteredLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                tint = EmeraldSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No hay tomas en esta categoría",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Usa el botón flotante '+' o la pestaña 'Toma' para registrar una nueva dosis.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredLogs, key = { it.id }) { log ->
                    DosageLogCard(
                        log = log,
                        onDelete = { logToDelete = log }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddLogClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_dosage"),
            containerColor = EmeraldPrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Registrar Dosis", modifier = Modifier.size(28.dp))
        }
    }

    // Delete Confirmation Dialog
    logToDelete?.let { log ->
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("Eliminar Registro de Toma") },
            text = { Text("¿Deseas eliminar este registro del historial médico? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteLog(log)
                        logToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SummaryMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DosageLogCard(
    log: DosageLog,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault()) }
    val dateString = remember(log.timestamp) { dateFormat.format(Date(log.timestamp)) }

    val strainColor = when {
        log.strainType.contains("Indica", ignoreCase = true) -> StrainIndicaColor
        log.strainType.contains("Sativa", ignoreCase = true) -> StrainSativaColor
        else -> StrainHybridColor
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
            // Top Row: Time, Format & Delete Button
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            log.format.icon,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = log.format.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Relief score badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (log.reliefScore >= 7) EmeraldPrimary.copy(alpha = 0.2f)
                                else EmeraldTertiary.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Alivio ${log.reliefScore}/10",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (log.reliefScore >= 7) EmeraldPrimary else EmeraldTertiary
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Strain & Dosage Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(strainColor)
                    )
                    Text(
                        text = log.strainName.ifBlank { "Variedad General" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "(${log.strainType})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Dosage badge with puff intensity & equivalent dried flower
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val doseText = if (log.puffsCount > 0) {
                        val intensityShort = log.puffIntensity.replace("Calada ", "")
                        "${log.puffsCount} caladas ($intensityShort)"
                    } else {
                        "${log.dosageValue} ${log.dosageUnit}"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = doseText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSecondary
                        )
                    }
                    if (log.equivalentGramsDriedFlower > 0.0) {
                        Text(
                            text = "≈ ${String.format(Locale.US, "%.3f", log.equivalentGramsDriedFlower)}g flor",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Temperature or Cannabinoid info if present
            if (log.temperatureCelsius != null || log.thcPercentage > 0f) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (log.temperatureCelsius != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = EmeraldTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${log.temperatureCelsius} °C",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldTertiary
                            )
                        }
                    }
                    if (log.thcPercentage > 0f || log.cbdPercentage > 0f) {
                        Text(
                            text = "THC ${log.thcPercentage}% • CBD ${log.cbdPercentage}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Symptoms treated chips
            if (log.treatedSymptoms.isNotBlank()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    log.treatedSymptoms.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { sym ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "• $sym",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Subjective Notes
            if (log.subjectiveNotes.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "\"${log.subjectiveNotes}\"",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Mood & Side-effects footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (log.moodEffect.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Mood,
                            contentDescription = null,
                            tint = EmeraldSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = log.moodEffect,
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldSecondary
                        )
                    }
                }
                if (log.sideEffects != "Ninguno" && log.sideEffects.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = EmeraldTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = log.sideEffects,
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldTertiary
                        )
                    }
                }
            }
        }
    }
}
