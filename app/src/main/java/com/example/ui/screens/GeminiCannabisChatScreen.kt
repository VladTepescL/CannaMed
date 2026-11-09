package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.CannaMedAiService
import com.example.data.ai.ChatMessage
import com.example.data.ai.MessageRole
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.EmeraldSecondaryContainer
import com.example.ui.theme.EmeraldTertiary
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeminiCannabisChatScreen(
    aiService: CannaMedAiService = remember { CannaMedAiService() }
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                role = MessageRole.ASSISTANT,
                content = "¡Hola! Soy tu Asesor Especializado en Cannabis Medicinal y Autocultivo Orgánico. Puedes consultarme sobre perfiles de cannabinoides y terpenos, titulación de dosis, prevención de tolerancia, técnicas de cultivo en living soil, nutrición orgánica o preparación médica para consulta facultativa."
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var currentStreamingChunk by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quickQuestions = listOf(
        "¿Cómo titular gotas de CBD sublingual?",
        "Tricomas: ¿cuándo cosechar para dolor?",
        "¿Qué terpenos ayudan con la ansiedad?",
        "Living soil: té de compost y micorrizas",
        "Vaporización herbal vs. combustión"
    )

    fun sendPrompt(prompt: String) {
        if (prompt.isBlank() || isGenerating) return

        val userMsg = ChatMessage(role = MessageRole.USER, content = prompt.trim())
        messages.add(userMsg)
        inputText = ""
        isGenerating = true
        errorMessage = null
        currentStreamingChunk = ""

        coroutineScope.launch {
            // Scroll to bottom
            listState.animateScrollToItem(messages.size)

            val stringBuilder = StringBuilder()
            aiService.sendMessageStream(messages.toList(), prompt)
                .catch { ex ->
                    errorMessage = "Error en la consulta: ${ex.localizedMessage ?: "Fallo de conexión"}"
                    isGenerating = false
                    currentStreamingChunk = ""
                }
                .collect { chunk ->
                    stringBuilder.append(chunk)
                    currentStreamingChunk = stringBuilder.toString()
                    listState.animateScrollToItem(messages.size)
                }

            if (stringBuilder.isNotEmpty()) {
                messages.add(
                    ChatMessage(
                        role = MessageRole.ASSISTANT,
                        content = stringBuilder.toString()
                    )
                )
            }
            currentStreamingChunk = ""
            isGenerating = false
            listState.animateScrollToItem(messages.size)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Clinical AI Badge Banner
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Eco, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(
                            text = "Asistente Cannabis Medicinal & Autocultivo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gemini 3.8 Flash • Enfoque Clínico y Botánico",
                            fontSize = 11.sp,
                            color = EmeraldSecondary
                        )
                    }
                }

                IconButton(
                    onClick = {
                        messages.clear()
                        messages.add(
                            ChatMessage(
                                role = MessageRole.ASSISTANT,
                                content = "Conversación reiniciada. ¿Qué consulta tienes hoy sobre cannabis terapéutico o autocultivo orgánico?"
                            )
                        )
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reiniciar conversación", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Quick Suggestions
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            quickQuestions.forEach { question ->
                AssistChip(
                    onClick = { sendPrompt(question) },
                    label = { Text(question, fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp), tint = EmeraldPrimary)
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Chat Message Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(messages) { message ->
                ChatBubble(message = message)
            }

            // Live streaming bubble
            if (isGenerating && currentStreamingChunk.isNotBlank()) {
                item {
                    ChatBubble(
                        message = ChatMessage(
                            role = MessageRole.ASSISTANT,
                            content = currentStreamingChunk
                        ),
                        isStreaming = true
                    )
                }
            }

            // Loading indicator before first token
            if (isGenerating && currentStreamingChunk.isBlank()) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = EmeraldPrimary, strokeWidth = 2.dp)
                        Text(
                            "Consultando evidencia clínica y botánica...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            errorMessage?.let { err ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Input Field Area
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text("Pregunta sobre dosis, cepas, terpenos o cultivo...", fontSize = 13.sp)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gemini_chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    maxLines = 4
                )

                Button(
                    onClick = { sendPrompt(inputText) },
                    enabled = inputText.isNotBlank() && !isGenerating,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("gemini_chat_send_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Enviar mensaje",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isStreaming: Boolean = false
) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Eco, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) EmeraldPrimary else MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.content,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )

                    if (isStreaming) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSecondary)
                            )
                            Text(
                                "Generando en tiempo real...",
                                fontSize = 10.sp,
                                color = EmeraldSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
