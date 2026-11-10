package com.example.data.ai

import kotlinx.coroutines.delay

data class ClinicalChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

object MockCannabisClinicalService {

    private val predefinedKnowledge = listOf(
        Pair(
            listOf("terpeno", "terpenos", "mirceno", "limoneno", "cariofileno", "linalool", "pineno", "aroma"),
            """
🌿 **Guía Farmacológica de Terpenos y Efecto Séquito**:

1. **Beta-Mirceno**:
   - *Aroma*: Terroso, clavo de olor, herbal.
   - *Efecto*: Potente relajante muscular, sedante suave y mejora la absorción hematoencefálica de cannabinoides.
   - *Punto de ebullición*: ~168°C. Ideal para insomnio y contracturas.

2. **D-Limoneno**:
   - *Aroma*: Cítrico, cáscara de naranja/limón.
   - *Efecto*: Elevador del ánimo, ansiolítico, gástrico y estimulante dopaminérgico.
   - *Punto de ebullición*: ~176°C. Frecuente en cepas sativas y balanceadas.

3. **Beta-Cariofileno**:
   - *Aroma*: Pimienta negra, especias, amaderado.
   - *Efecto*: Único terpeno que actúa directamente como agonista del receptor cannabinoide CB2. Notable antiinflamatorio sin efecto psicoactivo.
   - *Punto de ebullición*: ~130°C.

4. **Linalool**:
   - *Aroma*: Floral, lavanda fresca.
   - *Efecto*: Modulador GABAérgico, sedante, anticonvulsivante y calmante de crisis de angustia.
   - *Punto de ebullición*: ~198°C.
            """.trimIndent()
        ),
        Pair(
            listOf("dosis", "dosificacion", "calcular", "titulacion", "inicio", "cuanto", "miligramo", "mg"),
            """
⚖️ **Protocolo de Titulación y Dosificación Médica ("Start Low, Go Slow")**:

- **Fase de Inducción (Días 1 a 3)**:
  Comenzar con la dosis mínima eficaz (ejemplo: 1 calada corta de 2 segundos o 2.5 mg de CBD sublingual por la noche).
- **Titulación Progresiva**:
  Aumentar cada 3 o 4 días en incrementos del 20-25% únicamente si los síntomas persisten y no hay efectos adversos.
- **Equivalencia Estándar en Flores Secas (Health Canada)**:
  * 1 calada estándar (3s) en vaporizador ≈ **0.06 g** de flores secas.
  * 1 calada larga (5s) ≈ **0.10 g** de flores secas.
  * 1 mL de tintura sublingual (20 mg/mL) ≈ **0.20 g** equivalentes.
- **Límite Prescrito**:
  Mantenerse siempre dentro del gramaje diario autorizado por tu médico en la receta (por ejemplo: 2.0g/día).
            """.trimIndent()
        ),
        Pair(
            listOf("temperatura", "vaporizador", "vaporizar", "grados", "humo", "combustion"),
            """
🌡️ **Temperaturas de Vaporización Médica Recomendadas**:

- **Rango Bajo (160°C - 175°C)**:
  Extracción pura de monoterpenos (pineno, mirceno) y liberación leve de THC. Sabor intenso, vapor suave, efecto cerebral lúcido y diurno.
- **Rango Terapéutico Estándar (180°C - 195°C)**:
  Volatilización óptima de THC, CBD, limoneno y cariofileno. Mayor equilibrio entre alivio analgésico y relajación sin llegar a pirólisis.
- **Rango Alto / Sedante (200°C - 210°C)**:
  Extracción de linalool, CBN (cannabinol) y fitocannabinoides de alto peso molecular. Máximo alivio para dolor crónico severo e inducción del sueño.
⚠️ *Evitar temperaturas superiores a 220°C para prevenir la generación de benceno u otros subproductos de combustión.*
            """.trimIndent()
        ),
        Pair(
            listOf("cultivo", "autocultivo", "organico", "sustrato", "secado", "curado", "tricomas", "plagas"),
            """
🌱 **Principios de Autocultivo Orgánico Medicinal**:

1. **Sustrato Vivo (Living Soil)**:
   - Emplear mezclas ricas en humus de lombriz, compost maduro, micorrizas y enmiendas minerales (harina de rocas, diatomeas).
   - Fomenta la microbiota del suelo sin necesidad de sales minerales sintéticas.

2. **Cosecha por Maduración de Tricomas**:
   - Observar con lupa 60x: cosechar cuando el 70-80% de los tricomas estén lechosos y un 15-20% en tono ámbar para un perfil farmacológico equilibrado.

3. **Secado y Curado Farmacológico**:
   - *Secado*: Oscuridad total, 18°C-20°C y 50%-55% humedad relativa durante 10-14 días.
   - *Curado*: Frascos de vidrio sellados con sobres reguladores de humedad al 62% RH. Abrir diariamente durante las primeras 2 semanas para evaporar clorofila residual.
            """.trimIndent()
        ),
        Pair(
            listOf("ley", "chile", "receta", "legal", "fiscalizacion", "policia", "20000"),
            """
⚖️ **Marco Legal en Chile (Ley N° 20.000)**:

- **Artículos 8 y 50**: Despenalizan el cultivo y la tenencia de cannabis cuando su destino es exclusivo para uso médico personal y próximo en el tiempo.
- **Jurisprudencia de la Corte Suprema**: Ha reiterado que la posesión de una receta médica emitida por un médico habilitado, junto con el informe de diagnóstico, desvirtúa la presunción de tráfico.
- **Consejo ante Fiscalización**: Presenta la receta digital de CannaMed, el certificado de equivalencia y el cuerpo legal guardado en la pestaña *Área Médica > Marco Jurídico*.
            """.trimIndent()
        )
    )

    suspend fun getMockResponse(userQuery: String): String {
        delay(500)
        val cleanQuery = userQuery.lowercase()

        for ((keywords, answer) in predefinedKnowledge) {
            if (keywords.any { cleanQuery.contains(it) }) {
                return answer
            }
        }

        return """
🌿 **Asesor Clínico de Cannabis Medicinal (Modo Offline)**:

Has consultado sobre: *"${userQuery.take(40)}"*

• **Recomendación Clínica**: Registra tus dosis diarias en la bitácora con los gramos equivalentes de flores secas y puntajes de alivio para que tu médico tratante pueda evaluar la curva de tolerancia.
• **Perfiles de Terpenos**: Para mayor información, puedes consultar sobre **mirceno**, **limoneno**, **cariofileno**, **linalool** o **pineno**.
• **Titulación**: Recuerda no modificar tus dosis prescritas sin la previa indicación del médico tratante facultativo.
        """.trimIndent()
    }
}
