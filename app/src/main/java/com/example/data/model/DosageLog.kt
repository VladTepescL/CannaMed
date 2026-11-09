package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dosage_logs")
data class DosageLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val format: CannabisFormat = CannabisFormat.VAPORIZED_FLOWER,
    val strainName: String = "",
    val strainType: String = "Híbrida", // "Indica", "Sativa", "Híbrida", "Alto CBD"
    val dosageValue: Double = 3.0,
    val dosageUnit: String = "Caladas",
    val puffsCount: Int = 3,
    val puffIntensity: String = "Estándar (3-4s)", // "Corta (1-2s)", "Estándar (3-4s)", "Larga (5s+)"
    val equivalentGramsDriedFlower: Double = 0.18, // Descuento en gramos de flor seca estándar
    val temperatureCelsius: Int? = null,
    val cartridgeId: Long? = null,
    val thcPercentage: Float = 0f,
    val cbdPercentage: Float = 0f,
    val terpenesNote: String = "",
    val reliefScore: Int = 8, // 1 to 10
    val treatedSymptoms: String = "Dolor crónico, Ansiedad",
    val moodEffect: String = "Relajado",
    val sideEffects: String = "Ninguno",
    val subjectiveNotes: String = "",
    val photoUri: String? = null
)
