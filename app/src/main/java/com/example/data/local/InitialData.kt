package com.example.data.local

import com.example.data.model.CannabisFormat
import com.example.data.model.Cartridge
import com.example.data.model.DosageLog
import com.example.data.model.LegalExtract
import com.example.data.model.Prescription
import com.example.data.model.StrainInfo

object InitialData {
    val DEFAULT_STRAINS = listOf(
        StrainInfo(
            name = "Charlotte's Web",
            type = "Alto CBD",
            thcRange = "< 1%",
            cbdRange = "15% - 20%",
            dominantTerpenes = "Beta-Cariofileno, Mirceno, Bisabolol",
            medicalBenefits = "Epilepsia, dolor neuropático, inflamación crónica, estrés sin psicoactividad",
            flavorAroma = "Pino dulce, madera terrosa, floral",
            description = "Famosa cepa desarrollada específicamente para uso medicinal pediátrico y de adultos. Proporciona notable alivio corporal sin efectos intoxicantes.",
            leaflySlug = "charlottes-web",
            isCustom = false
        ),
        StrainInfo(
            name = "Harlequin",
            type = "Híbrida (CBD:THC 5:2)",
            thcRange = "4% - 7%",
            cbdRange = "8% - 15%",
            dominantTerpenes = "Mirceno, Alfa-Pineno, Beta-Cariofileno",
            medicalBenefits = "Dolor articular, migraña, espasmos musculares, fatiga diurna",
            flavorAroma = "Mango dulce, cítricos terrosos, almizcle",
            description = "Híbrida de predominancia sativa con ratio equilibrado CBD:THC. Permite relajación profunda manteniendo lucidez y claridad mental para trabajar.",
            leaflySlug = "harlequin",
            isCustom = false
        ),
        StrainInfo(
            name = "ACDC",
            type = "Alto CBD",
            thcRange = "0.5% - 1.2%",
            cbdRange = "14% - 22%",
            dominantTerpenes = "Mirceno, Pineno, Beta-Cariofileno",
            medicalBenefits = "Ansiedad severa, dolor inflamatorio, náuseas, temblores",
            flavorAroma = "Cerezas terrosas, pino fresco, notas herbales",
            description = "Fenotipo de Cannatonic de altísimo contenido en cannabidiol. Ideal para pacientes sensibles al THC que buscan reducción inmediata de inflamación.",
            leaflySlug = "acdc",
            isCustom = false
        ),
        StrainInfo(
            name = "Granddaddy Purple",
            type = "Indica",
            thcRange = "17% - 23%",
            cbdRange = "< 0.5%",
            dominantTerpenes = "Mirceno, Beta-Cariofileno, Pineno",
            medicalBenefits = "Insomnio severo, dolor musculoesquelético profundo, pérdida de apetito",
            flavorAroma = "Uva madura, bayas dulces, madera de roble",
            description = "Clásica cepa indica californiana. Efecto sedante muscular y somnolencia reparadora recomendada para uso nocturno.",
            leaflySlug = "granddaddy-purple",
            isCustom = false
        ),
        StrainInfo(
            name = "Blue Dream",
            type = "Híbrida",
            thcRange = "18% - 24%",
            cbdRange = "0.1% - 0.3%",
            dominantTerpenes = "Mirceno, Alfa-Pineno, Beta-Cariofileno",
            medicalBenefits = "Depresión reactiva, dolor crónico moderado, tensión neuromuscular",
            flavorAroma = "Arándanos frescos, vainilla dulce, pino suave",
            description = "Híbrida legendaria entre Blueberry y Haze. Produce alivio físico rápido con elevación anímica suave sin provocar taquicardia.",
            leaflySlug = "blue-dream",
            isCustom = false
        ),
        StrainInfo(
            name = "OG Kush",
            type = "Híbrida",
            thcRange = "20% - 25%",
            cbdRange = "0.2% - 0.5%",
            dominantTerpenes = "Limoneno, Mirceno, Beta-Cariofileno",
            medicalBenefits = "Estrés postraumático, náuseas refractarias, espasmos severos",
            flavorAroma = "Gas / combustible, limón ácido, pino especiado",
            description = "Cepa pilar de la farmacopea cannábica moderna con fuerte respuesta en receptores CB1 y modulación por limoneno.",
            leaflySlug = "og-kush",
            isCustom = false
        ),
        StrainInfo(
            name = "Cannatonic",
            type = "Híbrida (CBD:THC 1:1)",
            thcRange = "5% - 7%",
            cbdRange = "6% - 12%",
            dominantTerpenes = "Mirceno, Linalool, Beta-Cariofileno",
            medicalBenefits = "Fibromialgia, migrañas crónicas, dolor neuropático",
            flavorAroma = "Cítrico terroso, madera fresca, sutil floral",
            description = "Variedad terapéutica por excelencia para lograr efecto séquito equilibrado entre THC y CBD.",
            leaflySlug = "cannatonic",
            isCustom = false
        ),
        StrainInfo(
            name = "Jack Herer",
            type = "Sativa",
            thcRange = "18% - 24%",
            cbdRange = "0.1% - 0.2%",
            dominantTerpenes = "Terpinoleno, Beta-Cariofileno, Pineno",
            medicalBenefits = "Fatiga crónica, falta de apetito, depresión, déficit atencional",
            flavorAroma = "Pino especiado, pimienta herbal, naranja",
            description = "Sativa médica creada en Países Bajos. Aporta energía funcional y lucidez sin generar confusión cognitiva.",
            leaflySlug = "jack-herer",
            isCustom = false
        ),
        StrainInfo(
            name = "Northern Lights",
            type = "Indica",
            thcRange = "16% - 21%",
            cbdRange = "0.1% - 0.4%",
            dominantTerpenes = "Mirceno, Beta-Cariofileno, Limoneno",
            medicalBenefits = "Espasmos musculares severos, dolor de columna, insomnio de inicio",
            flavorAroma = "Pino dulce terroso, madera y especias",
            description = "Una de las cepas más estudiadas para relajación muscular completa y alivio del estrés físico crónico.",
            leaflySlug = "northern-lights",
            isCustom = false
        )
    )

