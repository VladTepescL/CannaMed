package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

enum class CannabisFormat(
    val displayName: String,
    val description: String,
    val defaultUnit: String,
    val availableUnits: List<String>,
    val allowsPuffs: Boolean,
    val allowsTemperature: Boolean,
    val allowsClearOilProfile: Boolean
) {
    VAPORIZED_FLOWER(
        displayName = "Vaporizado de Flor",
        description = "Vaporizador herbal a temperatura controlada",
        defaultUnit = "Caladas",
        availableUnits = listOf("Caladas", "Gramos (g)", "Cazoletas"),
        allowsPuffs = true,
        allowsTemperature = true,
        allowsClearOilProfile = false
    ),
    CONCENTRATE_VAPE(
        displayName = "Vaporizado Concentrado",
        description = "Cartucho o pod 510 con concentrado líquido",
        defaultUnit = "Caladas",
        availableUnits = listOf("Caladas", "Segundos de calada"),
        allowsPuffs = true,
        allowsTemperature = false,
        allowsClearOilProfile = true
    ),
    CLEAR_OIL(
        displayName = "Aceite Clear / Destilado",
        description = "Aceite de alta pureza con terpenos y cannabinoides medidos",
        defaultUnit = "ml",
        availableUnits = listOf("ml", "Gotas", "mg"),
        allowsPuffs = false,
        allowsTemperature = false,
        allowsClearOilProfile = true
    ),
    DROPS_TINCTURE(
        displayName = "Gotas / Tintura",
        description = "Administración sublingual mediante gotero",
        defaultUnit = "Gotas",
        availableUnits = listOf("Gotas", "ml", "mg"),
        allowsPuffs = false,
        allowsTemperature = false,
        allowsClearOilProfile = true
    ),
    EDIBLE(
        displayName = "Comestibles / Edibles",
        description = "Gomitas, alimentos o chocolates dosificados",
        defaultUnit = "mg",
        availableUnits = listOf("mg", "Porciones", "Unidades"),
        allowsPuffs = false,
        allowsTemperature = false,
        allowsClearOilProfile = false
    ),
    SMOKED_PIPE(
        displayName = "Fumado en Pipa",
        description = "Combustión en pipa tradicional de vidrio o madera",
        defaultUnit = "Caladas",
        availableUnits = listOf("Caladas", "Cazoletas", "Gramos"),
        allowsPuffs = true,
        allowsTemperature = false,
        allowsClearOilProfile = false
    ),
    SMOKED_JOINT(
        displayName = "Fumado en Papel",
        description = "Cigarrillo / papel enrolado medicinal",
        defaultUnit = "Caladas",
        availableUnits = listOf("Caladas", "Fracción (1/4, 1/2)", "Gramos"),
        allowsPuffs = true,
        allowsTemperature = false,
        allowsClearOilProfile = false
    ),
    SMOKED_BONG(
        displayName = "Fumado en Bong",
        description = "Pipa de agua con filtración líquida",
        defaultUnit = "Caladas",
        availableUnits = listOf("Caladas", "Hits / Inhalaciones"),
        allowsPuffs = true,
        allowsTemperature = false,
        allowsClearOilProfile = false
    ),
    CAPSULES(
        displayName = "Cápsulas Blandas",
        description = "Cápsulas orales de liberación controlada",
        defaultUnit = "Cápsula(s)",
        availableUnits = listOf("Cápsula(s)", "mg"),
        allowsPuffs = false,
        allowsTemperature = false,
        allowsClearOilProfile = false
    ),
    TOPICAL(
        displayName = "Tópico / Crema",
        description = "Pomadas, ungüentos y parches dérmicos",
        defaultUnit = "Aplicación",
        availableUnits = listOf("Aplicación", "Gramos (g)", "Dosis"),
        allowsPuffs = false,
        allowsTemperature = false,
        allowsClearOilProfile = false
    ),
    DABBING(
        displayName = "Dab / Concentrado Sólido",
        description = "Vaporización de wax, shatter o rosin en banger",
        defaultUnit = "Dabs",
        availableUnits = listOf("Dabs", "mg", "Hits"),
        allowsPuffs = true,
        allowsTemperature = true,
        allowsClearOilProfile = true
    );

    val icon: ImageVector
        get() = when (this) {
            VAPORIZED_FLOWER -> Icons.Default.Air
            CONCENTRATE_VAPE -> Icons.Default.ElectricBolt
            CLEAR_OIL -> Icons.Default.Science
            DROPS_TINCTURE -> Icons.Default.WaterDrop
            EDIBLE -> Icons.Default.Restaurant
            SMOKED_PIPE -> Icons.Default.SmokingRooms
            SMOKED_JOINT -> Icons.Default.Park
            SMOKED_BONG -> Icons.Default.LocalDrink
            CAPSULES -> Icons.Default.LocalPharmacy
            TOPICAL -> Icons.Default.Spa
            DABBING -> Icons.Default.Opacity
        }
}
