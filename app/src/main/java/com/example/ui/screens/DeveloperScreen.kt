package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun DeveloperScreen(
    terminalLogs: List<String>,
    onExecuteCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commandText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = ElectricCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "TERMINAL CLI & DEVELOPER ENVIRONMENT", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Interpréteur Shell nexus-cli, pont JNI Rust/C++", color = TextSecondary, fontSize = 11.sp)
            }
        }

        // Terminal Output Window
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepObsidian),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricCyan)),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(terminalLogs) { log ->
                    Text(
                        text = log,
                        color = if (log.startsWith("ERR")) CyberRed else if (log.startsWith("SYSTEM")) CyberGreen else ElectricCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Shell Input Box
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = commandText,
                onValueChange = { commandText = it },
                placeholder = { Text("Tapez 'help', 'status', 'jni', 'scan-med'...", color = TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminal_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (commandText.isNotBlank()) {
                        onExecuteCommand(commandText)
                        commandText = ""
                    }
                },
                modifier = Modifier
                    .background(ElectricCyan, RoundedCornerShape(8.dp))
                    .testTag("btn_terminal_send")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Run", tint = DeepObsidian)
            }
        }
    }
}
