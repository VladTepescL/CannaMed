package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.util.BiometricAuthHelper
import com.example.util.SecurityPreferences

@Composable
fun LegalDisclaimerScreen(
    onToggleScreenshotProtection: (Boolean) -> Unit = {},
    onClearAllData: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val secPrefs = remember { SecurityPreferences(context) }
    var biometricEnabled by remember { mutableStateOf(secPrefs.isBiometricLockEnabled) }
    var screenshotProtEnabled by remember { mutableStateOf(secPrefs.isScreenshotProtectionEnabled) }
    var isAgeVerified by remember { mutableStateOf(secPrefs.isAgeVerified21Plus) }
    var isPremium by remember { mutableStateOf(secPrefs.isPremium) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showIdentityCredentialModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Medical Emergency Protocol & Poison Control Contacts (PRIORITY 1 AT TOP)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(16.dp))
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
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "Protocolo de Emergencia Médica y Toxicología",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Atención 24/7 en Caso de Reacción Adversa o Intoxicación",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = "Si presentas taquicardia severa, hipotensión aguda o reacción adversa imprevista, suspende el uso y comunícate de inmediato:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EmergencyCallButton(
                            modifier = Modifier.weight(1f),
                            label = "SAMU (Chile) 131",
                            phone = "131",
                            context = context
                        )
                        EmergencyCallButton(
                            modifier = Modifier.weight(1f),
                            label = "CITUC +56 2 2635 3800",
                            phone = "+56226353800",
                            context = context
                        )
                    }

                    Text(
                        text = "Centro de Información Toxicológica UC (CITUC) disponible 24 horas continuas para orientación clínica por sobredosis o intoxicación por sustancias.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

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
                                text = "Versión 2.5.0 (Build 250) • Cumplimiento Google Play",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "Sistema médico integral para bitácora terapéutica, dosificación farmacológica de cannabinoides, gestión de recetas médicas y registro de equivalencias de flores secas conforme a estándares de salud pública.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // CRITICAL GOOGLE PLAY POLICY: Controlled Substances & No Marijuana Sales
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
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
                                .background(EmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "Cumplimiento Políticas Google Play Store",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Política de Sustancias Controladas y Prohibición de Venta",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSecondary
                            )
                        }
                    }

                    Text(
                        text = "• No Venta ni Comercio: CannaMed es una herramienta tecnológica exclusiva para el registro clínico personal. Esta aplicación NO vende, NO comercializa, NO suministra, NO procesa pedidos, NO contiene carritos de compra ni enlaces de pago, y NO facilita la recogida ni el reparto de cannabis ni de ningún producto derivado.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• No Enlaces Comerciales: La aplicación no enlaza a dispensarios, tiendas virtuales, carritos externos de compraventa ni servicios de entrega de sustancias psicotrópicas, cumpliendo cabalmente las directrices del programa de desarrolladores de Google Play.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

        // Mandatory Legal Disclaimer (Law 20.000 Chile & International Framework)
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

        // Mature 18+ Age & Prescription Verification Gate (Google Play Families Policy)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldPrimary)
                            Column {
                                Text("Mayoría de Edad Terapéutica (21+) y Condición de Paciente", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Acredito ser mayor de 21 años y usar la app bajo indicación o receta médica profesional.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isAgeVerified,
                            onCheckedChange = {
                                isAgeVerified = it
                                secPrefs.isAgeVerified21Plus = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = { showIdentityCredentialModal = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (secPrefs.hasIdentityVerification) "Ver Credencial y Respaldo de Identidad (Cédula y Selfie)" else "Respaldar Cédula de Identidad y Selfie con Documento (21+)",
                            color = EmeraldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Device Permission Transparency (Google Play Permission Audit)
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
                        Icon(Icons.Default.Policy, contentDescription = null, tint = EmeraldPrimary)
                        Text(
                            text = "Transparencia de Permisos de Dispositivo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    PermissionDetailRow(
                        icon = Icons.Default.CameraAlt,
                        title = "CÁMARA (CAMERA)",
                        description = "Utilizado exclusivamente para fotografiar recetas médicas físicas y en la función voluntaria de grabación de emergencia. Nunca opera en segundo plano."
                    )
                    PermissionDetailRow(
                        icon = Icons.Default.Mic,
                        title = "MICRÓFONO (RECORD_AUDIO)",
                        description = "Utilizado únicamente durante la grabación voluntaria en la pantalla de emergencia para registrar audio ambiental. Cero escucha pasiva."
                    )
                    PermissionDetailRow(
                        icon = Icons.Default.Fingerprint,
                        title = "BIOMETRÍA (USE_BIOMETRIC)",
                        description = "Autenticación local en el dispositivo (BiometricPrompt) para proteger la confidencialidad de la ficha y recetas médicas contra terceros."
                    )
                    PermissionDetailRow(
                        icon = Icons.Default.Security,
                        title = "PROTECCIÓN DE PANTALLA (FLAG_SECURE)",
                        description = "Impide capturas accidentales o extracción por malware espía en pantallas con recetas médicas que contengan RUT y datos del médico."
                    )
                    PermissionDetailRow(
                        icon = Icons.Default.Lock,
                        title = "ALMACENAMIENTO SANDBOX AISLADO",
                        description = "Todos los documentos y recetas se almacenan en el sandbox aislado de la app cifrados con EncryptedFile (AES-256). No se accede al almacenamiento compartido del teléfono sin SAF."
                    )
                }
            }
        }

        // Privacy and Data Protection Compliance (HIPAA, GDPR, Ley 19.628, Ley 20.584)
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
                            text = "Privacidad y Normativas Sanitarias de Datos",
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
                        subtitle = "Derechos y deberes de los pacientes: reserva inviolable de la ficha clínica."
                    )
                    ComplianceRow(
                        title = "HIPAA Security Rule (EE.UU. - 45 CFR § 164.312)",
                        subtitle = "Cifrado AES-256-GCM para datos médicos en tránsito y respaldos."
                    )
                    ComplianceRow(
                        title = "GDPR Artículos 9, 17 y 32 (Unión Europea)",
                        subtitle = "Tratamiento especial de categorías de datos de salud, almacenamiento Zero-Knowledge y derecho al olvido."
                    )

                    Button(
                        onClick = { showPrivacyPolicyDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Leer Política de Privacidad Completa", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Security Controls Card (Biometrics & Anti-Screenshot)
        item {
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
                            Text("Bloqueo Biométrico al Iniciar (BiometricPrompt)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Requiere huella dactilar, reconocimiento facial o PIN al abrir la app.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = {
                                biometricEnabled = it
                                secPrefs.isBiometricLockEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }

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
                        Switch(
                            checked = screenshotProtEnabled,
                            onCheckedChange = {
                                screenshotProtEnabled = it
                                secPrefs.isScreenshotProtectionEnabled = it
                                onToggleScreenshotProtection(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }

        // GOOGLE PLAY DATA SAFETY: Right to be Forgotten / Complete Data Wipe
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(
                            text = "Derecho al Olvido y Eliminación Total de Datos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Text(
                        text = "Conforme a los requerimientos de Data Safety de Google Play y GDPR Art. 17, puedes eliminar de forma permanente e irrecuperable todas tus tomas, recetas médicas, fotos y archivos locales de este dispositivo.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_all_patient_data_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eliminar Todos Mis Datos de Salud", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Premium Subscription & AdMob Management Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isPremium) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isPremium) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHighest),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = if (isPremium) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isPremium) "CannaMed Premium Activo" else "CannaMed con Anuncios (AdMob)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPremium) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isPremium) "Experiencia limpia sin anuncios y funciones avanzadas" else "Publicidad certificada en salud activa",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isPremium,
                            onCheckedChange = {
                                isPremium = it
                                secPrefs.isPremium = it
                                Toast.makeText(
                                    context,
                                    if (it) "¡Modo Premium activado! Anuncios AdMob ocultados" else "Modo Estándar con Anuncios activado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Garantía de Privacidad en Publicidad:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldPrimary
                            )
                            Text("• Cumplimiento estricto de Google AdMob: Los anuncios promocionan exclusivamente telemedicina, pastilleros inteligentes, purificadores y dispositivos médicos generales.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("• Cero publicidad de parafernalia, tabaco, alcohol ni venta de sustancias.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("• Las recetas médicas y la pantalla de emergencia NUNCA muestran anuncios en ningún modo.", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = EmeraldSecondary)
                        }
                    }
                }
            }
        }

        // Software Credits & Co-authorship Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "Créditos y Equipo de Desarrollo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Desarrollo Colaborativo y Arquitectura de Seguridad",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldSecondary
                            )
                        }
                    }

                    // Author 1: VladTepescL
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("VT", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "VladTepescL",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Perfil de GitHub: github.com/VladTepescL",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Fundador, Arquitecto de Software y Especialista en Farmacología Cannábica Terapéutica.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Author 2: AI Studio Coding Engine
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("AI", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AI Studio Coding Engine",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Co-desarrollador de Ingeniería de Software",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Implementación en Jetpack Compose, Seguridad Criptográfica Zero-Knowledge, Biometría Android y Visualización Gráfica.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Confirmation dialog for complete data erasure (Google Play Right to be Forgotten)
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "¿Eliminar definitivamente todos tus datos?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "Esta acción borrará de forma irreversible todas tus dosis registradas, recetas médicas, fotos de documentos y configuraciones personales. Se restaurará la aplicación a su estado inicial de fábrica en cumplimiento del derecho al olvido.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onClearAllData?.invoke()
                        secPrefs.clearAllPreferences()
                        Toast.makeText(context, "Todos los datos han sido eliminados de forma segura.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, Borrar Todo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // In-App Complete Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        PrivacyPolicyModal(onDismiss = { showPrivacyPolicyDialog = false })
    }

    if (showIdentityCredentialModal) {
        PatientIdentityCredentialModal(onDismiss = { showIdentityCredentialModal = false })
    }
}

@Composable
private fun EmergencyCallButton(
    modifier: Modifier = Modifier,
    label: String,
    phone: String,
    context: Context
) {
    OutlinedButton(
        onClick = {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                context.startActivity(intent)
            } catch (_: Exception) {}
        },
        modifier = modifier,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PermissionDetailRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DisclaimerCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
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

@Composable
private fun PrivacyPolicyModal(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxSize(0.92f)
                .clip(RoundedCornerShape(24.dp)),
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Policy, contentDescription = null, tint = EmeraldPrimary)
                        Text("Política de Privacidad CannaMed", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Última actualización: Noviembre 2026 • Versión 2.5", style = MaterialTheme.typography.labelSmall, color = EmeraldSecondary)

                    Text("1. Principio de Soberanía y Almacenamiento Local (Zero-Knowledge)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("CannaMed no recopila, no comercializa ni transmite datos médicos a servidores externos ni redes publicitarias. Toda la información de dosis, recetas, fotos y bitácora clínica se almacena exclusivamente de forma local en la memoria protegida del dispositivo bajo aislamiento sandbox de Android y cifrado EncryptedFile (AES-256-GCM).", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Text("2. Prohibición de Comercialización y Cumplimiento Google Play", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("Esta aplicación es estrictamente una herramienta informativa de dosificación y salud personal. En cumplimiento de las políticas de Google Play, la aplicación no vende, no facilita la venta, no despacha ni procesa pagos por cannabis ni sustancias estupefacientes o psicotrópicas.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Text("3. Uso Exclusivo de Permisos de Dispositivo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("• Cámara y Micrófono: Operan únicamente bajo activación consciente del usuario para el escaneo de recetas o la función voluntaria de grabación de emergencia ante fiscalizaciones. No existe grabación en segundo plano.\n• Biometría: Utilizada estrictamente en el subsistema local BiometricPrompt de Android sin salir del procesador seguro (TEE).\n• Ventana FLAG_SECURE: Previene capturas involuntarias de recetas médicas con información confidencial.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Text("4. Derecho al Olvido y Supresión Total (GDPR Art. 17 / Ley 19.628)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("El usuario tiene derecho inalienable a borrar todos sus registros médicos en cualquier momento mediante el botón 'Eliminar Todos Mis Datos de Salud' dentro de la pestaña Legal. Dicha acción destruye de forma irreversible todas las tablas y archivos de la base de datos local.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Text("5. Publicidad Conforme a Políticas Google AdMob", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("Los anuncios integrados promocionan exclusivamente servicios de salud certificados (telemedicina, monitores clínicos, purificadores y dispositivos médicos generales). Está terminantemente prohibida la publicidad de parafernalia, vaporizadores para drogas o venta de sustancias.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Entendido y Aceptado")
                    }
                }
            }
        }
    }
}
