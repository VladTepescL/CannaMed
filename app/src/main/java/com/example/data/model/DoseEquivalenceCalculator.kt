package com.example.data.model

enum class PuffDuration(
    val id: String,
    val displayName: String,
    val secondsRange: String,
    val description: String,
    // Grams of dried flower equivalent per puff for Cartridges/Concentrates (1g extract ~ 4g-5g flower)
    val concentrateFlowerGrams: Double,
    // Grams of dried flower equivalent per puff for Dry Herb Vaporizer (chamber ~0.18g, ~8-10 puffs)
    val herbalVapeFlowerGrams: Double,
    // Grams of dried flower equivalent per puff for Smoked / Joint (joint ~0.5g, ~15-20 puffs)
    val smokedJointFlowerGrams: Double
) {
    SHORT(
        id = "short",
        displayName = "Calada Corta",
        secondsRange = "1-2 seg",
        description = "Microdosificación / Inhalación rápida y suave",
        concentrateFlowerGrams = 0.035, // ~1.5-2 mg THC/CBD (~28 caladas = 1g flor)
        herbalVapeFlowerGrams = 0.015,
        smokedJointFlowerGrams = 0.020
    ),
    STANDARD(
        id = "standard",
        displayName = "Calada Estándar",
        secondsRange = "3-4 seg",
        description = "Inhalación clínica habitual recomendada",
        concentrateFlowerGrams = 0.060, // ~3.5-4 mg THC/CBD (~16 caladas = 1g flor)
        herbalVapeFlowerGrams = 0.025,
        smokedJointFlowerGrams = 0.035
    ),
    LONG(
        id = "long",
        displayName = "Calada Larga",
        secondsRange = "5+ seg",
        description = "Inhalación profunda / Dosis terapéutica intensiva",
        concentrateFlowerGrams = 0.110, // ~7-8 mg THC/CBD (~9 caladas = 1g flor)
        herbalVapeFlowerGrams = 0.040,
        smokedJointFlowerGrams = 0.055
    );

    companion object {
        fun fromId(id: String): PuffDuration {
            return values().find {
                it.id.equals(id, ignoreCase = true) ||
                        it.displayName.contains(id, ignoreCase = true) ||
                        it.name.equals(id, ignoreCase = true)
            } ?: STANDARD
        }
    }
}

object DoseEquivalenceCalculator {
    const val REFERENCE_STANDARD = "Flor Seca Estándar (~15% Cannabinoides / 150 mg/g)"
    const val REFERENCE_AUTHORITY = "Health Canada Cannabis Act (Schedule 3) & Farmacopea Médica Internacional"

    /**
     * Calculates equivalent grams of dried cannabis flower based on international clinical benchmarks.
     * Standards:
     * - Health Canada Schedule 3: 1 g dried flower = 0.25 g concentrate (4:1 multiplier)
     * - Oral oil drops: 20 drops = 1 mL ≈ 0.20 g dried flower (1 drop = 0.010 g)
     * - Edibles: 10 mg cannabinoid dose ≈ 0.070 g dried flower
     */
    fun calculateEquivalentDriedFlowerGrams(
        format: CannabisFormat,
        dosageValue: Double,
        puffsCount: Int = 0,
        puffDuration: PuffDuration = PuffDuration.STANDARD,
        isEdibleBeverage: Boolean = false
    ): Double {
        val raw = when (format) {
            CannabisFormat.CONCENTRATE_VAPE,
            CannabisFormat.CLEAR_OIL,
            CannabisFormat.DABBING -> {
                if (puffsCount > 0) {
                    puffsCount * puffDuration.concentrateFlowerGrams
                } else {
                    dosageValue * 4.0 // 1g concentrate = 4.0g dried flower
                }
            }

            CannabisFormat.VAPORIZED_FLOWER -> {
                if (puffsCount > 0) {
                    puffsCount * puffDuration.herbalVapeFlowerGrams
                } else {
                    dosageValue // 1:1 direct dried flower
                }
            }

            CannabisFormat.SMOKED_JOINT,
            CannabisFormat.SMOKED_PIPE,
            CannabisFormat.SMOKED_BONG -> {
                if (dosageValue > 0) {
                    dosageValue // direct grams of dried flower
                } else {
                    puffsCount * puffDuration.smokedJointFlowerGrams
                }
            }

            CannabisFormat.DROPS_TINCTURE -> {
                // 1 drop = 0.010 g dried flower
                dosageValue * 0.010
            }

            CannabisFormat.EDIBLE -> {
                // dosageValue in mg (default 10mg ~ 0.070g flower)
                if (isEdibleBeverage) {
                    (dosageValue / 10.0) * 0.080
                } else {
                    (dosageValue / 10.0) * 0.070
                }
            }

            CannabisFormat.CAPSULES -> {
                (dosageValue / 10.0) * 0.070
            }

            CannabisFormat.TOPICAL -> {
                dosageValue * 0.050
            }
        }

        return Math.round(raw * 1000.0) / 1000.0
    }
}
