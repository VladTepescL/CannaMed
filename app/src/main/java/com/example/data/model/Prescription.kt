package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prescriptions")
data class Prescription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val doctorName: String,
    val licenseNumber: String = "",
    val medicalCenter: String = "",
    val issueDateMillis: Long = System.currentTimeMillis(),
    val expiryDateMillis: Long = System.currentTimeMillis() + (180L * 24 * 60 * 60 * 1000), // 6 months default
    val prescribedDailyGrams: Double = 3.0, // Gramos diarios prescritos (flores secas)
    val prescribedFormat: String = "Flores secas y Extracto sublingual",
    val prescribedDailyDosage: String = "Hasta 3.0g diarios en vaporizado o extracto según dolor",
    val clinicalDiagnosis: String = "Manejo del dolor crónico e insomnio",
    val instructions: String = "Administrar según escala de dolor EVA. Mantener registro diario de consumo.",
    val localDocumentPath: String? = null,
    val documentMimeType: String? = null,
    val documentFileName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
