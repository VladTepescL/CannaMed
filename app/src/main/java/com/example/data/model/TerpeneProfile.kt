package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TerpCaryophyllene
import com.example.ui.theme.TerpHumulene
import com.example.ui.theme.TerpLimonene
import com.example.ui.theme.TerpLinalool
import com.example.ui.theme.TerpMyrcene
import com.example.ui.theme.TerpPinene
import com.example.ui.theme.TerpTerpinolene

data class TerpeneEntry(
    val name: String,
    val percentage: Float, // percentage e.g. 1.2%
    val aroma: String,
    val therapeuticBenefit: String,
    val color: Color
)

object KnownTerpenes {
    val ALL = listOf(
        TerpeneEntry(
            name = "Mirceno",
            percentage = 0f,
            aroma = "Terroso, herbal, clavo",
            therapeuticBenefit = "Sedante, relajante muscular, analgésico",
            color = TerpMyrcene
        ),
        TerpeneEntry(
            name = "Limoneno",
            percentage = 0f,
            aroma = "Cítrico, limón, naranja",
            therapeuticBenefit = "Ansiolítico, antidepresivo, anti-reflujo",
            color = TerpLimonene
        ),
        TerpeneEntry(
            name = "Beta-Cariofileno",
            percentage = 0f,
            aroma = "Pimienta negra, especiado, madera",
            therapeuticBenefit = "Antiinflamatorio potente (CB2), analgésico",
            color = TerpCaryophyllene
        ),
        TerpeneEntry(
            name = "Alfa-Pineno",
            percentage = 0f,
            aroma = "Pino fresco, romero",
            therapeuticBenefit = "Broncodilatador, memoria y lucidez mental",
            color = TerpPinene
        ),
        TerpeneEntry(
            name = "Linalool",
            percentage = 0f,
            aroma = "Lavanda, floral, dulce",
            therapeuticBenefit = "Calmante de ansiedad, anticonvulsivo, sueño",
            color = TerpLinalool
        ),
        TerpeneEntry(
            name = "Terpinoleno",
            percentage = 0f,
            aroma = "Herbal fresco, floral, cítrico",
            therapeuticBenefit = "Antioxidante, sedante leve, antibacteriano",
            color = TerpTerpinolene
        ),
        TerpeneEntry(
            name = "Humuleno",
            percentage = 0f,
            aroma = "Lúpulo, madera terrosa",
            therapeuticBenefit = "Supresor del apetito, antiinflamatorio",
            color = TerpHumulene
        )
    )
}
