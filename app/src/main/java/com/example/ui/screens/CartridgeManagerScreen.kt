package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldSecondaryContainer
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.StrainHybridColor
import com.example.ui.theme.StrainIndicaColor
import com.example.ui.theme.StrainSativaColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CartridgeManagerScreen(
    activeCartridges: List<Cartridge>,
    allCartridges: List<Cartridge>,
    onSaveCartridge: (Cartridge) -> Unit,
    onAddPuffs: (Long, Int) -> Unit,
    onMarkFinished: (Long) -> Unit
) {
    var isAddFormExpanded by remember { mutableStateOf(false) }
    var selectedCartridgeForDetails by remember { mutableStateOf<Cartridge?>(null) }
    val finishedCartridges = remember(allCartridges) { allCartridges.filter { !it.isActive } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            // Header Info & Toggle Add Form
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cartuchos & Aceite Clear",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pods abiertos, proporciones y control de caladas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { isAddFormExpanded = !isAddFormExpanded },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAddFormExpanded) MaterialTheme.colorScheme.surfaceContainerHigh else EmeraldPrimary,
                        contentColor = if (isAddFormExpanded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("toggle_add_cartridge_button")
                ) {
                    Icon(
                        if (isAddFormExpanded) Icons.Default.ExpandLess else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isAddFormExpanded) "Ocultar" else "Agregar")
                }
            }
        }

        // Inline Add Cartridge Form (Nombre, Tipo, Proporciones / % de Cannabinoides)
        item {
            AnimatedVisibility(visible = isAddFormExpanded) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    AddCartridgeFormContent(
                        onSave = { newCart ->
                            onSaveCartridge(newCart)
                            isAddFormExpanded = false
                        },
                        onCancel = { isAddFormExpanded = false }
                    )
                }
            }
        }

        // Section: Pods Abiertos (Showing ONLY name and strain type)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cápsulas y Pods Abiertos (${activeCartridges.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                Text(
                    text = "Toca para ver detalles",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (activeCartridges.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = EmeraldTertiary,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No hay cartuchos abiertos en uso",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Presiona 'Agregar' arriba para registrar un nuevo pod con nombre, proporciones y cannabinoides.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // ONLY name and strain type shown here, tapping opens full detail modal
            items(activeCartridges, key = { it.id }) { cart ->
                OpenCartridgeCompactCard(
                    cartridge = cart,
                    onClick = { selectedCartridgeForDetails = cart }
                )
            }
        }

        // Finished Pods History
        if (finishedCartridges.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = EmeraldSecondary)
                    Text(
                        text = "Historial de Cápsulas Cambiadas (${finishedCartridges.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            items(finishedCartridges, key = { it.id }) { cart ->
                FinishedCartridgeCard(cartridge = cart)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Modal with all detailed data when tapped
    selectedCartridgeForDetails?.let { cart ->
        CartridgeDetailDialog(
            cartridge = cart,
            onDismiss = { selectedCartridgeForDetails = null },
            onAddPuffs = { puffs ->
                onAddPuffs(cart.id, puffs)
                // Update local pointer so UI updates
                selectedCartridgeForDetails = cart.copy(totalPuffsLogged = cart.totalPuffsLogged + puffs)
            },
            onMarkFinished = {
                onMarkFinished(cart.id)
                selectedCartridgeForDetails = null
            }
        )
    }
}

