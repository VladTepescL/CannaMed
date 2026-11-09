package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strains")
data class StrainInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "Indica", "Sativa", "Híbrida"
    val thcRange: String,
    val cbdRange: String,
    val dominantTerpenes: String,
    val medicalBenefits: String,
    val flavorAroma: String,
    val description: String,
    val leaflySlug: String,
    val leaflyUrl: String = "https://www.leafly.com/strains/$leaflySlug",
    val isCustom: Boolean = false
)
