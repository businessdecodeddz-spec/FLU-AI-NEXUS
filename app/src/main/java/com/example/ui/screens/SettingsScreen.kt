package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.jni.NexusJniBridge
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    var airGappedMode by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = ElectricCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "PARAMÈTRES & CENTRAL DE PERMISSIONS", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Configuration du système souverain et clés d'API", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // Sovereign Air-Gapped Mode Toggle
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Mode 100% Souverain (Air-Gapped)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Exécution locale uniquement. Aucune télémétrie cloud externe.", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = airGappedMode,
                        onCheckedChange = { airGappedMode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberGreen, checkedTrackColor = GlowCyan),
                        modifier = Modifier.testTag("toggle_airgapped")
                    )
                }
            }
        }

        // Native JNI Bridge Info
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricBlue)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = ElectricCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "PONT JNI RUST / C++", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Statut: ${NexusJniBridge.getBridgeStatus()}", color = CyberGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Version Noyau: ${NexusJniBridge.getCoreVersion()}", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // Gemini API Configuration Guidance
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = CyberAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "CLÉ DE SERVICE GEMINI (AI STUDIO)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Les clés d'API sont gérées de manière sécurisée via le panneau Secrets AI Studio (Injecté via BuildConfig). Les clés ne sont jamais codées en dur.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Central Permissions List
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = ElectricCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "PERMISSIONS ANDROID ACCORDÉES", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionItem("INTERNET (Connexion Sockets)", true)
                    PermissionItem("ACCESS_NETWORK_STATE (Télémétrie)", true)
                    PermissionItem("POST_NOTIFICATIONS (Canal Alerte)", true)
                    PermissionItem("QUERY_ALL_PACKAGES (Inspection App Manager)", true)
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(name: String, granted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, color = TextSecondary, fontSize = 11.sp)
        Text(text = if (granted) "ACCORDÉ" else "NON ACCORDÉ", color = if (granted) CyberGreen else CyberRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
