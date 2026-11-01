package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutomationEntity
import com.example.ui.theme.*

@Composable
fun AutomationsScreen(
    automationsList: List<AutomationEntity>,
    onAddAutomation: (String, String, String, String) -> Unit,
    onToggleAutomation: (AutomationEntity) -> Unit,
    onDeleteAutomation: (AutomationEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var titleText by remember { mutableStateOf("") }
    var triggerText by remember { mutableStateOf("") }
    var actionText by remember { mutableStateOf("") }
    var deptText by remember { mutableStateOf("PERFORMANCE_ENGINE") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AutoMode, contentDescription = null, tint = ElectricCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "AUTOMATION ENGINE", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Règles autonomes 'Si Déclencheur -> Alors Action'", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // Add Rule Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("CRÉER UNE RÈGLE D'AUTOMATISATION", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        placeholder = { Text("Titre (ex: Anti-Surchauffe)", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auto_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricCyan, unfocusedBorderColor = CardBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedContainerColor = DarkNavy, unfocusedContainerColor = DarkNavy)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = triggerText,
                        onValueChange = { triggerText = it },
                        placeholder = { Text("Déclencheur (ex: TEMP_CPU > 45°C)", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auto_trigger_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricCyan, unfocusedBorderColor = CardBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedContainerColor = DarkNavy, unfocusedContainerColor = DarkNavy)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = actionText,
                        onValueChange = { actionText = it },
                        placeholder = { Text("Action (ex: Geler processus lourds)", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auto_action_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricCyan, unfocusedBorderColor = CardBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedContainerColor = DarkNavy, unfocusedContainerColor = DarkNavy)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (titleText.isNotBlank() && triggerText.isNotBlank() && actionText.isNotBlank()) {
                                onAddAutomation(titleText, triggerText, deptText, actionText)
                                titleText = ""
                                triggerText = ""
                                actionText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        modifier = Modifier.testTag("btn_add_automation")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = DeepObsidian)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ACTIVER LA RÈGLE", color = DeepObsidian, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("RÈGLES D'AUTOMATISATION ACTIVES (${automationsList.size})", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        items(automationsList) { auto ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(if (auto.isEnabled) ElectricCyan else CardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = auto.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "SI: ${auto.triggerCondition}", color = CyberAmber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "ALORS: ${auto.actionCommand}", color = ElectricCyan, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = auto.isEnabled,
                            onCheckedChange = { onToggleAutomation(auto) },
                            colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan, checkedTrackColor = GlowBlue)
                        )
                        IconButton(onClick = { onDeleteAutomation(auto) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberRed)
                        }
                    }
                }
            }
        }
    }
}
