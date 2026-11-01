package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionRecordEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun HistoryScreen(
    actionHistory: List<ActionRecordEntity>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.FactCheck, contentDescription = null, tint = CyberGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "HISTORIQUE ET REGISTRE DES PREUVES", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Audit Ledger cryptographique des opérations vérifiées", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        if (actionHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground, RoundedCornerShape(12.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucune preuve enregistrée dans le registre.", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            items(actionHistory) { action ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = action.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            StatusBadge(state = action.state)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Action ID: ${action.actionId} | Dept: ${action.departmentId} | Engine: ${action.engineId}", color = TextSecondary, fontSize = 11.sp)
                        Text(text = "Détails: ${action.details}", color = TextMuted, fontSize = 11.sp)

                        if (action.verificationProof.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkNavy, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(text = action.verificationProof, color = CyberGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    if (action.signature.isNotEmpty()) {
                                        Text(
                                            text = "SHA256: ${action.signature}",
                                            color = ElectricCyan,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
