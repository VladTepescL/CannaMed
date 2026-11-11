package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary

/**
 * Data structure for certified AdMob health & wellness simulated ads.
 * Strictly adheres to Google Play & Google AdMob Restricted Products Policies:
 * - NO promotion of recreational drugs, paraphernalia (bongs, pipes, vapes).
 * - NO sale of controlled substances or cannabinoids.
 * - Strictly approved health categories: Telemedicine, General Medical Monitors, HEPA Air Quality, Agronomy Sensors.
 */
data class CompliantAdCampaign(
    val title: String,
    val description: String,
    val categoryLabel: String,
    val ctaText: String = "Conocer Más"
)

object CompliantAdInventory {
    val BANNERS = listOf(
        CompliantAdCampaign(
            title = "Telemedicina & Consultas Médicas Online",
            description = "Atención con médicos especialistas acreditados para seguimiento de tratamientos.",
            categoryLabel = "Salud Acreditada"
        ),
        CompliantAdCampaign(
            title = "Monitores de Signos Vitales Grado Clínico",
            description = "Tensiómetros y pulsioxímetros digitales certificados para control domiciliario.",
            categoryLabel = "Dispositivos Médicos"
        ),
        CompliantAdCampaign(
            title = "Organizadores de Medicación con Recordatorio",
            description = "Dispensadores horarios inteligentes para máxima adherencia terapéutica.",
            categoryLabel = "Gestión de Salud"
        ),
        CompliantAdCampaign(
            title = "Purificadores de Aire con Filtro HEPA Médico",
            description = "Filtración de alérgenos y partículas para mejorar la salud respiratoria interior.",
            categoryLabel = "Higiene Ambiental"
        ),
        CompliantAdCampaign(
            title = "Sensores de Humedad y pH para Botánica Doméstica",
            description = "Monitoreo agronómico IoT de precisión para horticultura y cultivo orgánico.",
            categoryLabel = "Agrotecnología"
        )
    )

    val INTERSTITIAL = CompliantAdCampaign(
        title = "Red de Teleasistencia Médica Especializada",
        description = "Plataforma de orientación médica a distancia con profesionales habilitados por el Ministerio de Salud.",
        categoryLabel = "Atención Clínica Certificada"
    )
}

/**
 * Simulated AdMob Banner Component
 * Strictly complies with Google AdMob & Google Play Policies.
 * Hidden immediately if isPremium is true.
 */
@Composable
fun SimulatedAdMobBanner(
    isPremium: Boolean,
    modifier: Modifier = Modifier,
    adTopic: String? = null,
    onUpgradeClick: () -> Unit = {}
) {
    if (isPremium) return

    val ad = remember(adTopic) {
        if (adTopic != null) {
            CompliantAdInventory.BANNERS.find { it.title.contains(adTopic, ignoreCase = true) }
                ?: CompliantAdInventory.BANNERS.first()
        } else {
            CompliantAdInventory.BANNERS.random()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("simulated_admob_banner"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
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
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFB300))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "Ad",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Text(
                        text = "Anuncio AdMob • ${ad.categoryLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Quitar Anuncios (Premium)",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onUpgradeClick() }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ad.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = ad.description,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldPrimary)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = ad.ctaText,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Simulated AdMob Interstitial Dialog
 * Shown after actions like saving a dose, cartridge, or strain.
 * Automatically bypassed if isPremium is true.
 * NEVER shown on medical prescriptions or emergency screens.
 */
@Composable
fun SimulatedAdMobInterstitialDialog(
    isPremium: Boolean,
    actionContext: String = "Acción Registrada",
    onDismiss: () -> Unit,
    onUpgradeClick: () -> Unit = {}
) {
    if (isPremium) {
        onDismiss()
        return
    }

    val ad = CompliantAdInventory.INTERSTITIAL

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .testTag("simulated_interstitial_dialog")
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFFB300))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("AdMob Interstitial", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Text(
                            text = "Publicidad Médica Certificada",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar Anuncio")
                    }
                }

                Text(
                    text = "¡$actionContext guardado exitosamente!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    text = ad.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = ad.categoryLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Text(
                            text = ad.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onUpgradeClick()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Activar Premium", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Continuar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
