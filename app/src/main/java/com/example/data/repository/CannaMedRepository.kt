package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.AppDatabase
import com.example.data.model.Cartridge
import com.example.data.model.DosageLog
import com.example.data.model.LegalExtract
import com.example.data.model.Prescription
import com.example.data.model.StrainInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CannaMedRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    private val dosageDao = database.dosageLogDao()
    private val cartridgeDao = database.cartridgeDao()
    private val prescriptionDao = database.prescriptionDao()
    private val strainDao = database.strainDao()
    private val legalDao = database.legalExtractDao()

    // Legal Extracts
    val allLegalExtracts: Flow<List<LegalExtract>> = legalDao.getAllLegalExtracts()

    suspend fun insertLegalExtract(extract: LegalExtract): Long = withContext(Dispatchers.IO) {
        legalDao.insert(extract)
    }

    suspend fun deleteLegalExtract(extract: LegalExtract) = withContext(Dispatchers.IO) {
        legalDao.delete(extract)
    }

    // Full Backup & Restore
    private val backupManager = BackupManager(context, database)

    suspend fun generateBackupJson(): String = backupManager.createFullBackupJson()
    suspend fun restoreBackupJson(json: String): Boolean = backupManager.restoreFromBackupJson(json)

    // Logs
    val allLogs: Flow<List<DosageLog>> = dosageDao.getAllLogs()

    suspend fun insertLog(log: DosageLog): Long = withContext(Dispatchers.IO) {
        val id = dosageDao.insert(log)
        // If connected to a cartridge and puffs > 0, increment total puffs on cartridge
        if (log.cartridgeId != null && log.puffsCount > 0) {
            cartridgeDao.addPuffs(log.cartridgeId, log.puffsCount)
        }
        id
    }

    suspend fun deleteLog(log: DosageLog) = withContext(Dispatchers.IO) {
        dosageDao.delete(log)
    }

    // Cartridges & Clear Oil
    val allCartridges: Flow<List<Cartridge>> = cartridgeDao.getAllCartridges()
    val activeCartridges: Flow<List<Cartridge>> = cartridgeDao.getActiveCartridges()

    suspend fun insertCartridge(cartridge: Cartridge): Long = withContext(Dispatchers.IO) {
        cartridgeDao.insert(cartridge)
    }

    suspend fun updateCartridge(cartridge: Cartridge) = withContext(Dispatchers.IO) {
        cartridgeDao.update(cartridge)
    }

    suspend fun addPuffsToCartridge(cartridgeId: Long, count: Int) = withContext(Dispatchers.IO) {
        cartridgeDao.addPuffs(cartridgeId, count)
    }

    suspend fun markCartridgeFinished(cartridgeId: Long) = withContext(Dispatchers.IO) {
        cartridgeDao.markFinished(cartridgeId, System.currentTimeMillis())
    }

    suspend fun deleteCartridge(cartridge: Cartridge) = withContext(Dispatchers.IO) {
        cartridgeDao.delete(cartridge)
    }

    // Prescriptions
    val allPrescriptions: Flow<List<Prescription>> = prescriptionDao.getAllPrescriptions()

    suspend fun insertPrescription(prescription: Prescription): Long = withContext(Dispatchers.IO) {
        prescriptionDao.insert(prescription)
    }

    suspend fun deletePrescription(prescription: Prescription) = withContext(Dispatchers.IO) {
        // Also clean up local file if stored
        prescription.localDocumentPath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) file.delete()
            } catch (_: Exception) {}
        }
        prescriptionDao.delete(prescription)
    }

    // Strains
    val allStrains: Flow<List<StrainInfo>> = strainDao.getAllStrains()

    fun searchStrains(query: String): Flow<List<StrainInfo>> = strainDao.searchStrains(query)

    suspend fun insertStrain(strain: StrainInfo): Long = withContext(Dispatchers.IO) {
        strainDao.insert(strain)
    }

    // Save document file securely into hardware-backed encrypted app sandbox (EncryptedFile AES-256)
    suspend fun saveDocumentLocally(sourceUri: Uri, originalName: String): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val extension = originalName.substringAfterLast('.', "dat")
            val fileName = "doc_${System.currentTimeMillis()}.$extension"

            val mime = context.contentResolver.getType(sourceUri) ?: when (extension.lowercase(Locale.ROOT)) {
                "pdf" -> "application/pdf"
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                else -> "application/octet-stream"
            }

            val savedFile = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                com.example.util.EncryptedFileManager.saveEncryptedFile(context, input, fileName)
            }

            if (savedFile != null) {
                Pair(savedFile.absolutePath, mime)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Generate clinical medical report text with time period and selectable data
    fun generateDoctorReportText(
        patientName: String,
        doctorName: String,
        monthsPeriod: Int, // 1, 3, 6, 12 or 0 for all
        includeDoseSummary: Boolean = true,
        includeReliefAndCharts: Boolean = true,
        includeFormatsBreakdown: Boolean = true,
        includeClearPodsAndTerpenes: Boolean = true,
        includeSubjectiveNotes: Boolean = true,
        includePrescriptions: Boolean = true,
        logs: List<DosageLog>,
        cartridges: List<Cartridge>,
        prescriptions: List<Prescription>
    ): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sb = StringBuilder()

        val periodCutoff = if (monthsPeriod > 0) {
            System.currentTimeMillis() - (monthsPeriod.toLong() * 30L * 24 * 60 * 60 * 1000)
        } else 0L

        val filteredLogs = logs.filter { it.timestamp >= periodCutoff }

        sb.appendLine("==================================================")
        sb.appendLine("           INFORME MÉDICO DE SEGUIMIENTO          ")
        sb.appendLine("         TERAPIA CON CANNABIS MEDICINAL           ")
        sb.appendLine("==================================================")
        sb.appendLine("Generado: ${dateFormat.format(Date())}")
        sb.appendLine("Paciente: ${patientName.ifBlank { "Paciente Registrado" }}")
        if (doctorName.isNotBlank()) {
            sb.appendLine("Médico Tratante: $doctorName")
        }
        val periodLabel = if (monthsPeriod > 0) "Últimos $monthsPeriod meses" else "Historial completo"
        sb.appendLine("Período evaluado: $periodLabel")
        sb.appendLine()

        if (includeDoseSummary) {
            sb.appendLine("1. RESUMEN CLÍNICO GENERAL")
            sb.appendLine("--------------------------------------------------")
            val totalDoses = filteredLogs.size
            val avgRelief = if (filteredLogs.isNotEmpty()) filteredLogs.map { it.reliefScore }.average() else 0.0
            val totalPuffs = filteredLogs.sumOf { it.puffsCount }
            val totalEquivalentFlower = filteredLogs.sumOf { it.equivalentGramsDriedFlower }
            val daysInPeriod = (monthsPeriod * 30).coerceAtLeast(1)
            val avgDailyGrams = totalEquivalentFlower / daysInPeriod
            val prescribedDaily = prescriptions.firstOrNull()?.prescribedDailyGrams ?: 3.0

            sb.appendLine("- Total de tomas registradas en período: $totalDoses")
            sb.appendLine("- Alivio promedio reportado (Escala 1-10): ${String.format(Locale.US, "%.1f", avgRelief)} / 10")
            sb.appendLine("- Caladas totales inhaladas: $totalPuffs")
            sb.appendLine("- Consumo total equivalente a flor seca: ${String.format(Locale.US, "%.2f", totalEquivalentFlower)} g")
            sb.appendLine("- Promedio diario consumido: ${String.format(Locale.US, "%.2f", avgDailyGrams)} g/día (Prescrito: ${String.format(Locale.US, "%.2f", prescribedDaily)} g/día)")
            sb.appendLine("- Estándar de cálculo: Health Canada Cannabis Act Schedule 3 & Farmacopea Médica")
            sb.appendLine()
        }

        if (includeFormatsBreakdown) {
            sb.appendLine("2. DESGLOSE POR FORMATO DE ADMINISTRACIÓN")
            sb.appendLine("--------------------------------------------------")
            val formatsCount = filteredLogs.groupBy { it.format.displayName }
            if (formatsCount.isNotEmpty()) {
                formatsCount.forEach { (format, list) ->
                    val formatAvgRelief = list.map { it.reliefScore }.average()
                    sb.appendLine("• $format: ${list.size} tomas (Alivio promedio: ${String.format(Locale.US, "%.1f", formatAvgRelief)}/10)")
                }
            } else {
                sb.appendLine("Sin tomas registradas en este período.")
            }
            sb.appendLine()
        }

        if (includeReliefAndCharts) {
            sb.appendLine("3. SÍNTOMAS TRATADOS Y EFECTIVIDAD CLÍNICA")
            sb.appendLine("--------------------------------------------------")
            val allSymptoms = filteredLogs.flatMap { it.treatedSymptoms.split(",").map { s -> s.trim() } }
                .filter { it.isNotBlank() }
                .groupingBy { it }
                .eachCount()
            if (allSymptoms.isNotEmpty()) {
                allSymptoms.toList().sortedByDescending { it.second }.forEach { (symptom, count) ->
                    sb.appendLine("• $symptom: $count ocasiones registradas")
                }
            } else {
                sb.appendLine("Sin registro de síntomas específicos.")
            }
            sb.appendLine()
        }

        if (includeClearPodsAndTerpenes) {
            sb.appendLine("4. CONTROL DE CARTUCHOS, CLEAR OIL Y TERPENOS")
            sb.appendLine("--------------------------------------------------")
            if (cartridges.isNotEmpty()) {
                cartridges.forEach { cart ->
                    val installDate = dateOnlyFormat.format(Date(cart.installedTimestamp))
                    val status = if (cart.isActive) "Activo / En uso" else "Cambiado / Terminado (${cart.finishedTimestamp?.let { dateOnlyFormat.format(Date(it)) } ?: "-"})"
                    sb.appendLine("• ${cart.brandName} - ${cart.varietyName} [${cart.formatType.displayName}]")
                    sb.appendLine("  - Estado: $status | Fecha de inicio: $installDate")
                    sb.appendLine("  - Composición: THC ${cart.thcPercentage}% (${cart.mgPerMlThc.toInt()} mg/ml) | CBD ${cart.cbdPercentage}% (${cart.mgPerMlCbd.toInt()} mg/ml)")
                    if (cart.cbnPercentage > 0 || cart.cbgPercentage > 0) {
                        sb.appendLine("  - Cannabinoides secundarios: CBN ${cart.cbnPercentage}% | CBG ${cart.cbgPercentage}%")
                    }
                    sb.appendLine("  - Terpenos reportados: ${cart.terpenesSummary}")
                    sb.appendLine("  - Caladas acumuladas en cápsula: ${cart.totalPuffsLogged}")
                }
            } else {
                sb.appendLine("No hay cartuchos registrados.")
            }
            sb.appendLine()
        }

        if (includePrescriptions) {
            sb.appendLine("5. RECETAS MÉDICAS Y CONTROLES VIGENTES")
            sb.appendLine("--------------------------------------------------")
            if (prescriptions.isNotEmpty()) {
                prescriptions.forEach { p ->
                    val issue = dateOnlyFormat.format(Date(p.issueDateMillis))
                    val expiry = dateOnlyFormat.format(Date(p.expiryDateMillis))
                    sb.appendLine("• Dr(a). ${p.doctorName} (Reg/Matrícula: ${p.licenseNumber})")
                    sb.appendLine("  - Centro Médico: ${p.medicalCenter.ifBlank { "N/A" }}")
                    sb.appendLine("  - Emisión: $issue | Próximo control/vencimiento: $expiry")
                    sb.appendLine("  - Pauta: ${p.prescribedFormat} - ${p.prescribedDailyDosage}")
                    sb.appendLine("  - Diagnóstico: ${p.clinicalDiagnosis}")
                }
            } else {
                sb.appendLine("No hay recetas registradas.")
            }
            sb.appendLine()
        }

        if (includeSubjectiveNotes) {
            sb.appendLine("6. REGISTRO CRONOLÓGICO Y NOTAS SUBJETIVAS")
            sb.appendLine("--------------------------------------------------")
            if (filteredLogs.isNotEmpty()) {
                filteredLogs.take(50).forEach { log ->
                    val timeStr = dateFormat.format(Date(log.timestamp))
                    sb.appendLine("[$timeStr] ${log.format.displayName} - ${log.strainName.ifBlank { "Variedad general" }}")
                    val doseStr = if (log.puffsCount > 0) "${log.puffsCount} caladas (${log.puffIntensity})" else "${log.dosageValue} ${log.dosageUnit}"
                    sb.appendLine("  Dosis: $doseStr (Equiv. Flor Seca: ${String.format(Locale.US, "%.3f", log.equivalentGramsDriedFlower)} g) | Alivio: ${log.reliefScore}/10")
                    if (log.treatedSymptoms.isNotBlank()) {
                        sb.appendLine("  Síntomas: ${log.treatedSymptoms}")
                    }
                    if (log.moodEffect.isNotBlank()) {
                        sb.appendLine("  Efecto anímico: ${log.moodEffect}")
                    }
                    if (log.sideEffects.isNotBlank()) {
                        sb.appendLine("  Efectos adversos: ${log.sideEffects}")
                    }
                    if (log.subjectiveNotes.isNotBlank()) {
                        sb.appendLine("  Nota subjetiva: \"${log.subjectiveNotes}\"")
                    }
                    sb.appendLine()
                }
            } else {
                sb.appendLine("Sin tomas registradas en este período.")
            }
        }

        sb.appendLine("==================================================")
        sb.appendLine("Informe generado por CannaMed para consulta médica.")
        sb.appendLine("Datos confidenciales del paciente.")
        return sb.toString()
    }

    /**
     * Wipes all user medical data, complying with Google Play Data Safety
     * Right to be Forgotten (GDPR Art. 17, Ley N° 19.628 Chile, HIPAA).
     */
    suspend fun clearAllUserData(): Unit = withContext(Dispatchers.IO) {
        database.clearAllTables()
        // Re-populate baseline default educational strains and legal extracts
        database.strainDao().insertAll(com.example.data.local.InitialData.DEFAULT_STRAINS)
        database.legalExtractDao().insertAll(com.example.data.local.InitialData.DEFAULT_LEGAL_EXTRACTS)
        // Clean encrypted sandbox files (prescriptions, videos)
        try {
            val filesDir = context.filesDir
            filesDir.listFiles()?.forEach { file ->
                if (file.isDirectory) file.deleteRecursively() else file.delete()
            }
            val cacheDir = context.cacheDir
            cacheDir.listFiles()?.forEach { file ->
                if (file.isDirectory) file.deleteRecursively() else file.delete()
            }
        } catch (_: Exception) {}
    }
}
