package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cartridges")
data class Cartridge(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val brandName: String,
    val varietyName: String,
    val formatType: CannabisFormat = CannabisFormat.CONCENTRATE_VAPE,
    val capacityMl: Float = 1.0f,
    val thcPercentage: Float = 75.0f,
    val cbdPercentage: Float = 2.0f,
    val cbnPercentage: Float = 0.5f,
    val cbgPercentage: Float = 1.0f,
    val terpenesSummary: String = "Mirceno 1.5%, Limoneno 0.9%, Beta-Cariofileno 0.7%",
    val totalPuffsLogged: Int = 0,
    val installedTimestamp: Long = System.currentTimeMillis(),
    val finishedTimestamp: Long? = null,
    val isActive: Boolean = true,
    val batchLotNumber: String = "",
    val notes: String = ""
) {
    val totalCannabinoidsPercentage: Float
        get() = thcPercentage + cbdPercentage + cbnPercentage + cbgPercentage

    val mgPerMlThc: Float
        get() = (thcPercentage / 100f) * 1000f

    val mgPerMlCbd: Float
        get() = (cbdPercentage / 100f) * 1000f
}
