package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.foundation.border
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CannaMedViewModel
import com.example.ui.components.BiometricAnimatedLoopBackground
import com.example.ui.components.SimulatedAdMobInterstitialDialog
import com.example.ui.screens.BackupDialog
import com.example.ui.screens.CartridgeManagerScreen
import com.example.ui.screens.DoctorReportScreen
import com.example.ui.screens.DosageJournalScreen
import com.example.ui.screens.DoseWizardScreen
import com.example.ui.screens.EmergencyRecorderScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LegalDisclaimerScreen
import com.example.ui.screens.LegalFrameworkScreen
import com.example.ui.screens.MockCannabisAiScreen
import com.example.ui.screens.PrescriptionsScreen
import com.example.ui.screens.StrainsLiteratureScreen
import com.example.ui.theme.CannaMedTheme
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary

import android.view.WindowManager
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.FragmentActivity
import com.example.util.BiometricAuthHelper
import com.example.util.SecurityPreferences

data class NavTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : FragmentActivity() {

    private lateinit var securityPrefs: SecurityPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        securityPrefs = SecurityPreferences(this)

        // Apply anti-screenshot protection if enabled
        applyScreenshotProtection(securityPrefs.isScreenshotProtectionEnabled)

        enableEdgeToEdge()
        setContent {
            CannaMedTheme {
                var isBiometricAuthenticated by remember {
                    mutableStateOf(!securityPrefs.isBiometricLockEnabled)
                }
                var biometricError by remember { mutableStateOf<String?>(null) }
                val context = this@MainActivity

                val triggerBiometricPrompt = {
                    BiometricAuthHelper.promptBiometric(
                        activity = context,
                        onSuccess = {
                            isBiometricAuthenticated = true
                            biometricError = null
                        },
                        onError = { err ->
                            biometricError = err
                        }
                    )
                }

                LaunchedEffect(Unit) {
                    if (securityPrefs.isBiometricLockEnabled) {
                        triggerBiometricPrompt()
                    }
                }

                if (!isBiometricAuthenticated) {
                    BiometricLockOverlay(
                        errorMessage = biometricError,
                        onRetry = triggerBiometricPrompt,
                        onBypassWithClinicalPin = {
                            isBiometricAuthenticated = true
                            biometricError = null
                        }
                    )
                } else {
                    CannaMedMainApp(
                        onToggleScreenshotProtection = { enabled ->
                            applyScreenshotProtection(enabled)
                        },
                        onLockApp = {
                            isBiometricAuthenticated = false
                            biometricError = null
                        }
                    )
                }
            }
        }
    }

    private fun applyScreenshotProtection(enabled: Boolean) {
        if (enabled) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

@Composable
fun BiometricLockOverlay(
    errorMessage: String?,
    onRetry: () -> Unit,
    onBypassWithClinicalPin: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    val biometricStatusDesc = remember { BiometricAuthHelper.getBiometricStatusDescription(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07100B)),
        contentAlignment = Alignment.Center
    ) {
        // Continuous animated loop background (pulses, waves, and floating cannabinoid particles)
        BiometricAnimatedLoopBackground()

        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
            ),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.15f))
                        .border(2.dp, EmeraldPrimary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = "Autenticación Biométrica",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Text(
                    text = "CannaMed Seguro",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Protección Biométrica Activa (BiometricPrompt)",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "Para resguardar tus recetas médicas, dosis y datos de salud sensibles conforme a normativas de privacidad clínica, autentícate para ingresar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Device biometric status badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = biometricStatusDesc,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("biometric_unlock_button")
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Desbloquear con Biometría", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showPinDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("biometric_pin_fallback_button")
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Desbloquear con PIN de Seguridad")
                }
            }
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("PIN de Seguridad Médico", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Introduce el PIN de respaldo o clave de paciente (por defecto: 1234):",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { enteredPin = it },
                        label = { Text("PIN (4 dígitos)") },
                        singleLine = true,
                        isError = pinError != null,
                        supportingText = pinError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredPin == "1234" || enteredPin.length >= 4) {
                            showPinDialog = false
                            onBypassWithClinicalPin()
                        } else {
                            pinError = "PIN incorrecto. Ingresa al menos 4 dígitos (p. ej. 1234)."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CannaMedMainApp(
    viewModel: CannaMedViewModel = viewModel(),
    onToggleScreenshotProtection: (Boolean) -> Unit = {},
    onLockApp: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val securityPrefs = remember { SecurityPreferences(context) }
    var isPremium by remember { mutableStateOf(securityPrefs.isPremium) }
    var showInterstitialAfterDose by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showDoseWizard by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showEmergencyRecorder by remember { mutableStateOf(false) }
    var doctorSubTab by remember { mutableIntStateOf(0) } // 0: Informe, 1: Recetas, 2: Marco Legal

    val dosageLogs by viewModel.dosageLogs.collectAsStateWithLifecycle()
    val activeCartridges by viewModel.activeCartridges.collectAsStateWithLifecycle()
    val allCartridges by viewModel.allCartridges.collectAsStateWithLifecycle()
    val prescriptions by viewModel.prescriptions.collectAsStateWithLifecycle()
    val strains by viewModel.strains.collectAsStateWithLifecycle()
    val strainNamesList by viewModel.strainNamesList.collectAsStateWithLifecycle()
    val allLegalExtracts by viewModel.allLegalExtracts.collectAsStateWithLifecycle()
    val dailyGramsProgress by viewModel.dailyGramsProgress.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchStrainQuery.collectAsStateWithLifecycle()
    val quickStats by viewModel.quickStats.collectAsStateWithLifecycle()

    val navItems = listOf(
        NavTabItem("Toma", Icons.Default.MedicalServices, "nav_home_toma"),
        NavTabItem("Diario", Icons.Default.Book, "nav_journal"),
        NavTabItem("Pods", Icons.Default.ElectricBolt, "nav_cartridges"),
        NavTabItem("Cepas", Icons.Default.Spa, "nav_strains"),
        NavTabItem("Asesor", Icons.Default.Psychology, "nav_mock_ai"),
        NavTabItem("Médico", Icons.Default.LocalHospital, "nav_doctor"),
        NavTabItem("Legal", Icons.Default.Shield, "nav_disclaimer")
    )

    if (showDoseWizard) {
        DoseWizardScreen(
            activeCartridges = activeCartridges,
            strains = strains,
            dailyGramsProgress = dailyGramsProgress,
            onClose = { showDoseWizard = false },
            onSaveLog = { log ->
                viewModel.saveDosageLog(log)
                showDoseWizard = false
                isPremium = securityPrefs.isPremium
                if (!isPremium) {
                    showInterstitialAfterDose = true
                }
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Row {
                                Text(
                                    text = "Canna",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Med",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onLockApp,
                            modifier = Modifier.testTag("top_app_bar_lock_button")
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Bloquear Aplicación",
                                tint = EmeraldPrimary
                            )
                        }
                        IconButton(
                            onClick = { showEmergencyRecorder = true },
                            modifier = Modifier.testTag("top_app_bar_emergency_button")
                        ) {
                            Icon(
                                Icons.Default.Videocam,
                                contentDescription = "Grabación de Emergencia",
                                tint = Color(0xFFFF5252)
                            )
                        }
                        IconButton(
                            onClick = { showBackupDialog = true },
                            modifier = Modifier.testTag("top_app_bar_backup_button")
                        ) {
                            Icon(
                                Icons.Default.Backup,
                                contentDescription = "Copia de Seguridad",
                                tint = EmeraldPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 8.dp
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = EmeraldPrimary,
                                indicatorColor = EmeraldPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    // TAB 0: PAGINA PRINCIPAL (SOLO TOMA DE DOSIS CON BOTON GRANDE + CONTADOR RETROCESO)
                    0 -> HomeScreen(
                        latestLog = dosageLogs.firstOrNull(),
                        activeCartridge = activeCartridges.firstOrNull(),
                        quickStats = quickStats,
                        dailyGramsProgress = dailyGramsProgress,
                        onStartDoseWizard = { showDoseWizard = true },
                        onOpenEmergencyRecorder = { showEmergencyRecorder = true }
                    )

                    // TAB 1: DIARIO DE TOMAS (SIN BOTON DUPLICADO, METRICA DE GRAMOS RESTANTES)
                    1 -> DosageJournalScreen(
                        logs = dosageLogs,
                        quickStats = quickStats,
                        dailyGramsProgress = dailyGramsProgress,
                        onAddLogClick = { showDoseWizard = true },
                        onDeleteLog = { viewModel.deleteDosageLog(it) }
                    )

                    // TAB 2: CLEAR PODS Y TERPENOS (FORMULARIO AGREGAR ARRIBA + LISTA COMPACTA DE ABIERTOS)
                    2 -> CartridgeManagerScreen(
                        activeCartridges = activeCartridges,
                        allCartridges = allCartridges,
                        onSaveCartridge = { viewModel.saveCartridge(it) },
                        onAddPuffs = { id, puffs -> viewModel.addPuffsToCartridge(id, puffs) },
                        onMarkFinished = { viewModel.markCartridgeFinished(it) }
                    )

                    // TAB 3: CEPAS Y LEAFLY (AUTOCOMPLETADO EN MEMORIA + 2X2 TIPOS Y DOLENCIAS)
                    3 -> StrainsLiteratureScreen(
                        strains = strains,
                        strainNamesList = strainNamesList,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.onStrainSearchQueryChanged(it) },
                        onSaveCustomStrain = { viewModel.saveStrain(it) }
                    )

                    // TAB 4: ASESOR CLÍNICO OFFLINE (MOCK SIN API KEY DE GOOGLE)
                    4 -> MockCannabisAiScreen()

                    // TAB 5: ÁREA MÉDICA (INFORME, RECETAS Y MARCO JURÍDICO LEY 20.000 CHILE)
                    5 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            ScrollableTabRow(
                                selectedTabIndex = doctorSubTab,
                                edgePadding = 12.dp,
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = EmeraldPrimary
                            ) {
                                Tab(
                                    selected = doctorSubTab == 0,
                                    onClick = { doctorSubTab = 0 },
                                    text = { Text("Informe Médico", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                )
                                Tab(
                                    selected = doctorSubTab == 1,
                                    onClick = { doctorSubTab = 1 },
                                    text = { Text("Recetas & Calendario", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                )
                                Tab(
                                    selected = doctorSubTab == 2,
                                    onClick = { doctorSubTab = 2 },
                                    text = { Text("Marco Jurídico (Chile)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                )
                            }

                            when (doctorSubTab) {
                                0 -> DoctorReportScreen(
                                    logs = dosageLogs,
                                    cartridges = allCartridges,
                                    prescriptions = prescriptions,
                                    onGenerateReport = { patient, doctor, months, dSummary, rCharts, fBreakdown, cPods, sNotes, pres ->
                                        viewModel.generateDoctorReport(
                                            patientName = patient,
                                            doctorName = doctor,
                                            monthsPeriod = months,
                                            includeDoseSummary = dSummary,
                                            includeReliefAndCharts = rCharts,
                                            includeFormatsBreakdown = fBreakdown,
                                            includeClearPodsAndTerpenes = cPods,
                                            includeSubjectiveNotes = sNotes,
                                            includePrescriptions = pres
                                        )
                                    }
                                )
                                1 -> PrescriptionsScreen(
                                    prescriptions = prescriptions,
                                    onSavePrescription = { pres, uri, name ->
                                        viewModel.savePrescription(pres, uri, name)
                                    },
                                    onDeletePrescription = { viewModel.deletePrescription(it) }
                                )
                                2 -> LegalFrameworkScreen(
                                    legalExtracts = allLegalExtracts,
                                    activePrescriptions = prescriptions,
                                    onSaveLegalExtract = { viewModel.saveLegalExtract(it) },
                                    onDeleteLegalExtract = { viewModel.deleteLegalExtract(it) }
                                )
                            }
                        }
                    }

                    // TAB 6: CONTROL DE VERSIÓN, CRÉDITOS, SUSCRIPCIÓN PREMIUM Y NORMATIVAS DE SEGURIDAD
                    6 -> LegalDisclaimerScreen(
                        onToggleScreenshotProtection = onToggleScreenshotProtection
                    )
                }
            }
        }
    }

    if (showEmergencyRecorder) {
        EmergencyRecorderScreen(
            onClose = { showEmergencyRecorder = false }
        )
    }

    if (showBackupDialog) {
        BackupDialog(
            onDismiss = { showBackupDialog = false },
            onExportBackup = { viewModel.exportBackupJson() },
            onRestoreBackup = { json -> viewModel.restoreBackupJson(json) }
        )
    }

    if (showInterstitialAfterDose && !isPremium) {
        SimulatedAdMobInterstitialDialog(
            isPremium = isPremium,
            actionContext = "Registro de Dosis Terapéutica Guardado",
            onDismiss = { showInterstitialAfterDose = false }
        )
    }
}
