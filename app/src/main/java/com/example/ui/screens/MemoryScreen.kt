package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NexusMemoryEntity
import com.example.ui.theme.*

@Composable
fun MemoryScreen(
    memoryList: List<NexusMemoryEntity>,
    onSaveMemory: (String, String, String) -> Unit,
    onDeleteMemory: (NexusMemoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var keyText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var categoryText by remember { mutableStateOf("USER_KNOWLEDGE") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = ElectricCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "KNOWLEDGE ENGINE & MÉMOIRE PERSISTANTE", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Stockage local contextuel SQLite / Room sans fuite externe", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // Add Memory Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("AJOUTER UN CONTEXTE EN MÉMOIRE", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = keyText,
                        onValueChange = { keyText = it },
                        placeholder = { Text("Clé (ex: PREF_THEME, REGLE_RESEAU)", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("memory_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricCyan, unfocusedBorderColor = CardBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedContainerColor = DarkNavy, unfocusedContainerColor = DarkNavy)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = contentText,
                        onValueChange = { contentText = it },
                        placeholder = { Text("Contenu du contexte...", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("memory_content_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricCyan, unfocusedBorderColor = CardBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedContainerColor = DarkNavy, unfocusedContainerColor = DarkNavy)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (keyText.isNotBlank() && contentText.isNotBlank()) {
                                onSaveMemory(keyText, categoryText, contentText)
                                keyText = ""
                                contentText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        modifier = Modifier.testTag("btn_save_memory")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = DeepObsidian)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ENREGISTRER EN MÉMOIRE", color = DeepObsidian, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("EXTRAITS DE MÉMOIRE STOCKÉS (${memoryList.size})", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        if (memoryList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucune mémoire enregistrée.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(memoryList) { mem ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = mem.key, color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(NeonPurple.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = mem.category, color = NeonPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = mem.content, color = TextPrimary, fontSize = 12.sp)
                        }
                        IconButton(onClick = { onDeleteMemory(mem) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Supprimer", tint = CyberRed)
                        }
                    }
                }
            }
        }
    }
}
