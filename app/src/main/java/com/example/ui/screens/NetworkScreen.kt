package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Wifi
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
fun NetworkScreen(
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
                Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = ElectricCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "NETWORK COMMAND & BIZTALK ROUTING", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Gestion prioritaire du Wi-Fi / 5G et du Data Hub BizTalk", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Router, contentDescription = null, tint = ElectricCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("MÉTRIQUES RÉSEAU & DEBIT LIVE", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        NetworkStatItem("TÉLÉCHARGEMENT", "${telemetry.downloadSpeedKbps / 1000f} Mbps", ElectricCyan)
                        NetworkStatItem("ENVOI (UPLOAD)", "${telemetry.uploadSpeedKbps / 1000f} Mbps", NeonPurple)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricBlue)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("BIZTALK DATA HUB BUS", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Canal d'intégration de données inter-modules souverain", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Status: ACTIVE (Air-Gapped Local Host)", color = CyberGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("• Routage Imagerie Médicale: Port 8090 / Chronos Sync", color = TextMuted, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun NetworkStatItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
