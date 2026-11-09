package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "legal_extracts")
data class LegalExtract(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val country: String = "Chile",
    val title: String,
    val category: String, // "Ley", "Artículo", "Jurisprudencia", "Protocolo Fiscalización", "Decreto"
    val fullLegalText: String,
    val practicalSummary: String,
    val relevanceForPatient: String,
    val isOfficialPreset: Boolean = true,
    val customNotes: String = ""
)
