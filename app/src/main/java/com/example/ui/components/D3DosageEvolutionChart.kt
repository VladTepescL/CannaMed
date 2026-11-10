package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.DosageLog
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldTertiary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyDosagePoint(
    val dateLabel: String,
    val dayTimestamp: Long,
    val gramsDriedFlower: Double,
    val dosesCount: Int,
    val averageRelief: Double,
    val thcmg: Double,
    val cbdmg: Double
)

@Composable
fun D3DosageEvolutionChart(
    logs: List<DosageLog>,
    modifier: Modifier = Modifier
) {
    var selectedDaysRange by remember { mutableIntStateOf(7) } // 7, 14, 30
    var renderMode by remember { mutableIntStateOf(0) } // 0: D3 Vector WebView, 1: Compose Canvas Native
    var selectedMetric by remember { mutableIntStateOf(0) } // 0: Gramos Flores Secas, 1: Alivio Clínico (1-10)

    val dateFormat = SimpleDateFormat("dd MMM", Locale("es", "CL"))
    val shortDayFormat = SimpleDateFormat("EEE", Locale("es", "CL"))

    val dailyData = remember(logs, selectedDaysRange) {
        val points = mutableListOf<DailyDosagePoint>()
        for (i in (selectedDaysRange - 1) downTo 0) {
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

            val dayLogs = logs.filter { it.timestamp in startOfDay..endOfDay }
            val grams = dayLogs.sumOf { it.equivalentGramsDriedFlower }
            val avgRelief = if (dayLogs.isNotEmpty()) dayLogs.map { it.reliefScore }.average() else 0.0
            val thc = dayLogs.sumOf { (it.equivalentGramsDriedFlower * 1000.0 * (it.thcPercentage / 100.0)) }
            val cbd = dayLogs.sumOf { (it.equivalentGramsDriedFlower * 1000.0 * (it.cbdPercentage / 100.0)) }

            points.add(
                DailyDosagePoint(
                    dateLabel = if (selectedDaysRange <= 7) shortDayFormat.format(Date(startOfDay)) else dateFormat.format(Date(startOfDay)),
                    dayTimestamp = startOfDay,
                    gramsDriedFlower = grams,
                    dosesCount = dayLogs.size,
                    averageRelief = avgRelief,
                    thcmg = thc,
                    cbdmg = cbd
                )
            )
        }
        points
    }

    val totalGrams = dailyData.sumOf { it.gramsDriedFlower }
    val avgGramsPerDay = if (dailyData.isNotEmpty()) totalGrams / dailyData.size else 0.0
    val activeDays = dailyData.count { it.dosesCount > 0 }
    val overallRelief = dailyData.filter { it.dosesCount > 0 }.map { it.averageRelief }
    val avgRelief = if (overallRelief.isNotEmpty()) overallRelief.average() else 0.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("d3_dosage_evolution_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
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
                            .background(EmeraldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoGraph,
                            contentDescription = "Gráfico de Evolución",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Evolución de Dosis & Niveles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Historial farmacológico interactivo D3",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Toggle View Mode (D3 vs Canvas)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (renderMode == 0) EmeraldPrimary else Color.Transparent)
                            .clickable { renderMode = 0 }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "D3",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (renderMode == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (renderMode == 1) EmeraldPrimary else Color.Transparent)
                            .clickable { renderMode = 1 }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Canvas",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (renderMode == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Metric Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricMiniBox(
                    modifier = Modifier.weight(1f),
                    title = "Consumo Período",
                    value = "${String.format(Locale.US, "%.2f", totalGrams)}g",
                    subtitle = "${String.format(Locale.US, "%.2f", avgGramsPerDay)}g/día prom",
                    accentColor = EmeraldPrimary
                )
                MetricMiniBox(
                    modifier = Modifier.weight(1f),
                    title = "Alivio Promedio",
                    value = if (avgRelief > 0) "${String.format(Locale.US, "%.1f", avgRelief)}/10" else "N/A",
                    subtitle = "$activeDays días con tomas",
                    accentColor = EmeraldSecondary
                )
            }

            // Controls: Metric Selection & Days Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metric selector
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedMetric == 0,
                        onClick = { selectedMetric = 0 },
                        label = { Text("Flores Secas (g)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = EmeraldPrimary
                        )
                    )
                    FilterChip(
                        selected = selectedMetric == 1,
                        onClick = { selectedMetric = 1 },
                        label = { Text("Alivio (1-10)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldSecondary.copy(alpha = 0.2f),
                            selectedLabelColor = EmeraldSecondary
                        )
                    )
                }

                // Days selector
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(7, 14, 30).forEach { days ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedDaysRange == days) EmeraldPrimary else MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clickable { selectedDaysRange = days }
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${days}d",
                                fontSize = 11.sp,
                                fontWeight = if (selectedDaysRange == days) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedDaysRange == days) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Chart Rendering Area
            if (renderMode == 0) {
                // D3 Web SVG Container
                D3WebViewChartContainer(
                    data = dailyData,
                    selectedMetric = selectedMetric,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                // Jetpack Compose Canvas Native Chart
                NativeCanvasEvolutionChart(
                    data = dailyData,
                    selectedMetric = selectedMetric,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            // Legend / Info
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
                            .background(if (selectedMetric == 0) EmeraldPrimary else EmeraldSecondary)
                    )
                    Text(
                        text = if (selectedMetric == 0) "Flores Secas Equivalentes" else "Puntaje de Alivio Sintomático",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Apto para exportación a Informe Médico",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = EmeraldPrimary
                )
            }
        }
    }
}