// Compact row showing ONLY the name and strain type of the open pod
@Composable
fun OpenCartridgeCompactCard(
    cartridge: Cartridge,
    onClick: () -> Unit
) {
    val strainColor = when {
        cartridge.varietyName.contains("Indica", ignoreCase = true) -> StrainIndicaColor
        cartridge.varietyName.contains("Sativa", ignoreCase = true) -> StrainSativaColor
        else -> StrainHybridColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("open_cartridge_item_${cartridge.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "${cartridge.brandName} - ${cartridge.varietyName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tipo: ${cartridge.formatType.displayName} • Cepa Abierta",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmeraldPrimary.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ABIERTO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            }
        }
    }
}

// Dialog that opens when tapping an open pod, displaying all detailed metrics
@Composable
fun CartridgeDetailDialog(
    cartridge: Cartridge,
    onDismiss: () -> Unit,
    onAddPuffs: (Int) -> Unit,
    onMarkFinished: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val installDateStr = remember(cartridge.installedTimestamp) {
        dateFormat.format(Date(cartridge.installedTimestamp))
    }

    val estimatedMaxPuffs = remember(cartridge.capacityMl) {
        (cartridge.capacityMl * 220).toInt()
    }
    val progress = (cartridge.totalPuffsLogged.toFloat() / estimatedMaxPuffs.coerceAtLeast(1)).coerceIn(0f, 1f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
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
                    Column {
                        Text(
                            text = cartridge.varietyName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${cartridge.brandName} • Detalle del Pod",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date & Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Fecha y Hora de Apertura:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(installDateStr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("EN USO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                    }

                    // Cannabinoids % and mg/ml
                    Text("Proporciones y Porcentajes de Cannabinoides:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CannabinoidStatChip(
                            modifier = Modifier.weight(1f),
                            label = "THC",
                            percentage = "${cartridge.thcPercentage}%",
                            mgPerMl = "${cartridge.mgPerMlThc.toInt()} mg/ml",
                            color = EmeraldTertiary
                        )
                        CannabinoidStatChip(
                            modifier = Modifier.weight(1f),
                            label = "CBD",
                            percentage = "${cartridge.cbdPercentage}%",
                            mgPerMl = "${cartridge.mgPerMlCbd.toInt()} mg/ml",
                            color = EmeraldPrimary
                        )
                        if (cartridge.cbnPercentage > 0f || cartridge.cbgPercentage > 0f) {
                            CannabinoidStatChip(
                                modifier = Modifier.weight(1f),
                                label = "CBN / CBG",
                                percentage = "${cartridge.cbnPercentage}% / ${cartridge.cbgPercentage}%",
                                mgPerMl = "${((cartridge.cbnPercentage + cartridge.cbgPercentage) * 10).toInt()} mg",
                                color = EmeraldSecondary
                            )
                        }
                    }

                    // Dominant Terpenes
                    if (cartridge.terpenesSummary.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Science, contentDescription = null, tint = EmeraldSecondary, modifier = Modifier.size(16.dp))
                                Text("Perfil de Terpenos:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                            Text(cartridge.terpenesSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Puffs Tracker Card with controls
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Caladas Acumuladas:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${cartridge.totalPuffsLogged} caladas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { onAddPuffs(1) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                    ) {
                                        Text("+1")
                                    }
                                    Button(
                                        onClick = { onAddPuffs(3) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondaryContainer)
                                    ) {
                                        Text("+3")
                                    }
                                }
                            }

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = if (progress > 0.85f) EmeraldTertiary else EmeraldPrimary,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                            Text(
                                text = "~${(progress * 100).toInt()}% consumido de ${cartridge.capacityMl} ml (~$estimatedMaxPuffs caladas estimadas)",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (cartridge.notes.isNotBlank()) {
                        Text("Notas: ${cartridge.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Button to Swap / Change Capsule
                    OutlinedButton(
                        onClick = onMarkFinished,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldTertiary)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cambiar / Finalizar Cápsula", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Inline Form for adding cartridge with name, type, proportion or percentages
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCartridgeFormContent(
    onSave: (Cartridge) -> Unit,
    onCancel: () -> Unit
) {
    var brandName by remember { mutableStateOf("") }
    var varietyName by remember { mutableStateOf("") }
    var selectedStrainType by remember { mutableStateOf("Híbrida") }
    var capacityMl by remember { mutableFloatStateOf(1.0f) }
    var thcPercentage by remember { mutableFloatStateOf(70.0f) }
    var cbdPercentage by remember { mutableFloatStateOf(5.0f) }
    var cbnPercentage by remember { mutableFloatStateOf(1.0f) }
    var cbgPercentage by remember { mutableFloatStateOf(1.0f) }
    var selectedTerpenes by remember {
        mutableStateOf(setOf("Mirceno 1.5%", "Beta-Cariofileno 1.0%", "Limoneno 0.8%"))
    }
    var notes by remember { mutableStateOf("") }

    val presetTerpenes = listOf(
        "Mirceno 1.5%", "Limoneno 1.2%", "Beta-Cariofileno 1.0%",
        "Alfa-Pineno 0.6%", "Linalool 0.5%", "Humuleno 0.4%"
    )

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Registrar Nuevo Cartucho / Pod",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary
        )

        OutlinedTextField(
            value = brandName,
            onValueChange = { brandName = it },
            label = { Text("Marca / Fabricante (ej. MedReleaf, Curaleaf)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary)
        )

        OutlinedTextField(
            value = varietyName,
            onValueChange = { varietyName = it },
            label = { Text("Nombre de Cepa / Variedad (ej. Clear Gelato, ACDC)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary)
        )

        // Type selection
        Text("Tipo de Cepa:", style = MaterialTheme.typography.labelSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Sativa", "Indica", "Híbrida", "Alto CBD").forEach { type ->
                val isSel = selectedStrainType == type
                FilterChip(
                    selected = isSel,
                    onClick = { selectedStrainType = type },
                    label = { Text(type, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        // Capacity
        Text("Capacidad (ml):", style = MaterialTheme.typography.labelSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0.5f, 1.0f, 2.0f).forEach { cap ->
                Button(
                    onClick = { capacityMl = cap },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (capacityMl == cap) EmeraldPrimary else MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("${cap} ml", fontSize = 12.sp)
                }
            }
        }

        // Cannabinoid percentages
        Text("Proporciones de Cannabinoides (%):", style = MaterialTheme.typography.labelSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = thcPercentage.toString(),
                onValueChange = { thcPercentage = it.toFloatOrNull() ?: 0f },
                label = { Text("THC %") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldTertiary)
            )
            OutlinedTextField(
                value = cbdPercentage.toString(),
                onValueChange = { cbdPercentage = it.toFloatOrNull() ?: 0f },
                label = { Text("CBD %") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = cbnPercentage.toString(),
                onValueChange = { cbnPercentage = it.toFloatOrNull() ?: 0f },
                label = { Text("CBN %") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = cbgPercentage.toString(),
                onValueChange = { cbgPercentage = it.toFloatOrNull() ?: 0f },
                label = { Text("CBG %") },
                modifier = Modifier.weight(1f)
            )
        }

        // Terpenes
        Text("Terpenos Reportados:", style = MaterialTheme.typography.labelSmall)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presetTerpenes.forEach { terp ->
                val isSel = selectedTerpenes.contains(terp)
                Button(
                    onClick = {
                        selectedTerpenes = if (isSel) selectedTerpenes - terp else selectedTerpenes + terp
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSel) EmeraldSecondary else MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(terp, fontSize = 11.sp)
                }
            }
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notas clínicas o fecha de vencimiento") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    val fullVariety = "${varietyName.ifBlank { "Clear Extract" }} ($selectedStrainType)"
                    val newCart = Cartridge(
                        brandName = brandName.ifBlank { "MedReleaf" },
                        varietyName = fullVariety,
                        formatType = CannabisFormat.CONCENTRATE_VAPE,
                        capacityMl = capacityMl,
                        thcPercentage = thcPercentage,
                        cbdPercentage = cbdPercentage,
                        cbnPercentage = cbnPercentage,
                        cbgPercentage = cbgPercentage,
                        terpenesSummary = selectedTerpenes.joinToString(", ").ifBlank { "Sin terpenos añadidos" },
                        totalPuffsLogged = 0,
                        installedTimestamp = System.currentTimeMillis(),
                        isActive = true,
                        batchLotNumber = "LOT-${System.currentTimeMillis() % 10000}",
                        notes = notes
                    )
                    onSave(newCart)
                },
                modifier = Modifier.weight(2f),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar e Iniciar Pod", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CannabinoidStatChip(
    modifier: Modifier = Modifier,
    label: String,
    percentage: String,
    mgPerMl: String,
    color: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Text(percentage, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(mgPerMl, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun FinishedCartridgeCard(cartridge: Cartridge) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val installStr = remember(cartridge.installedTimestamp) { dateFormat.format(Date(cartridge.installedTimestamp)) }
    val finishStr = remember(cartridge.finishedTimestamp) {
        cartridge.finishedTimestamp?.let { dateFormat.format(Date(it)) } ?: "Finalizado"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${cartridge.brandName} - ${cartridge.varietyName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Vigencia: $installStr al $finishStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total de caladas administradas: ${cartridge.totalPuffsLogged}",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldSecondary
                )
            }
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Completado",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
