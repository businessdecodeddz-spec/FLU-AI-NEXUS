package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BatteryStatEntity
import com.example.ui.components.HighDrainAlertCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.TelemetryState

@Composable
fun BatteryScreen(
    telemetry: TelemetryState,
    highDrainAlerts: List<BatteryStatEntity>,
    batteryStatsHistory: List<BatteryStatEntity>,
    onRestrictProcess: (BatteryStatEntity) -> Unit,
    onFreezeProcess: (BatteryStatEntity) -> Unit,
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
                Icon(imageVector = Icons.Default.BatteryChargingFull, contentDescription = null, tint = CyberGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "BATTERY INTELLIGENCE & HEALTH MONITOR", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Journalisation Room des statistiques & détection de processus énergivores", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // Live Battery Gauge Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberGreen)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("battery_health_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "${telemetry.batteryLevel}%", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 44.sp)
                    Text(text = "AUTONOMIE ESTIMÉE: 18H 45M", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Thermostat, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${telemetry.batteryTemp}°C", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(text = "|", color = TextMuted)

                        Text(text = "Santé: 96% (Tension: 3,850 mV)", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        // Section: High-Drain Background Process Alerts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.BatteryAlert, contentDescription = null, tint = CyberAmber)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ALERTES PROCESSUS ÉNERGIVORES EN ARRIÈRE-PLAN (${highDrainAlerts.size})",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        if (highDrainAlerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun processus d'arrière-plan anormale détecté.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(highDrainAlerts.take(4)) { alertStat ->
                HighDrainAlertCard(
                    stat = alertStat,
                    onRestrictProcess = onRestrictProcess,
                    onFreezeProcess = onFreezeProcess
                )
            }
        }

        // Section: Historical Room Telemetry Log
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, tint = ElectricCyan)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "HISTORIQUE PERSISTÉ ROOM (SAMPLES BATTERIE: ${batteryStatsHistory.size})",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        if (batteryStatsHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucune donnée de télémétrie enregistrée dans Room.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(batteryStatsHistory.take(5)) { stat ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Niveau: ${stat.batteryLevel}% | Temp: ${stat.batteryTemperature}°C | ${stat.chargingStatus}",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Top Consommateur: ${stat.topDrainAppName} (${stat.topDrainPowerConsumptionMah.toInt()} mAh)",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "${stat.drainRatePercentPerHour}%/h",
                            color = if (stat.isHighDrainAlert) CyberAmber else CyberGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
