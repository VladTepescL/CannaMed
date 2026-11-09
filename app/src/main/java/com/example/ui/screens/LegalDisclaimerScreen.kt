package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary

@Composable
fun LegalDisclaimerScreen(
    onToggleScreenshotProtection: (Boolean) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Version Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "CannaMed Mobile",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Versión 2.4.0 (Build 240) • Canal Oficial de Pacientes",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "Sistema integral para bitácora terapéutica, dosificación farmacológica de cannabinoides, gestión de recetas médicas y registro de equivalencias de flores secas conforme a estándares de salud pública.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Mandatory Medical Disclaimer
        item {
            DisclaimerCard(
                title = "Descargo de Responsabilidad Médica (Medical Disclaimer)",
                icon = Icons.Default.MedicalInformation,
                accentColor = MaterialTheme.colorScheme.error,
                paragraphs = listOf(
                    "CannaMed es una herramienta tecnológica para el registro y monitoreo personal del paciente bajo prescripción facultativa. NO constituye ni sustituye la consulta médica, diagnóstico, evaluación ni prescripción de un médico cirujano o profesional de la salud debidamente colegiado.",
                    "Los cálculos de dosificación y equivalencias a flores secas son referenciales y se basan en farmacopeas internacionales (Health Canada Cannabis Act - Schedule 3). Nunca modifiques las dosis prescritas por tu médico tratante sin su previa autorización.",
                    "En caso de experimentar reacciones adversas agudas, taquicardia severa, crisis de pánico o malestar imprevisto, suspende la administración de inmediato y acude al servicio de urgencias más cercano."
                )
            )
        }

        // Mandatory Legal Disclaimer
        item {
            DisclaimerCard(
                title = "Descargo de Responsabilidad Jurídica (Legal Disclaimer)",
                icon = Icons.Default.Gavel,
                accentColor = EmeraldPrimary,
                paragraphs = listOf(
                    "Esta aplicación no promueve la compra, venta, suministro ilícito ni tráfico de sustancias estupefacientes o psicotrópicas. Su único propósito es la asistencia a pacientes con prescripción facultativa emitida por un profesional habilitado.",
                    "En Chile, el autocultivo y la tenencia de cannabis para tratamiento médico están amparados por los artículos 4, 8 y 50 de la Ley N° 20.000, sustentados por una receta médica vigente, informe de diagnóstico y destino de uso personal exclusivo y próximo en el tiempo ratificado por jurisprudencia de la Corte Suprema.",
                    "La legislación sobre cannabis medicinal varía significativamente según el país y jurisdicción. Es responsabilidad exclusiva del usuario conocer y cumplir las normas legales locales que regulan la posesión y el consumo terapéutico."
                )
            )
        }

        // Artificial Intelligence Disclaimer
        item {
            DisclaimerCard(
                title = "Uso de Inteligencia Artificial (Gemini 3.8 Flash)",
                icon = Icons.Default.Balance,
                accentColor = EmeraldSecondary,
                paragraphs = listOf(
                    "El Asistente Clínico integrado utiliza modelos avanzados de inteligencia artificial (Gemini 3.8 Flash a través de Firebase AI Logic) para proporcionar orientación botánica, propiedades de cepas, perfiles de terpenos y prácticas de autocultivo orgánico.",
                    "Las respuestas generadas por IA tienen carácter meramente orientativo y educativo. No constituyen diagnósticos clínicos ni órdenes médicas.",
                    "La interacción con la IA no establece una relación médico-paciente ni profesional-cliente."
                )
            )
        }

        // Privacy and Data Protection Compliance
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary)
                        Text(
                            text = "Privacidad y Cumplimiento Normativo de Datos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    ComplianceRow(
                        title = "Ley N° 19.628 (Chile)",
                        subtitle = "Protección de la vida privada y datos sensibles de salud."
                    )
                    ComplianceRow(
                        title = "Ley N° 20.584 (Chile)",
                        subtitle = "Derechos y deberes que tienen las personas en relación con acciones vinculadas a su atención en salud (reserva de ficha clínica)."
                    )
                    ComplianceRow(
                        title = "HIPAA Security Rule (EE.UU. - 45 CFR § 164.312)",
                        subtitle = "Cifrado AES-256-GCM para datos médicos en tránsito y respaldos."
                    )
                    ComplianceRow(
                        title = "GDPR Artículos 9 y 32 (Unión Europea)",
                        subtitle = "Tratamiento y seguridad especial para categorías de datos relativos a la salud con almacenamiento Zero-Knowledge estrictamente en el dispositivo."
                    )
                }
            }
        }

        // Active Security Controls Card (Biometrics & Anti-Screenshot)
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            val secPrefs = remember { com.example.util.SecurityPreferences(context) }
            var biometricEnabled by remember { mutableStateOf(secPrefs.isBiometricLockEnabled) }
            var screenshotProtEnabled by remember { mutableStateOf(secPrefs.isScreenshotProtectionEnabled) }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary)
                        Text(
                            text = "Ajustes de Seguridad en el Dispositivo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Biometric Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bloqueo Biométrico al Iniciar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Requiere huella dactilar o reconocimiento facial al abrir la app.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        androidx.compose.material3.Switch(
                            checked = biometricEnabled,
                            onCheckedChange = {
                                biometricEnabled = it
                                secPrefs.isBiometricLockEnabled = it
                            },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Screenshot Protection Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Protección contra Capturas (FLAG_SECURE)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Impide capturas de pantalla o grabación por apps espía en recetas y fichas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        androidx.compose.material3.Switch(
                            checked = screenshotProtEnabled,
                            onCheckedChange = {
                                screenshotProtEnabled = it
                                secPrefs.isScreenshotProtectionEnabled = it
                                onToggleScreenshotProtection(it)
                            },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DisclaimerCard(
    title: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    paragraphs: List<String>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            paragraphs.forEach { paragraph ->
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Text(
                        text = paragraph,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ComplianceRow(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(18.dp)
        )
        Column {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