    val DEFAULT_LEGAL_EXTRACTS = listOf(
        LegalExtract(
            country = "Chile",
            title = "Ley 20.000 - Artículo 8 (Exención Cultivo y Destino Médico)",
            category = "Ley de la República",
            fullLegalText = "El que, careciendo de la debida autorización, siembre, plante, cultive o coseche especies de género cannabis... a menos que justifique que están destinadas a su uso o consumo personal exclusivo y próximo en el tiempo, caso en el cual sólo se aplicarán las sanciones de los artículos 50 y siguientes.",
            practicalSummary = "El cultivo personal no constituye delito cuando está destinado a un tratamiento médico personal exclusivo y próximo en el tiempo, acreditado fehacientemente mediante prescripción médica extendida por médico cirujano habilitado.",
            relevanceForPatient = "Acredita la licitud del cultivo medicinal propio ante fiscalizaciones en el domicilio."
        ),
        LegalExtract(
            country = "Chile",
            title = "Ley 20.000 - Artículo 4 (Porte, Transporte y Tratamiento Médico)",
            category = "Ley de la República",
            fullLegalText = "El que, sin la debida autorización legal, posea, transporte, guarde o porte consigo pequeñas cantidades de sustancias o drogas estupefacientes... a menos que justifique que están destinadas a la atención de un tratamiento médico o a su uso o consumo personal exclusivo y próximo en el tiempo.",
            practicalSummary = "El artículo 4 consagra expresamente la excepción penal para el paciente: el porte o transporte está amparado por la ley cuando se realiza para la 'atención de un tratamiento médico'.",
            relevanceForPatient = "Protege el transporte y porte de la medicina en la vía pública junto a la receta y cédula de identidad."
        ),
        LegalExtract(
            country = "Chile",
            title = "Ley 20.000 - Artículo 50 (Consumo Personal y Porte Justificado)",
            category = "Ley de la República",
            fullLegalText = "Los que consumieren alguna de las drogas o sustancias estupefacientes... a menos que se justifique el uso personal exclusivo y próximo en el tiempo con prescripción médica.",
            practicalSummary = "No existe falta administrativa ni sanción cuando el consumo o porte está respaldado por indicación médica para alivio de patologías.",
            relevanceForPatient = "Justifica la administración en lugares públicos o privados para alivio de crisis o sintomatología."
        ),
        LegalExtract(
            country = "Chile",
            title = "Corte Suprema de Chile - Jurisprudencia Reiterada sobre Uso Medicinal",
            category = "Jurisprudencia Judicial",
            fullLegalText = "La Segunda Sala de la Excma. Corte Suprema (Fallos Rol 81.399-2021, Rol 4.156-2015, entre otros) ha sostenido uniformemente que la tenencia y cultivo de cannabis justificada médicamente por un profesional no lesiona el bien jurídico de la salud pública, constituyendo una conducta atípica amparada por el derecho constitucional a la salud e integridad física y psíquica.",
            practicalSummary = "El máximo tribunal del país ratifica que los pacientes medicinales que cuentan con receta médica no cometen delito al proveerse de su propia medicina.",
            relevanceForPatient = "Criterio judicial vinculante que protege al paciente frente a imputaciones del Ministerio Público o policías."
        ),
        LegalExtract(
            country = "Chile",
            title = "Protocolo de Actuación y Recomendaciones ante Fiscalización",
            category = "Protocolo de Seguridad",
            fullLegalText = "1. Mantener la calma y declarar de inmediato su condición de PACIENTE EN TRATAMIENTO MÉDICO.\n2. Exhibir de inmediato la Cédula de Identidad y la Receta Médica vigente (física o digital en CannaMed).\n3. Mostrar el registro diario de dosificación donde constan las horas, formatos y dosis prescritas.\n4. Invocar respetuosamente el Artículo 4 de la Ley 20.000 que exime el tratamiento médico.\n5. En caso de allanamiento, no prestar declaración sin presencia de abogado defensor y exigir que se consigne en el acta la existencia de la receta médica.",
            practicalSummary = "Guía rápida para actuar con serenidad y respaldo normativo durante un control de Carabineros de Chile o PDI.",
            relevanceForPatient = "Instrucciones prácticas de defensa inmediata de derechos."
        )
    )

