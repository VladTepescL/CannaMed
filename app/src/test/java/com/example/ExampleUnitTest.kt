package com.example

import com.example.data.local.InitialData
import com.example.data.model.CannabisFormat
import com.example.data.model.Cartridge
import com.example.data.model.DoseEquivalenceCalculator
import com.example.data.model.PuffDuration
import com.example.ui.DailyGramsProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testCannabisFormatsCountAndProperties() {
        val formats = CannabisFormat.values()
        assertTrue("Debe soportar múltiples formatos de administración medicinal", formats.size >= 10)
        assertTrue("Vaporizado flor debe permitir caladas", CannabisFormat.VAPORIZED_FLOWER.allowsPuffs)
        assertTrue("Aceite clear debe permitir perfil de terpenos", CannabisFormat.CLEAR_OIL.allowsClearOilProfile)
    }

    @Test
    fun testCartridgeCalculations() {
        val cart = Cartridge(
            brandName = "Extracto Test",
            varietyName = "Harlequin 1:1",
            capacityMl = 1.0f,
            thcPercentage = 40.0f,
            cbdPercentage = 40.0f,
            cbnPercentage = 0.0f,
            cbgPercentage = 0.0f
        )
        assertEquals(400.0f, cart.mgPerMlThc, 0.01f)
        assertEquals(400.0f, cart.mgPerMlCbd, 0.01f)
        assertEquals(80.0f, cart.totalCannabinoidsPercentage, 0.01f)
    }

    @Test
    fun testInitialClinicalStrains() {
        val defaultStrains = InitialData.DEFAULT_STRAINS
        assertTrue(defaultStrains.isNotEmpty())
        val charlotte = defaultStrains.find { it.name == "Charlotte's Web" }
        assertNotNull(charlotte)
        assertTrue(charlotte!!.leaflyUrl.contains("leafly.com"))
    }

    @Test
    fun testDailyGramsCountdownMath() {
        val progress = DailyGramsProgress(
            prescribedGrams = 3.0,
            consumedGrams = 0.40,
            remainingGrams = 2.60
        )
        assertEquals(3.0, progress.prescribedGrams, 0.001)
        assertEquals(0.40, progress.consumedGrams, 0.001)
        assertEquals(2.60, progress.remainingGrams, 0.001)
        assertTrue(progress.remainingGrams <= progress.prescribedGrams)
    }

    @Test
    fun testDoseEquivalencePuffDurations() {
        // Clear Pods / Concentrates:
        // Short puff: 0.035g dried flower
        val shortPod = DoseEquivalenceCalculator.calculateEquivalentDriedFlowerGrams(
            format = CannabisFormat.CONCENTRATE_VAPE,
            dosageValue = 3.0,
            puffsCount = 3,
            puffDuration = PuffDuration.SHORT
        )
        assertEquals(0.105, shortPod, 0.001)

        // Standard puff: 0.060g dried flower
        val standardPod = DoseEquivalenceCalculator.calculateEquivalentDriedFlowerGrams(
            format = CannabisFormat.CONCENTRATE_VAPE,
            dosageValue = 3.0,
            puffsCount = 3,
            puffDuration = PuffDuration.STANDARD
        )
        assertEquals(0.180, standardPod, 0.001)

        // Long puff: 0.110g dried flower
        val longPod = DoseEquivalenceCalculator.calculateEquivalentDriedFlowerGrams(
            format = CannabisFormat.CONCENTRATE_VAPE,
            dosageValue = 3.0,
            puffsCount = 3,
            puffDuration = PuffDuration.LONG
        )
        assertEquals(0.330, longPod, 0.001)
    }

    @Test
    fun testDoseEquivalenceOtherFormats() {
        // Drops: 10 drops = 0.100g dried flower
        val dropsGrams = DoseEquivalenceCalculator.calculateEquivalentDriedFlowerGrams(
            format = CannabisFormat.DROPS_TINCTURE,
            dosageValue = 10.0,
            puffsCount = 0
        )
        assertEquals(0.100, dropsGrams, 0.001)

        // Smoked direct grams: 0.25g = 0.250g
        val smokedGrams = DoseEquivalenceCalculator.calculateEquivalentDriedFlowerGrams(
            format = CannabisFormat.SMOKED_JOINT,
            dosageValue = 0.25,
            puffsCount = 0
        )
        assertEquals(0.250, smokedGrams, 0.001)

        // Edibles: 20mg = 0.140g dried flower
        val edibleGrams = DoseEquivalenceCalculator.calculateEquivalentDriedFlowerGrams(
            format = CannabisFormat.EDIBLE,
            dosageValue = 20.0,
            puffsCount = 0,
            isEdibleBeverage = false
        )
        assertEquals(0.140, edibleGrams, 0.001)
    }

    @Test
    fun testChileanLegalExtracts() {
        val extracts = InitialData.DEFAULT_LEGAL_EXTRACTS
        assertTrue("Debe incluir al menos 5 cuerpos jurídicos de Chile", extracts.size >= 5)

        val art4 = extracts.find { it.title.contains("Artículo 4") }
        assertNotNull("Debe incluir Artículo 4 de Ley 20.000", art4)
        assertTrue(art4!!.fullLegalText.contains("tratamiento médico"))

        val art8 = extracts.find { it.title.contains("Artículo 8") }
        assertNotNull("Debe incluir Artículo 8 de Ley 20.000", art8)
        assertTrue(art8!!.fullLegalText.contains("cultivo") || art8.fullLegalText.contains("siembre"))

        val art50 = extracts.find { it.title.contains("Artículo 50") }
        assertNotNull("Debe incluir Artículo 50 de Ley 20.000", art50)

        val suprema = extracts.find { it.title.contains("Corte Suprema") }
        assertNotNull("Debe incluir jurisprudencia de la Corte Suprema", suprema)

        val protocolo = extracts.find { it.title.contains("Protocolo") }
        assertNotNull("Debe incluir protocolo de fiscalización", protocolo)
    }

    @Test
    fun testInitialPrescriptionPrescribedGrams() {
        val initialPres = InitialData.createInitialPrescription()
        assertEquals(3.0, initialPres.prescribedDailyGrams, 0.01)
        assertTrue(initialPres.doctorName.isNotBlank())
    }
}
