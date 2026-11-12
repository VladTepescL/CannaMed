package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPrintshop
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Cartridge
import com.example.data.model.DosageLog
import com.example.data.model.Prescription
import com.example.ui.components.D3DosageEvolutionChart
import com.example.ui.components.SimulatedAdMobBanner
import com.example.ui.components.SimulatedAdMobInterstitialDialog
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldSecondaryContainer
import com.example.ui.theme.EmeraldTertiary
import com.example.util.SecurityPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReportDetailTier(val label: String, val subtitle: String) {
    BASIC("Básico", "Datos del paciente, prescripciones y total de tomas"),
    INTERMEDIATE("Intermedio", "Básico + formatos y promedios de alivio"),
    ADVANCED("Avanzado", "Intermedio + Clear Pods, terpenos y notas"),
    COMPLETE("Completo", "Avanzado + gráficos vectoriales D3/SVG de los datos")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DoctorReportScreen(
    logs: List<DosageLog>,
    cartridges: List<Cartridge>,
    prescriptions: List<Prescription>,
    onGenerateReport: (String, String, Int, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean) -> String
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val secPrefs = remember { SecurityPreferences(context) }
    var showInterstitialAfterExport by remember { mutableStateOf(false) }

    var selectedTier by remember { mutableStateOf(ReportDetailTier.COMPLETE) }
    var patientName by remember { mutableStateOf("Paciente") }
    var doctorName by remember {
        mutableStateOf(prescriptions.firstOrNull()?.doctorName ?: "Dra. Sofía Mendoza")
    }

    // Time periods requested: 1, 3, 6, 12 meses
    var selectedMonthsPeriod by remember { mutableIntStateOf(3) }

    // Eligible data sections
    var incDoseSummary by remember { mutableStateOf(true) }
    var incReliefAndCharts by remember { mutableStateOf(true) }
    var incFormatsBreakdown by remember { mutableStateOf(true) }
    var incClearPodsAndTerpenes by remember { mutableStateOf(true) }
    var incSubjectiveNotes by remember { mutableStateOf(true) }
    var incPrescriptions by remember { mutableStateOf(true) }

    val periodCutoff = remember(selectedMonthsPeriod) {
        if (selectedMonthsPeriod > 0) {
            System.currentTimeMillis() - (selectedMonthsPeriod.toLong() * 30L * 24 * 60 * 60 * 1000)
        } else 0L
    }

    val periodLogs = remember(logs, periodCutoff) {
        logs.filter { it.timestamp >= periodCutoff }
    }

    val reportText = remember(
        patientName, doctorName, selectedMonthsPeriod,
        incDoseSummary, incReliefAndCharts, incFormatsBreakdown,
        incClearPodsAndTerpenes, incSubjectiveNotes, incPrescriptions,
        logs, cartridges, prescriptions
    ) {
        onGenerateReport(
            patientName,
            doctorName,
            selectedMonthsPeriod,
            incDoseSummary,
            incReliefAndCharts,
            incFormatsBreakdown,
            incClearPodsAndTerpenes,
            incSubjectiveNotes,
            incPrescriptions
        )
    }

    var showEquivalenceDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            // Title
            Column {
                Text(
                    text = "Informe Clínico al Médico",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configura el período de tiempo y selecciona los datos elegibles a exportar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Equivalence scale banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { showEquivalenceDialog = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Scale, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Escala de Equivalencias a Flor Seca", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Estándar clínico de conversión para control de dosis (Health Canada Schedule 3)", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.Default.Info, contentDescription = null, tint = EmeraldSecondary)
                }
            }
        }

        // Subtle simulated AdMob banner (hidden if isPremium)
        item {
            SimulatedAdMobBanner(isPremium = secPrefs.isPremium)
        }

        // 1. Configurable Report Tier (Básico, Intermedio, Avanzado, Completo)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = EmeraldPrimary)
                        Text(
                            text = "1. Nivel de Detalle del Informe:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ReportDetailTier.values().forEach { tier ->
                            val isSelected = selectedTier == tier
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedTier = tier
                                    when (tier) {
                                        ReportDetailTier.BASIC -> {
                                            incDoseSummary = true
                                            incReliefAndCharts = false
                                            incFormatsBreakdown = false
                                            incClearPodsAndTerpenes = false
                                            incSubjectiveNotes = false
                                            incPrescriptions = true
                                        }
                                        ReportDetailTier.INTERMEDIATE -> {
                                            incDoseSummary = true
                                            incReliefAndCharts = true
                                            incFormatsBreakdown = true
                                            incClearPodsAndTerpenes = false
                                            incSubjectiveNotes = false
                                            incPrescriptions = true
                                        }
                                        ReportDetailTier.ADVANCED -> {
                                            incDoseSummary = true
                                            incReliefAndCharts = true
                                            incFormatsBreakdown = true
                                            incClearPodsAndTerpenes = true
                                            incSubjectiveNotes = true
                                            incPrescriptions = true
                                        }
                                        ReportDetailTier.COMPLETE -> {
                                            incDoseSummary = true
                                            incReliefAndCharts = true
                                            incFormatsBreakdown = true
                                            incClearPodsAndTerpenes = true
                                            incSubjectiveNotes = true
                                            incPrescriptions = true
                                        }
                                    }
                                },
                                label = { Text(tier.label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Text(
                        text = selectedTier.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Time Period Selector Card (1, 3, 6, 12 meses)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = EmeraldPrimary)
                        Text(
                            text = "2. Período de Tiempo del Informe:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Pair(1, "1 Mes"),
                            Pair(3, "3 Meses"),
                            Pair(6, "6 Meses"),
                            Pair(12, "12 Meses (1 Año)"),
                            Pair(0, "Todo")
                        ).forEach { (months, label) ->
                            val isSelected = selectedMonthsPeriod == months
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMonthsPeriod = months },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                    Text(
                        text = "Tomas comprendidas en este período: ${periodLogs.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Eligible Data to Export Card (Checkboxes)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = EmeraldTertiary)
                        Text(
                            text = "2. Datos Elegibles a Exportar:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    EligibleItemRow("Resumen de tomas y dosificación", incDoseSummary) { incDoseSummary = it }
                    EligibleItemRow("Efectividad y alivio de síntomas (1-10)", incReliefAndCharts) { incReliefAndCharts = it }
                    EligibleItemRow("Desglose por formato de administración", incFormatsBreakdown) { incFormatsBreakdown = it }
                    EligibleItemRow("Control de Clear Pods, % cannabinoides y terpenos", incClearPodsAndTerpenes) { incClearPodsAndTerpenes = it }
                    EligibleItemRow("Notas subjetivas del paciente sobre el efecto", incSubjectiveNotes) { incSubjectiveNotes = it }
                    EligibleItemRow("Recetas médicas y vigencia de control", incPrescriptions) { incPrescriptions = it }
                }
            }
        }

        // Charts (Responsive to selected time period)
        if (incReliefAndCharts) {
            item {
                D3DosageEvolutionChart(logs = periodLogs)
            }

            item {
                WeeklyIntakeChartCard(logs = periodLogs)
            }

            item {
                ReliefByFormatCard(logs = periodLogs)
            }
        }

        // Patient & Doctor Name Input & Export Actions
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = EmeraldPrimary)
                        Text(
                            text = "Datos de Encabezado Médico",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = patientName,
                            onValueChange = { patientName = it },
                            label = { Text("Nombre del Paciente") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary)
                        )
                        OutlinedTextField(
                            value = doctorName,
                            onValueChange = { doctorName = it },
                            label = { Text("Médico Tratante") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary)
                        )
                    }

                    // Print & Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!secPrefs.isPremium) {
                                    showInterstitialAfterExport = true
                                }
                                printOrSavePdf(
                                    context = context,
                                    reportContent = reportText,
                                    patientName = patientName,
                                    logs = periodLogs,
                                    includeCharts = (selectedTier == ReportDetailTier.COMPLETE)
                                )
                            },
                            modifier = Modifier.weight(1f).testTag("print_report_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.LocalPrintshop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Imprimir / PDF")
                        }

                        Button(
                            onClick = {
                                if (!secPrefs.isPremium) {
                                    showInterstitialAfterExport = true
                                }
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Informe Clínico Cannabis ($patientName) - Nivel ${selectedTier.label}")
                                    putExtra(Intent.EXTRA_TEXT, reportText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Enviar Informe Médico vía (WhatsApp, Correo, etc.)"))
                            },
                            modifier = Modifier.weight(1f).testTag("share_report_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compartir")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (!secPrefs.isPremium) {
                                showInterstitialAfterExport = true
                            }
                            clipboardManager.setText(AnnotatedString(reportText))
                            Toast.makeText(context, "Informe copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar Texto Completo")
                    }

                    // Preview
                    Text(
                        text = "Vista previa del informe a exportar:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = reportText,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showEquivalenceDialog) {
        EquivalenceScaleDialog(onDismiss = { showEquivalenceDialog = false })
    }

    if (showInterstitialAfterExport && !secPrefs.isPremium) {
        SimulatedAdMobInterstitialDialog(
            isPremium = secPrefs.isPremium,
            actionContext = "Exportación de Informe Médico Finalizada",
            onDismiss = { showInterstitialAfterExport = false }
        )
    }
}