    fun createInitialCartridge(): Cartridge {
        return Cartridge(
            brandName = "MedReleaf Clear Extract",
            varietyName = "Harlequin Therapeutic 1:1",
            formatType = CannabisFormat.CONCENTRATE_VAPE,
            capacityMl = 1.0f,
            thcPercentage = 42.5f,
            cbdPercentage = 45.0f,
            cbnPercentage = 1.2f,
            cbgPercentage = 1.8f,
            terpenesSummary = "Mirceno 2.1%, Beta-Cariofileno 1.4%, Limoneno 0.9%, Pineno 0.6%",
            totalPuffsLogged = 18,
            installedTimestamp = System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000),
            isActive = true,
            batchLotNumber = "LOT-2026-MED449",
            notes = "Cartucho CCELL 510 para dolor agudo vespertino. Sabor suave herbal."
        )
    }

    fun createInitialLogs(): List<DosageLog> {
        val now = System.currentTimeMillis()
        return listOf(
            DosageLog(
                timestamp = now - (2L * 60 * 60 * 1000), // 2 hours ago
                format = CannabisFormat.CONCENTRATE_VAPE,
                strainName = "Harlequin Therapeutic 1:1",
                strainType = "Híbrida",
                dosageValue = 3.0,
                dosageUnit = "Caladas",
                puffsCount = 3,
                puffIntensity = "Estándar (3-4s)",
                equivalentGramsDriedFlower = 0.18, // 3 caladas estándar * 0.060g
                temperatureCelsius = null,
                cartridgeId = 1L,
                thcPercentage = 42.5f,
                cbdPercentage = 45.0f,
                terpenesNote = "Mirceno 2.1%, Cariofileno 1.4%",
                reliefScore = 9,
                treatedSymptoms = "Dolor lumbar, Tensión muscular",
                moodEffect = "Calmado y productivo",
                sideEffects = "Ninguno",
                subjectiveNotes = "Alivio rápido en 8 minutos. Pude retomar el trabajo en el ordenador sin molestia."
            ),
            DosageLog(
                timestamp = now - (14L * 60 * 60 * 1000), // morning
                format = CannabisFormat.SMOKED_JOINT,
                strainName = "Harlequin Flores",
                strainType = "Híbrida",
                dosageValue = 0.40,
                dosageUnit = "g",
                puffsCount = 4,
                puffIntensity = "Estándar (3-4s)",
                equivalentGramsDriedFlower = 0.40, // 0.40g flor seca directa
                temperatureCelsius = null,
                cartridgeId = null,
                thcPercentage = 6.0f,
                cbdPercentage = 10.0f,
                terpenesNote = "Mirceno, Pineno",
                reliefScore = 8,
                treatedSymptoms = "Rigidez matutina, Dolor articular",
                moodEffect = "Enfocado y sereno",
                sideEffects = "Ninguno",
                subjectiveNotes = "0.40g consumidos en la mañana para control de movilidad."
            )
        )
    }

    fun createInitialPrescription(): Prescription {
        val now = System.currentTimeMillis()
        return Prescription(
            doctorName = "Dra. Sofía Mendoza Valdés",
            licenseNumber = "MED-782410-COL",
            medicalCenter = "Centro Médico de Terapias Cannábicas y Dolor",
            issueDateMillis = now - (15L * 24 * 60 * 60 * 1000),
            expiryDateMillis = now + (165L * 24 * 60 * 60 * 1000), // 6 months control
            prescribedDailyGrams = 3.0, // 3.0 gramos diarios prescritos
            prescribedFormat = "Flores secas y Extracto sublingual",
            prescribedDailyDosage = "Hasta 3.0g diarios en vaporizado o infusión según EVA",
            clinicalDiagnosis = "Lumbociatalgia crónica recurrente y trastorno del sueño reactivo",
            instructions = "Mantener registro de caladas y evolución del dolor en cada administración para control en 60 días.",
            localDocumentPath = null,
            documentMimeType = null,
            documentFileName = "Receta_Oficial_Mendoza_2026.pdf"
        )
    }
}
