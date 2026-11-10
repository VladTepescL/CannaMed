package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.KnownTerpenes
import com.example.data.model.StrainInfo
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.StrainHybridColor
import com.example.ui.theme.StrainIndicaColor
import com.example.ui.theme.StrainSativaColor
import java.net.URLEncoder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StrainsLiteratureScreen(
    strains: List<StrainInfo>,
    strainNamesList: List<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSaveCustomStrain: (StrainInfo) -> Unit
) {
    val context = LocalContext.current
    val secPrefs = remember { com.example.util.SecurityPreferences(context) }
    var showAddStrainDialog by remember { mutableStateOf(false) }
    var showTerpenesGuide by remember { mutableStateOf(false) }
    var showInterstitialAfterStrain by remember { mutableStateOf(false) }

    // Autocomplete filtering from in-memory strainNamesList
    val autocompleteSuggestions = remember(searchQuery, strainNamesList) {
        if (searchQuery.length >= 2) {
            strainNamesList.filter { it.contains(searchQuery, ignoreCase = true) }.take(5)
        } else {
            emptyList()
        }
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
                        text = "Literatura & Cepas (Leafly)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Búsquedas directas a la base científica de Leafly.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showAddStrainDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_custom_strain_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nueva")
                }
            }
        }

        // Direct Leafly Search Bar with In-Memory Autocomplete Suggestions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("strain_search_input"),
                        placeholder = { Text("Buscar cepa, tipo, efecto o indicación...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar", tint = EmeraldPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary
                        )
                    )

                    // In-Memory Autocomplete Suggestions Chips
                    AnimatedVisibility(visible = autocompleteSuggestions.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Sugerencias de cepas en memoria:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                autocompleteSuggestions.forEach { suggestion ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(EmeraldPrimary.copy(alpha = 0.2f))
                                            .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .clickable {
                                                onSearchQueryChange(suggestion)
                                                openLeaflyUrl(context, "https://www.leafly.com/search?q=${URLEncoder.encode(suggestion, "UTF-8")}")
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "$suggestion ↗",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Direct Search button to Leafly.com
                    Button(
                        onClick = {
                            val queryParam = if (searchQuery.isNotBlank()) searchQuery else "medical cannabis"
                            openLeaflyUrl(context, "https://www.leafly.com/search?q=${URLEncoder.encode(queryParam, "UTF-8")}")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("direct_leafly_search_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Buscar '$searchQuery' en Leafly.com" else "Explorar Catálogo en Leafly.com",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Simulated AdMob Banner below search bar (hidden if isPremium)
        item {
            com.example.ui.components.SimulatedAdMobBanner(isPremium = secPrefs.isPremium)
        }

        // SEGMENT 1: 4 OPCIONES (2 CUADROS DE 2 FILAS) PARA SATIVA, INDICA, HÍBRIDA, ALTO CBD
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Tipos de Cepas Principales (Catálogo Leafly):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )

                // Row 1: Sativa & Indica
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StrainTypeGridCard(
                        modifier = Modifier.weight(1f),
                        title = "Sativa",
                        subtitle = "Energía y Enfoque diurno",
                        badgeColor = StrainSativaColor,
                        onClick = { openLeaflyUrl(context, "https://www.leafly.com/strains/sativa") }
                    )
                    StrainTypeGridCard(
                        modifier = Modifier.weight(1f),
                        title = "Indica",
                        subtitle = "Sedación y Alivio nocturno",
                        badgeColor = StrainIndicaColor,
                        onClick = { openLeaflyUrl(context, "https://www.leafly.com/strains/indica") }
                    )
                }

                // Row 2: Híbrida & Alto CBD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StrainTypeGridCard(
                        modifier = Modifier.weight(1f),
                        title = "Híbrida",
                        subtitle = "Equilibrio corporal y mental",
                        badgeColor = StrainHybridColor,
                        onClick = { openLeaflyUrl(context, "https://www.leafly.com/strains/hybrid") }
                    )
                    StrainTypeGridCard(
                        modifier = Modifier.weight(1f),
                        title = "Alto CBD",
                        subtitle = "Sin psicoactividad, antiinflamatorio",
                        badgeColor = EmeraldSecondary,
                        onClick = { openLeaflyUrl(context, "https://www.leafly.com/search?q=high+cbd") }
                    )
                }
            }
        }

        // SEGMENT 2: MISMO FORMATO DE CUADROS PARA DOLENCIAS (MIGRAÑA, ANSIEDAD, DEPRESIÓN, DOLOR CRÓNICO, ETC.)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Cepas por Dolencia Médica (Búsqueda Directa Leafly):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldSecondary
                )

                // Row 1: Migraña & Ansiedad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AilmentGridCard(
                        modifier = Modifier.weight(1f),
                        ailmentName = "Migraña",
                        description = "Alivio cefaleas y náuseas",
                        onClick = { openLeaflyUrl(context, "https://www.leafly.com/search?q=migraines") }
                    )
                    AilmentGridCard(
                        modifier = Modifier.weight(1f),
                        ailmentName = "Ansiedad",
                        description = "Reducción de estrés y pánico",
                        onClick = { openLeaflyUrl(context, "https://www.leafly.com/search?q=anxiety+relief") }
                    )
                }

                // Row 2: Depresión & Dolor Crónico
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AilmentGridCard(
                        modifier = Modifier.weight(1f),
                        ailmentName = "Depresión",
                        description = "Elevación anímica y vitalidad",
                        onClick = { openLeaflyUrl(context, "https://www.leafly.com/search?q=depression") }
                    )
                    AilmentGridCard(
                        modifier = Modifier.weight(1f),
                        ailmentName = "Dolor Crónico",
                        description = "Neuropatía, articulaciones y lumbar",
                        onClick = { openLeaflyUrl(context, "https://www.leafly.com/search?q=chronic+pain") }
                    )
                }

                // Row 3: Insomnio & Espasmos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AilmentGridCard(
                        modifier = Modifier.weight(1f),
                        ailmentName = "Insomnio",
                        description = "Inducción al sueño reparador",
                        onClick = { openLeaflyUrl(context, "https://www.leafly.com/search?q=insomnia+sleep") }
                    )
                    AilmentGridCard(
                        modifier = Modifier.weight(1f),
                        ailmentName = "Espasmos",
                        description = "Relajación muscular y temblores",
                        onClick = { openLeaflyUrl(context, "https://www.leafly.com/search?q=spasms+tremors") }
                    )
                }
            }
        }

        // Educational Terpenes banner toggle
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showTerpenesGuide = !showTerpenesGuide },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Science, contentDescription = null, tint = EmeraldSecondary)
                        Column {
                            Text(
                                text = "Efecto Séquito y Guía de Terpenos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (showTerpenesGuide) "Toca para ocultar" else "Mirceno, Limoneno, Cariofileno y enlaces directos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = EmeraldTertiary)
                }

                if (showTerpenesGuide) {
                    Column(
                        modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KnownTerpenes.ALL.forEach { terp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable {
                                        openLeaflyUrl(context, "https://www.leafly.com/search?q=${terp.name.lowercase()}")
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(terp.color)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${terp.name} (Buscar en Leafly)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = terp.color
                                    )
                                    Text(
                                        text = "Aroma: ${terp.aroma} • Efecto: ${terp.therapeuticBenefit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = EmeraldSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Local Curated Strains Cards
        items(strains, key = { it.id }) { strain ->
            StrainCard(
                strain = strain,
                onOpenLeafly = {
                    openLeaflyUrl(context, strain.leaflyUrl)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showAddStrainDialog) {
        AddCustomStrainDialog(
            onDismiss = { showAddStrainDialog = false },
            onSave = {
                onSaveCustomStrain(it)
                showAddStrainDialog = false
                if (!secPrefs.isPremium) {
                    showInterstitialAfterStrain = true
                }
            }
        )
    }

    if (showInterstitialAfterStrain && !secPrefs.isPremium) {
        com.example.ui.components.SimulatedAdMobInterstitialDialog(
            isPremium = secPrefs.isPremium,
            actionContext = "Cepa Personalizada Guardada",
            onDismiss = { showInterstitialAfterStrain = false }
        )
    }
}

// 2x2 grid card for Sativa, Indica, Híbrida, Alto CBD
@Composable
fun StrainTypeGridCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = badgeColor, modifier = Modifier.size(16.dp))
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

// 2x2 grid card for medical ailments (Migraña, Ansiedad, etc.)
@Composable
fun AilmentGridCard(
    modifier: Modifier = Modifier,
    ailmentName: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, EmeraldSecondary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ailmentName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldSecondary
                )
                Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = EmeraldSecondary, modifier = Modifier.size(16.dp))
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

fun openLeaflyUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StrainCard(
    strain: StrainInfo,
    onOpenLeafly: () -> Unit
) {
    val typeColor = when {
        strain.type.contains("Indica", ignoreCase = true) -> StrainIndicaColor
        strain.type.contains("Sativa", ignoreCase = true) -> StrainSativaColor
        strain.type.contains("Alto CBD", ignoreCase = true) -> EmeraldSecondary
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
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(typeColor)
                    )
                    Text(
                        text = strain.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(typeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = strain.type,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(8.dp)
                ) {
                    Column {
                        Text("THC Estimado", style = MaterialTheme.typography.labelSmall, color = EmeraldTertiary)
                        Text(strain.thcRange, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(8.dp)
                ) {
                    Column {
                        Text("CBD Estimado", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                        Text(strain.cbdRange, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = "Terpenos: ${strain.dominantTerpenes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Indicaciones clínicas: ${strain.medicalBenefits}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldPrimary
            )

            Text(
                text = strain.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onOpenLeafly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ver Literatura e Investigación en Leafly.com")
            }
        }
    }
}

@Composable
fun AddCustomStrainDialog(
    onDismiss: () -> Unit,
    onSave: (StrainInfo) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Híbrida") }
    var thcRange by remember { mutableStateOf("15-20%") }
    var cbdRange by remember { mutableStateOf("< 1%") }
    var terpenes by remember { mutableStateOf("Mirceno, Cariofileno") }
    var benefits by remember { mutableStateOf("Dolor, Insomnio") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Añadir Cepa Personalizada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la cepa") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Tipo (Indica, Sativa, Híbrida, Alto CBD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = thcRange,
                        onValueChange = { thcRange = it },
                        label = { Text("THC %") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cbdRange,
                        onValueChange = { cbdRange = it },
                        label = { Text("CBD %") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = terpenes,
                    onValueChange = { terpenes = it },
                    label = { Text("Terpenos dominantes") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = benefits,
                    onValueChange = { benefits = it },
                    label = { Text("Beneficios medicinales") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val slug = name.lowercase().replace(" ", "-")
                            onSave(
                                StrainInfo(
                                    name = name,
                                    type = type,
                                    thcRange = thcRange,
                                    cbdRange = cbdRange,
                                    dominantTerpenes = terpenes,
                                    medicalBenefits = benefits,
                                    flavorAroma = "Herbal",
                                    description = description,
                                    leaflySlug = slug,
                                    leaflyUrl = "https://www.leafly.com/search?q=$slug",
                                    isCustom = true
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Guardar Cepa")
                }
            }
        }
    }
}
