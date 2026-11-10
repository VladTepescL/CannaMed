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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Videocam
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
import com.example.ui.screens.BackupDialog
import com.example.ui.screens.CartridgeManagerScreen
import com.example.ui.screens.DoctorReportScreen
import com.example.ui.screens.DosageJournalScreen
import com.example.ui.screens.DoseWizardScreen
import com.example.ui.screens.EmergencyRecorderScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LegalDisclaimerScreen
import com.example.ui.screens.LegalFrameworkScreen
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
                    mutableStateOf(!securityPrefs.isBiometricLockEnabled || !BiometricAuthHelper.canAuthenticate(this@MainActivity))
                }
                var biometricError by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    if (securityPrefs.isBiometricLockEnabled && BiometricAuthHelper.canAuthenticate(this@MainActivity)) {
                        BiometricAuthHelper.promptBiometric(
                            activity = this@MainActivity,
                            onSuccess = {
                                isBiometricAuthenticated = true
                                biometricError = null
                            },
                            onError = { err ->
                                biometricError = err
                            }
                        )
                    }
                }

                if (!isBiometricAuthenticated) {
                    BiometricLockOverlay(
                        errorMessage = biometricError,
                        onRetry = {
                            BiometricAuthHelper.promptBiometric(
                                activity = this@MainActivity,
                                onSuccess = {
                                    isBiometricAuthenticated = true
                                    biometricError = null
                                },
                                onError = { err ->
                                    biometricError = err
                                }
                            )
                        }
                    )
                } else {
                    CannaMedMainApp(
                        onToggleScreenshotProtection = { enabled ->
                            applyScreenshotProtection(enabled)
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
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Text(
                text = "CannaMed Seguro",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Acceso protegido a registros médicos de cannabis. Utiliza tu huella dactilar, reconocimiento facial o PIN del dispositivo para ingresar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            errorMessage?.let { err ->
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Desbloquear con Biometría", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CannaMedMainApp(
    viewModel: CannaMedViewModel = viewModel(),
    onToggleScreenshotProtection: (Boolean) -> Unit = {}
) {
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
        NavTabItem("Clear Pods", Icons.Default.ElectricBolt, "nav_cartridges"),
        NavTabItem("Cepas", Icons.Default.Spa, "nav_strains"),
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

                    // TAB 4: ÁREA MÉDICA (INFORME, RECETAS Y MARCO JURÍDICO LEY 20.000 CHILE)
                    4 -> {
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

                    // TAB 5: CONTROL DE VERSIÓN, DESCARGO DE RESPONSABILIDAD Y NORMATIVAS DE SEGURIDAD
                    5 -> LegalDisclaimerScreen(
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
}
