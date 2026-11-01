package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun OptimizationScreen(
    onOptimizeRam: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = ElectricCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "PERFORMANCE ENGINE & TURBOCOMPTE", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Accélération processeur et compaction mémoire BitNet", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // Optimization Action Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricCyan)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "TURBOCOMPTE ACCELERATOR v2.4", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Compaction intelligente de la RAM sans perte d'état app", color = TextSecondary, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onOptimizeRam,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        modifier = Modifier.testTag("btn_optimize_now")
                    ) {
                        Text("EXÉCUTER L'AUTO-TRIM TURBOCOMPTE", color = DeepObsidian, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(text = "ESTIMATIONS DE GAIN", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            GainEstimateCard("RAM Libérable", "~ 1,200 MB", "Compaction vectorielle", NeonPurple)
        }
        item {
            GainEstimateCard("Réactivité Processeur", "+ 24%", "Boost du CPU Governor", ElectricCyan)
        }
        item {
            GainEstimateCard("Consommation Énergie", "- 15%", "Refroidissement actif", CyberGreen)
        }
    }
}

@Composable
private fun GainEstimateCard(label: String, gain: String, detail: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = label, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = detail, color = TextSecondary, fontSize = 11.sp)
            }
            Text(text = gain, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
