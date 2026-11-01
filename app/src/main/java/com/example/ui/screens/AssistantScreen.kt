package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatMessage

@Composable
fun AssistantScreen(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = ElectricCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "ASSISTANT NEXUS (JARVIS ARCHITECTURE)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = "Qwen-2.5 / Gemma-4 Local Engine + BitNet Quantization", color = TextSecondary, fontSize = 11.sp)
            }
        }

        // Suggestions bar
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            SuggestionChip(
                label = "Optimiser le smartphone",
                onClick = { onSendMessage("Optimiser les performances et la RAM du smartphone") }
            )
            SuggestionChip(
                label = "Diagnostic Médical",
                onClick = { onSendMessage("Effectuer un scanner radio DICOM de contrôle") }
            )
        }

        // Chat message list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Posez une question ou donnez une instruction à l'assistant NEXUS.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(messages) { msg ->
                    ChatMessageBubble(msg = msg)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Tapez votre instruction ici...", color = TextMuted, fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field")
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .background(ElectricCyan, RoundedCornerShape(50))
                    .testTag("chat_send_button")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Envoyer", tint = DeepObsidian)
            }
        }
    }
}

@Composable
private fun SuggestionChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(CardBackground, RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ChatMessageBubble(msg: ChatMessage) {
    val isUser = msg.sender == "USER"
    var showTrace by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(
                    color = if (isUser) ElectricBlue.copy(alpha = 0.85f) else CardBackground,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) ElectricCyan else CardBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = if (isUser) "UTILISATEUR" else "NEXUS AGENT",
                    color = if (isUser) Color.White else ElectricCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = msg.text,
                    color = TextPrimary,
                    fontSize = 13.sp
                )

                if (msg.actionState != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    StatusBadge(state = msg.actionState)
                }

                if (msg.thoughtTrace.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .clickable { showTrace = !showTrace }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showTrace) "Masquer la réflexion" else "Voir la réflexion agentique",
                            color = NeonPurple,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AnimatedVisibility(visible = showTrace) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .background(DarkNavy, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = msg.thoughtTrace,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
