package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.CannaMedApp
import com.example.data.model.Cartridge
import com.example.data.model.DosageLog
import com.example.data.model.DoseEquivalenceCalculator
import com.example.data.model.LegalExtract
import com.example.data.model.Prescription
import com.example.data.model.PuffDuration
import com.example.data.model.StrainInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class QuickStats(
    val dosesToday: Int = 0,
    val puffsToday: Int = 0,
    val averageReliefToday: Double = 0.0,
    val mostUsedFormat: String = "Vaporizado"
)

data class DailyGramsProgress(
    val prescribedGrams: Double = 3.0,
    val consumedGrams: Double = 0.40,
    val remainingGrams: Double = 2.60
)

class CannaMedViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as CannaMedApp).repository

    val dosageLogs: StateFlow<List<DosageLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeCartridges: StateFlow<List<Cartridge>> = repository.activeCartridges
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCartridges: StateFlow<List<Cartridge>> = repository.allCartridges
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val prescriptions: StateFlow<List<Prescription>> = repository.allPrescriptions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allLegalExtracts: StateFlow<List<LegalExtract>> = repository.allLegalExtracts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchStrainQuery = MutableStateFlow("")
    val searchStrainQuery = _searchStrainQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val strains: StateFlow<List<StrainInfo>> = _searchStrainQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allStrains
            else repository.searchStrains(query.trim())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // In-memory list of strain names for instant autocompletion
    val strainNamesList: StateFlow<List<String>> = strains.map { list ->
        list.map { it.name }.distinct()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = listOf(
            "Charlotte's Web", "Harlequin", "ACDC", "Granddaddy Purple",
            "Blue Dream", "OG Kush", "Cannatonic", "Jack Herer", "Northern Lights"
        )
    )

    // Daily countdown gauge: Prescribed Grams vs Consumed Grams today (basado en flor seca equivalente)
    val dailyGramsProgress: StateFlow<DailyGramsProgress> = dosageLogs.combine(prescriptions) { logs, presList ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        val todayLogs = logs.filter { it.timestamp >= startOfToday }

        // Sum consumed grams in dried flower equivalence across ALL formats
        var consumedG = 0.0
        todayLogs.forEach { log ->
            if (log.equivalentGramsDriedFlower > 0.0) {
                consumedG += log.equivalentGramsDriedFlower
            } else {
                val fallback = DoseEquivalenceCalculator.calculateEquivalentDriedFlowerGrams(
                    format = log.format,
                    dosageValue = log.dosageValue,
                    puffsCount = log.puffsCount,
                    puffDuration = PuffDuration.fromId(log.puffIntensity)
                )
                consumedG += fallback
            }
        }
        val roundedConsumed = (Math.round(consumedG * 1000.0) / 1000.0)

        // Prescribed daily grams from active prescription
        val prescribedG = presList.firstOrNull()?.prescribedDailyGrams ?: 3.0
        val remaining = (Math.round((prescribedG - roundedConsumed) * 1000.0) / 1000.0).coerceAtLeast(0.0)

        DailyGramsProgress(
            prescribedGrams = prescribedG,
            consumedGrams = roundedConsumed,
            remainingGrams = remaining
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyGramsProgress(3.0, 0.40, 2.60)
    )

    val quickStats: StateFlow<QuickStats> = dosageLogs.combine(activeCartridges) { logs, _ ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        val todayLogs = logs.filter { it.timestamp >= startOfToday }
        val dosesCount = todayLogs.size
        val puffs = todayLogs.sumOf { it.puffsCount }
        val avgRelief = if (todayLogs.isNotEmpty()) todayLogs.map { it.reliefScore }.average() else 0.0
        val topFormat = todayLogs.groupBy { it.format.displayName }
            .maxByOrNull { it.value.size }?.key ?: (logs.firstOrNull()?.format?.displayName ?: "Vaporizado")

        QuickStats(
            dosesToday = dosesCount,
            puffsToday = puffs,
            averageReliefToday = avgRelief,
            mostUsedFormat = topFormat
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuickStats()
    )

    fun onStrainSearchQueryChanged(newQuery: String) {
        _searchStrainQuery.value = newQuery
    }

    fun saveDosageLog(log: DosageLog) {
        viewModelScope.launch {
            repository.insertLog(log)
        }
    }

    fun deleteDosageLog(log: DosageLog) {
        viewModelScope.launch {
            repository.deleteLog(log)
        }
    }

    fun saveCartridge(cartridge: Cartridge) {
        viewModelScope.launch {
            repository.insertCartridge(cartridge)
        }
    }

    fun updateCartridge(cartridge: Cartridge) {
        viewModelScope.launch {
            repository.updateCartridge(cartridge)
        }
    }

    fun addPuffsToCartridge(cartridgeId: Long, count: Int) {
        viewModelScope.launch {
            repository.addPuffsToCartridge(cartridgeId, count)
        }
    }

    fun markCartridgeFinished(cartridgeId: Long) {
        viewModelScope.launch {
            repository.markCartridgeFinished(cartridgeId)
        }
    }

    fun deleteCartridge(cartridge: Cartridge) {
        viewModelScope.launch {
            repository.deleteCartridge(cartridge)
        }
    }

    fun savePrescription(
        prescription: Prescription,
        documentUri: Uri?,
        originalFileName: String?
    ) {
        viewModelScope.launch {
            var finalPrescription = prescription
            if (documentUri != null && originalFileName != null) {
                val saved = repository.saveDocumentLocally(documentUri, originalFileName)
                if (saved != null) {
                    finalPrescription = prescription.copy(
                        localDocumentPath = saved.first,
                        documentMimeType = saved.second,
                        documentFileName = originalFileName
                    )
                }
            }
            repository.insertPrescription(finalPrescription)
        }
    }

    fun deletePrescription(prescription: Prescription) {
        viewModelScope.launch {
            repository.deletePrescription(prescription)
        }
    }

    fun saveStrain(strain: StrainInfo) {
        viewModelScope.launch {
            repository.insertStrain(strain)
        }
    }

    fun saveLegalExtract(extract: LegalExtract) {
        viewModelScope.launch {
            repository.insertLegalExtract(extract)
        }
    }

    fun deleteLegalExtract(extract: LegalExtract) {
        viewModelScope.launch {
            repository.deleteLegalExtract(extract)
        }
    }

    suspend fun exportBackupJson(): String {
        return repository.generateBackupJson()
    }

    suspend fun restoreBackupJson(json: String): Boolean {
        return repository.restoreBackupJson(json)
    }

    fun generateDoctorReport(
        patientName: String,
        doctorName: String,
        monthsPeriod: Int = 3,
        includeDoseSummary: Boolean = true,
        includeReliefAndCharts: Boolean = true,
        includeFormatsBreakdown: Boolean = true,
        includeClearPodsAndTerpenes: Boolean = true,
        includeSubjectiveNotes: Boolean = true,
        includePrescriptions: Boolean = true
    ): String {
        return repository.generateDoctorReportText(
            patientName = patientName,
            doctorName = doctorName,
            monthsPeriod = monthsPeriod,
            includeDoseSummary = includeDoseSummary,
            includeReliefAndCharts = includeReliefAndCharts,
            includeFormatsBreakdown = includeFormatsBreakdown,
            includeClearPodsAndTerpenes = includeClearPodsAndTerpenes,
            includeSubjectiveNotes = includeSubjectiveNotes,
            includePrescriptions = includePrescriptions,
            logs = dosageLogs.value,
            cartridges = allCartridges.value,
            prescriptions = prescriptions.value
        )
    }
}
