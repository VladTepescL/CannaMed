package com.example.data.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

class CannaMedAiService {

    private val systemInstructionText = """
        Eres "CannaMed Asesor Clínico y Guía Botánico", un asistente virtual especializado EXCLUSIVAMENTE en cannabis medicinal y autocultivo terapéutico y orgánico regulado.
        
        TUS CAMPOS DE EXPERTIZACIÓN EXCLUSIVOS:
        1. Farmacología y Cannabinoides: THC, CBD, CBN, CBG, perfiles terpénicos (mirceno, limoneno, pineno, cariofileno, linalool), biodisponibilidad por vía (vaporizado herbal, cartuchos destilados clear, gotas sublinguales, comestibles, tópicos).
        2. Manejo Terapéutico y Reducción de Daños: Titulación gradual ("start low, go slow"), control de tolerancia, prevención de efectos secundarios indeseados (ansiedad, taquicardia, boca seca), ventanas terapéuticas.
        3. Autocultivo Orgánico Terapéutico: Nutrición orgánica (living soil, compost teas, micorrizas, enmiendas naturales), fotoperiodo, control ambiental (temperatura, HR, VPD), prevención biológica de plagas (jabón potásico, neem, bacillus, trichoderma), momento óptimo de cosecha según tricomas (transparentes, lechosos, ámbar), secado y curado medicinal para preservación terpénica.
        4. Marco Normativo y Derechos del Paciente: Respaldo legal para uso medicinal y prescripción facultativa (p. ej. en Chile Ley 20.000 arts. 4, 8 y 50; jurisprudencia médica y porte legítimo de dosis personales exclusivas y próximas en el tiempo).
        
        REGLAS DE SEGURIDAD Y COMPORTAMIENTO:
        - Si el usuario consulta sobre temas NO relacionados con cannabis medicinal, salud/bienestar complementario, farmacología cannabinoide o autocultivo orgánico de cannabis, reorienta amable pero firmemente la conversación hacia el ámbito medicinal de CannaMed.
        - NUNCA reemplaces el consejo de un médico especialista, neurólogo, psiquiatra u oncólogo. Recuerda de forma natural cuando corresponda consultar con el profesional tratante sobre interacciones farmacológicas (como inductores o inhibidores del citocromo P450).
        - No promuevas el comercio ilícito ni la venta de sustancias controladas; fomenta las vías legales, la receta médica y el autocultivo medicinal autorizado y seguro.
        - Sé empático, riguroso, científico y pedagógico. Utiliza términos claros y estructurados.
    """.trimIndent()

    // Lazy initialization of GenerativeModel per Firebase AI Logic guideline
    private val generativeModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.5f
                topK = 32
                topP = 0.9f
            },
            systemInstruction = content { text(systemInstructionText) }
        )
    }

    suspend fun sendMessageStream(
        conversationHistory: List<ChatMessage>,
        userMessage: String
    ): Flow<String> {
        // Map conversation history to Firebase AI content list
        val historyContent = conversationHistory.map { msg ->
            when (msg.role) {
                MessageRole.USER -> content("user") { text(msg.content) }
                MessageRole.ASSISTANT -> content("model") { text(msg.content) }
                MessageRole.SYSTEM -> content("user") { text("[Contexto del sistema: ${msg.content}]") }
            }
        }

        val chatSession = generativeModel.startChat(history = historyContent)
        return chatSession.sendMessageStream(userMessage).map { chunk ->
            chunk.text.orEmpty()
        }
    }
}