@Composable
fun EligibleItemRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
        )
    }
}


@Composable
fun WeeklyIntakeChartCard(logs: List<DosageLog>) {
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

    val dailyBuckets = remember(logs) {
        val days = mutableListOf<Pair<String, Int>>()
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val endOfDay = cal.timeInMillis

            val count = logs.count { it.timestamp in startOfDay..endOfDay }
            days.add(Pair(dayFormat.format(Date(startOfDay)), count))
        }
        days
    }

    val maxCount = (dailyBuckets.maxOfOrNull { it.second } ?: 1).coerceAtLeast(4)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tomas Diarias en el Período",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${logs.size} tomas totales",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldPrimary
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 8.dp)
            ) {
                val barWidth = size.width / (dailyBuckets.size * 2f)
                val spacing = size.width / dailyBuckets.size
                val chartHeight = size.height - 24.dp.toPx()

                dailyBuckets.forEachIndexed { index, pair ->
                    val x = index * spacing + spacing / 2f
                    val barHeight = (pair.second.toFloat() / maxCount) * chartHeight
                    val topY = chartHeight - barHeight

                    drawRoundRect(
                        color = Color(0xFF1E352C),
                        topLeft = Offset(x - barWidth / 2f, 0f),
                        size = Size(barWidth, chartHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    if (barHeight > 0) {
                        drawRoundRect(
                            color = EmeraldPrimary,
                            topLeft = Offset(x - barWidth / 2f, topY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dailyBuckets.forEach { pair ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${pair.second}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (pair.second > 0) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = pair.first,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReliefByFormatCard(logs: List<DosageLog>) {
    val formatStats = remember(logs) {
        logs.groupBy { it.format.displayName }
            .map { (format, list) ->
                val avg = list.map { it.reliefScore }.average()
                Triple(format, list.size, avg)
            }
            .sortedByDescending { it.third }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Efectividad de Alivio por Formato",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Promedio de alivio en escala 1-10 según método de administración",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (formatStats.isEmpty()) {
                Text(
                    text = "Sin registros suficientes para correlación.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                formatStats.forEach { (format, count, avg) ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$format ($count tomas)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", avg)} / 10",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (avg >= 7.0) EmeraldPrimary else EmeraldTertiary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (avg / 10.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (avg >= 7.0) EmeraldPrimary else EmeraldTertiary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    }
                }
            }
        }
    }
}

fun generateExportChartSvg(logs: List<DosageLog>): String {
    val dayFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    val shortDay = SimpleDateFormat("EEE", Locale.getDefault())

    val points = mutableListOf<Triple<String, Double, Double>>() // Date label, grams, relief
    for (i in 13 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -i)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis

        val dayLogs = logs.filter { it.timestamp in start..end }
        val grams = dayLogs.sumOf { it.equivalentGramsDriedFlower }
        val avgRelief = if (dayLogs.isNotEmpty()) dayLogs.map { it.reliefScore }.average() else 0.0
        val label = "${shortDay.format(Date(start))} ${dayFormat.format(Date(start))}"
        points.add(Triple(label, grams, avgRelief))
    }

    val maxGrams = (points.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)
    val width = 640
    val height = 220
    val padL = 40
    val padR = 25
    val padT = 25
    val padB = 40
    val chartW = width - padL - padR
    val chartH = height - padT - padB

    val barW = (chartW.toDouble() / points.size * 0.55).toInt()
    val svgBuilder = StringBuilder()
    svgBuilder.append("<svg width=\"100%\" height=\"220\" viewBox=\"0 0 $width $height\" xmlns=\"http://www.w3.org/2000/svg\" style=\"background:#FAFAFA; border:1px solid #E2E8F0; border-radius:8px; margin: 16px 0;\">")

    // Grid lines
    for (i in 0..4) {
        val y = padT + (chartH * i / 4)
        val gVal = String.format(Locale.US, "%.1f", maxGrams * (4 - i) / 4.0)
        svgBuilder.append("<line x1=\"$padL\" y1=\"$y\" x2=\"${width - padR}\" y2=\"$y\" stroke=\"#E2E8F0\" stroke-dasharray=\"3,3\" />")
        svgBuilder.append("<text x=\"${padL - 6}\" y=\"${y + 4}\" font-size=\"9\" fill=\"#64748B\" text-anchor=\"end\">${gVal}g</text>")
    }

    // Bars: Grams
    val reliefPoints = mutableListOf<Pair<Int, Int>>()
    points.forEachIndexed { idx, (label, grams, relief) ->
        val xCenter = padL + (idx * chartW / (points.size - 1).coerceAtLeast(1))
        val barH = (grams / maxGrams * chartH).toInt()
        val barY = padT + chartH - barH

        if (barH > 0) {
            svgBuilder.append("<rect x=\"${xCenter - barW / 2}\" y=\"$barY\" width=\"$barW\" height=\"$barH\" fill=\"#10B981\" rx=\"3\" opacity=\"0.85\" />")
        }

        // Relief coordinate (scaled 0-10 to chartH)
        val reliefY = padT + chartH - ((relief / 10.0) * chartH).toInt()
        reliefPoints.add(Pair(xCenter, reliefY))

        // X Axis labels (alternate or every 2)
        if (idx % 2 == 0 || idx == points.size - 1) {
            svgBuilder.append("<text x=\"$xCenter\" y=\"${height - 12}\" font-size=\"8.5\" fill=\"#475569\" text-anchor=\"middle\">$label</text>")
        }
    }

    // Relief Score Line & Dots
    if (reliefPoints.size > 1) {
        val pathD = StringBuilder("M ${reliefPoints[0].first} ${reliefPoints[0].second}")
        for (j in 1 until reliefPoints.size) {
            pathD.append(" L ${reliefPoints[j].first} ${reliefPoints[j].second}")
        }
        svgBuilder.append("<path d=\"$pathD\" fill=\"none\" stroke=\"#F59E0B\" stroke-width=\"2.2\" stroke-linecap=\"round\" stroke-linejoin=\"round\" />")
    }
    reliefPoints.forEachIndexed { idx, (rx, ry) ->
        val rVal = points[idx].third
        if (rVal > 0) {
            svgBuilder.append("<circle cx=\"$rx\" cy=\"$ry\" r=\"3.5\" fill=\"#F59E0B\" stroke=\"#FFFFFF\" stroke-width=\"1.5\" />")
        }
    }

    svgBuilder.append("</svg>")
    return svgBuilder.toString()
}

fun printOrSavePdf(
    context: Context,
    reportContent: String,
    patientName: String,
    logs: List<DosageLog> = emptyList(),
    includeCharts: Boolean = true
) {
    try {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager != null) {
            val webView = WebView(context)
            val chartSvg = if (includeCharts) generateExportChartSvg(logs) else ""

            val chartSectionHtml = if (includeCharts && chartSvg.isNotBlank()) {
                """
                    <div class="chart-box">
                        <div class="chart-title">Evolución Gráfica de Dosificación Diaria & Alivio Sintomático (Últimos 14 Días)</div>
                        $chartSvg
                        <div class="chart-legend">
                            <span><span class="legend-dot" style="background:#10B981;"></span> Ingesta Flores Secas Equivalentes (g/día)</span>
                            <span><span class="legend-dot" style="background:#F59E0B;"></span> Nivel de Alivio Sintomático (1-10)</span>
                        </div>
                    </div>
                """.trimIndent()
            } else ""

            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>Informe Médico Cannabis - $patientName</title>
                    <style>
                        body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding: 24px; color: #111; line-height: 1.5; }
                        h1 { color: #064E3B; font-size: 20px; border-bottom: 2px solid #10B981; padding-bottom: 8px; margin-bottom: 6px; }
                        .subtitle { font-size: 13px; color: #4B5563; margin-bottom: 16px; }
                        .chart-box { margin: 16px 0; background: #FFFFFF; border: 1px solid #CBD5E1; border-radius: 8px; padding: 14px; }
                        .chart-title { font-size: 13px; font-weight: bold; color: #0F172A; margin-bottom: 6px; }
                        .chart-legend { display: flex; gap: 16px; font-size: 11px; color: #475569; margin-top: 6px; }
                        .legend-dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 4px; vertical-align: middle; }
                        pre { background: #F8FAFC; border: 1px solid #E2E8F0; padding: 16px; border-radius: 8px; font-size: 12px; white-space: pre-wrap; word-wrap: break-word; font-family: monospace; }
                        .footer { margin-top: 30px; font-size: 11px; color: #64748B; border-top: 1px solid #E2E8F0; padding-top: 10px; }
                    </style>
                </head>
                <body>
                    <h1>CannaMed - Informe de Seguimiento de Cannabis Medicinal</h1>
                    <div class="subtitle">Bitácora Farmacológica y Evolución Clínica • Paciente: <strong>$patientName</strong></div>

                    $chartSectionHtml

                    <pre>${reportContent.replace("<", "&lt;").replace(">", "&gt;")}</pre>
                    <div class="footer">Generado por CannaMed • Almacenamiento Local Privado Sandbox • Conforme Ley N° 20.000 Chile • Confidencial</div>
                </body>
                </html>
            """.trimIndent()

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printAdapter = webView.createPrintDocumentAdapter("Informe_Cannabis_$patientName")
                    val jobName = "CannaMed_Informe_$patientName"
                    printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al abrir la impresora/PDF", Toast.LENGTH_SHORT).show()
    }
}

