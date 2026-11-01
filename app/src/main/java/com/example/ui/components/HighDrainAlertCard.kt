package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BatteryStatEntity
import com.example.ui.theme.*

@Composable
fun HighDrainAlertCard(
    stat: BatteryStatEntity,
    onRestrictProcess: (BatteryStatEntity) -> Unit,
    onFreezeProcess: (BatteryStatEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberAmber)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("high_drain_card_${stat.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: App Name, Drain Rate & Alert Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(CyberAmber.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryAlert,
                            contentDescription = null,
                            tint = CyberAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stat.topDrainAppName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = stat.topDrainPackageName,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(CyberAmber.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .border(1.dp, CyberAmber, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${stat.drainRatePercentPerHour}% / heure",
                        color = CyberAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Alert details box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CONSOMMATION : ${stat.topDrainPowerConsumptionMah.toInt()} mAh en arrière-plan",
                            color = CyberAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (stat.alertReason.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stat.alertReason,
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mitigation Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Restrict Background Process
                Button(
                    onClick = { onRestrictProcess(stat) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberAmber),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_restrict_drain_${stat.id}")
                ) {
                    Icon(imageVector = Icons.Default.PauseCircle, contentDescription = null, tint = DeepObsidian, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restreindre", color = DeepObsidian, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Freeze High Drain App Process
                OutlinedButton(
                    onClick = { onFreezeProcess(stat) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_freeze_drain_${stat.id}")
                ) {
                    Icon(imageVector = Icons.Default.AcUnit, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Geler Processus", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
