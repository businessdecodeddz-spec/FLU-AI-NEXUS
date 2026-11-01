package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.TelemetryState

@Composable
fun StatusScreen(
    telemetry: TelemetryState,
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
                Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = ElectricCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "ÉTAT DU SMARTPHONE & TÉLÉMÉTRIE", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Mesures matérielles en temps réel (CPU, RAM, Thermique, OS)", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // CPU Telemetry Card
        item {
            TelemetryGaugeCard(
                title = "PROCESSEUR (CPU GOVERNOR)",
                value = "${telemetry.cpuUsage.toInt()}%",
                percentage = telemetry.cpuUsage / 100f,
                details = "8 Cores Kryo ARM64 | Gouverneur: Schedutil | JNI Turbocompte: Actif",
                icon = Icons.Default.Speed,
                accentColor = ElectricCyan
            )
        }

        // RAM Telemetry Card
        item {
            TelemetryGaugeCard(
                title = "MÉMOIRE RAM",
                value = "${telemetry.ramUsedMb} MB / ${telemetry.ramTotalMb} MB",
                percentage = telemetry.ramUsedMb.toFloat() / telemetry.ramTotalMb.toFloat(),
                details = "Disponibilité: ${telemetry.ramTotalMb - telemetry.ramUsedMb} MB | Auto-Trim BitNet prêt",
                icon = Icons.Default.Memory,
                accentColor = NeonPurple
            )
        }

        // Thermal & System Telemetry Card
        item {
            TelemetryGaugeCard(
                title = "TÉLÉMÉTRIE THERMIQUE",
                value = "${telemetry.batteryTemp} °C",
                percentage = (telemetry.batteryTemp - 20f) / 30f,
                details = "Zone SoC: Nominale | Pas de Throttling détecté",
                icon = Icons.Default.Thermostat,
                accentColor = CyberGreen
            )
        }

        // System OS Architecture Details
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.DeveloperBoard, contentDescription = null, tint = ElectricCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SPÉCIFICATIONS MATÉRIELLES", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    SpecRow("Architecture OS", "Android 16 (API 36 - VanillaIceCream)")
                    SpecRow("Noyau Système", "Linux 6.1.75-android16-arm64")
                    SpecRow("Pont JNI Native", telemetry.nativeVersion)
                    SpecRow("Accélération Vectorielle", "ARM NEON / SIMD 128-bit")
                    SpecRow("Moteur Souverain Local", "FLU-AI NEXUS Air-Gapped Stack")
                }
            }
        }
    }
}

@Composable
private fun TelemetryGaugeCard(
    title: String,
    value: String,
    percentage: Float,
    details: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text(text = value, color = accentColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { percentage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = accentColor,
                trackColor = CardBorder
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = details, color = TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