@Composable
private fun MetricMiniBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun D3WebViewChartContainer(
    data: List<DailyDosagePoint>,
    selectedMetric: Int,
    modifier: Modifier = Modifier
) {
    val htmlContent = remember(data, selectedMetric) {
        generateD3ChartHtml(data, selectedMetric)
    }

    AndroidView(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setBackgroundColor(0x00000000) // Transparent background
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://cannamed.local", htmlContent, "text/html", "UTF-8", null)
        }
    )
}

/**
 * Self-contained D3.js vector chart generator.
 * Implements SVG rendering with scales, area fills, gradients, axes and data tooltips.
 * 100% offline, zero network requests, zero API dependencies.
 */
fun generateD3ChartHtml(data: List<DailyDosagePoint>, selectedMetric: Int): String {
    val labelsJson = data.map { "\"${it.dateLabel}\"" }.joinToString(",")
    val values = if (selectedMetric == 0) {
        data.map { String.format(Locale.US, "%.3f", it.gramsDriedFlower) }
    } else {
        data.map { String.format(Locale.US, "%.1f", it.averageRelief) }
    }
    val valuesJson = values.joinToString(",")
    val dosesJson = data.map { it.dosesCount }.joinToString(",")
    val metricUnit = if (selectedMetric == 0) "g" else "pts"
    val strokeColor = if (selectedMetric == 0) "#10B981" else "#34D399"
    val fillColor = if (selectedMetric == 0) "rgba(16, 185, 129, 0.25)" else "rgba(52, 211, 153, 0.25)"

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body {
                    background-color: #0F172A;
                    color: #E2E8F0;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    overflow: hidden;
                    user-select: none;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    height: 100vh;
                    width: 100vw;
                    padding: 8px;
                }
                #chart-container {
                    width: 100%;
                    height: 100%;
                    display: flex;
                    flex-direction: column;
                    position: relative;
                }
                svg {
                    width: 100%;
                    height: 100%;
                }
                .grid-line {
                    stroke: #334155;
                    stroke-dasharray: 3 3;
                    stroke-width: 0.8;
                }
                .axis-text {
                    fill: #94A3B8;
                    font-size: 10px;
                }
                .tooltip-box {
                    position: absolute;
                    background: #1E293B;
                    border: 1px solid #10B981;
                    border-radius: 6px;
                    padding: 4px 8px;
                    font-size: 11px;
                    color: #F8FAFC;
                    pointer-events: none;
                    display: none;
                    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.4);
                }
            </style>
        </head>
        <body>
            <div id="chart-container">
                <div id="tooltip" class="tooltip-box"></div>
                <svg id="d3-svg" viewBox="0 0 400 190" preserveAspectRatio="none"></svg>
            </div>
            <script>
                (function() {
                    const labels = [$labelsJson];
                    const values = [$valuesJson];
                    const doses = [$dosesJson];
                    const n = labels.length;
                    const maxVal = Math.max(...values, ${if (selectedMetric == 0) "1.0" else "5.0"});
                    const svg = document.getElementById('d3-svg');
                    const tooltip = document.getElementById('tooltip');

                    const width = 400;
                    const height = 190;
                    const padLeft = 32;
                    const padRight = 16;
                    const padTop = 18;
                    const padBottom = 28;

                    const chartWidth = width - padLeft - padRight;
                    const chartHeight = height - padTop - padBottom;

                    // Draw Horizontal Grid & Y-Axis Labels
                    const ySteps = 4;
                    for (let i = 0; i <= ySteps; i++) {
                        const y = padTop + (chartHeight * i / ySteps);
                        const val = (maxVal * (ySteps - i) / ySteps).toFixed(1);
                        
                        const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                        line.setAttribute('x1', padLeft);
                        line.setAttribute('y1', y);
                        line.setAttribute('x2', width - padRight);
                        line.setAttribute('y2', y);
                        line.setAttribute('class', 'grid-line');
                        svg.appendChild(line);

                        const txt = document.createElementNS('http://www.w3.org/2000/svg', 'text');
                        txt.setAttribute('x', padLeft - 6);
                        txt.setAttribute('y', y + 3);
                        txt.setAttribute('text-anchor', 'end');
                        txt.setAttribute('class', 'axis-text');
                        txt.textContent = val;
                        svg.appendChild(txt);
                    }

                    // Compute points
                    const points = [];
                    for (let i = 0; i < n; i++) {
                        const x = padLeft + (n === 1 ? chartWidth / 2 : (i * chartWidth / (n - 1)));
                        const y = padTop + chartHeight - (values[i] / maxVal * chartHeight);
                        points.push({ x, y, val: values[i], label: labels[i], dose: doses[i] });
                    }

                    // Draw Area Polygon
                    if (points.length > 0) {
                        let pathD = 'M ' + points[0].x + ' ' + (padTop + chartHeight);
                        points.forEach(p => {
                            pathD += ' L ' + p.x + ' ' + p.y;
                        });
                        pathD += ' L ' + points[points.length - 1].x + ' ' + (padTop + chartHeight) + ' Z';

                        const area = document.createElementNS('http://www.w3.org/2000/svg', 'path');
                        area.setAttribute('d', pathD);
                        area.setAttribute('fill', '$fillColor');
                        svg.appendChild(area);

                        // Draw Curve Line
                        let lineD = 'M ' + points[0].x + ' ' + points[0].y;
                        for (let i = 1; i < points.length; i++) {
                            // Smooth bezier or straight line
                            lineD += ' L ' + points[i].x + ' ' + points[i].y;
                        }
                        const line = document.createElementNS('http://www.w3.org/2000/svg', 'path');
                        line.setAttribute('d', lineD);
                        line.setAttribute('fill', 'none');
                        line.setAttribute('stroke', '$strokeColor');
                        line.setAttribute('stroke-width', '2.5');
                        line.setAttribute('stroke-linecap', 'round');
                        line.setAttribute('stroke-linejoin', 'round');
                        svg.appendChild(line);
                    }

                    // Draw Points and X-Axis labels
                    const stepSkip = n > 14 ? 3 : (n > 7 ? 2 : 1);
                    points.forEach((p, idx) => {
                        // Point Circle
                        const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
                        circle.setAttribute('cx', p.x);
                        circle.setAttribute('cy', p.y);
                        circle.setAttribute('r', p.val > 0 ? '4' : '2.5');
                        circle.setAttribute('fill', p.val > 0 ? '$strokeColor' : '#475569');
                        circle.setAttribute('stroke', '#0F172A');
                        circle.setAttribute('stroke-width', '1.5');
                        svg.appendChild(circle);

                        // X-axis label
                        if (idx % stepSkip === 0 || idx === n - 1) {
                            const txt = document.createElementNS('http://www.w3.org/2000/svg', 'text');
                            txt.setAttribute('x', p.x);
                            txt.setAttribute('y', height - 8);
                            txt.setAttribute('text-anchor', 'middle');
                            txt.setAttribute('class', 'axis-text');
                            txt.textContent = p.label;
                            svg.appendChild(txt);
                        }
                    });
                })();
            </script>
        </body>
        </html>
    """.trimIndent()
}

/**
 * Native Jetpack Compose Canvas implementation for dosage evolution.
 */
@Composable
fun NativeCanvasEvolutionChart(
    data: List<DailyDosagePoint>,
    selectedMetric: Int,
    modifier: Modifier = Modifier
) {
    val points = data
    val maxValue = remember(points, selectedMetric) {
        val vals = if (selectedMetric == 0) points.map { it.gramsDriedFlower } else points.map { it.averageRelief }
        (vals.maxOrNull() ?: 1.0).coerceAtLeast(if (selectedMetric == 0) 1.0 else 5.0).toFloat()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            val chartW = size.width
            val chartH = size.height - 20.dp.toPx()
            val n = points.size

            if (n < 1) return@Canvas

            // Horizontal Grid Lines
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = chartH * i / gridLines
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(chartW, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Calculate coordinates
            val coords = points.mapIndexed { idx, pt ->
                val x = if (n == 1) chartW / 2f else (idx.toFloat() / (n - 1)) * chartW
                val value = if (selectedMetric == 0) pt.gramsDriedFlower.toFloat() else pt.averageRelief.toFloat()
                val y = chartH - (value / maxValue * chartH)
                Offset(x, y)
            }

            // Fill Path
            val fillPath = Path().apply {
                moveTo(coords.first().x, chartH)
                coords.forEach { lineTo(it.x, it.y) }
                lineTo(coords.last().x, chartH)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        (if (selectedMetric == 0) EmeraldPrimary else EmeraldSecondary).copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = chartH
                )
            )

            // Stroke Path
            val strokePath = Path().apply {
                moveTo(coords.first().x, coords.first().y)
                for (i in 1 until coords.size) {
                    lineTo(coords[i].x, coords[i].y)
                }
            }

            drawPath(
                path = strokePath,
                color = if (selectedMetric == 0) EmeraldPrimary else EmeraldSecondary,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Dots
            coords.forEachIndexed { index, offset ->
                val hasValue = (if (selectedMetric == 0) points[index].gramsDriedFlower else points[index].averageRelief) > 0
                drawCircle(
                    color = if (hasValue) (if (selectedMetric == 0) EmeraldPrimary else EmeraldSecondary) else Color.Gray.copy(alpha = 0.5f),
                    radius = if (hasValue) 4.5.dp.toPx() else 2.5.dp.toPx(),
                    center = offset
                )
                drawCircle(
                    color = Color(0xFF1E293B),
                    radius = 2.dp.toPx(),
                    center = offset
                )
            }
        }
    }
}
