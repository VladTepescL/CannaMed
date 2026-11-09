package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.CannabisFormat

class Converters {
    @TypeConverter
    fun fromCannabisFormat(format: CannabisFormat?): String {
        return format?.name ?: CannabisFormat.VAPORIZED_FLOWER.name
    }

    @TypeConverter
    fun toCannabisFormat(name: String?): CannabisFormat {
        return try {
            if (name != null) CannabisFormat.valueOf(name) else CannabisFormat.VAPORIZED_FLOWER
        } catch (_: Exception) {
            CannabisFormat.VAPORIZED_FLOWER
        }
    }
}
