package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionRecordEntity
import com.example.ui.components.CoreVisualizer
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.NexusScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.TelemetryState

@Composable
fun DashboardScreen(
    telemetry: TelemetryState,
    actionHistory: List<ActionRecordEntity>,
    onTriggerQuickAction: (String, String, Boolean) -> Unit,
    onNavigateToScreen: (NexusScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Central Core Visualizer Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_core_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NEXUS INTELLIGENCE CORE",
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(CyberGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("10 MOTEURS ACTIFS", color = CyberGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Core Orb
                    CoreVisualizer(size = 170.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Système Souverain en Ligne",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = telemetry.jniStatus,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Quick Metrics Dashboard Row
        item {
            Text(
                text = "TÉLÉMÉTRIE TEMPS RÉEL",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // CPU Card
                MetricBox(
                    title = "CPU Governor",
                    value = "${telemetry.cpuUsage.toInt()}%",
                    subtitle = "8 Cores Active",
                    accentColor = ElectricCyan,
                    modifier = Modifier.weight(1f)
                )
                // RAM Card
                MetricBox(
                    title = "Mémoire RAM",
                    value = "${telemetry.ramUsedMb} MB",
                    subtitle = "sur ${telemetry.ramTotalMb} MB",
                    accentColor = NeonPurple,
                    modifier = Modifier.weight(1f)
                )
                // Battery Card
                MetricBox(
                    title = "Batterie",
                    value = "${telemetry.batteryLevel}%",
                    subtitle = "${telemetry.batteryTemp}°C Normal",
                    accentColor = CyberGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action Center - 4 Quick Commands
        item {
            Text(
                text = "COMMANDES RAPIDES",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickActionButton(
                        title = "Optimiser RAM",
                        description = "Turbocompte Auto-Trim",
                        icon = Icons.Default.Speed,
                        accentColor = ElectricCyan,
                        onClick = { onTriggerQuickAction("Optimisation RAM Turbocompte", "PERFORMANCE_ENGINE", false) },
                        modifier = Modifier.weight(1f).testTag("btn_quick_ram")
                    )
                    QuickActionButton(
                        title = "Scan Sécurité",
                        description = "System Guard & Threat Check",
                        icon = Icons.Default.Security,
                        accentColor = CyberGreen,
                        onClick = { onTriggerQuickAction("Audit complet de sécurité", "SYSTEM_GUARD", false) },
                        modifier = Modifier.weight(1f).testTag("btn_quick_security")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickActionButton(
                        title = "Imagerie Médicale",
                        description = "BizTalk-Chronos Pipeline",
                        icon = Icons.Default.Science,
                        accentColor = NeonPurple,
                        onClick = { onNavigateToScreen(NexusScreen.Research) },
                        modifier = Modifier.weight(1f).testTag("btn_quick_medical")
                    )
                    QuickActionButton(
                        title = "Nettoyage Cache",
                        description = "Storage Intelligence Purge",
                        icon = Icons.Default.CleaningServices,
                        accentColor = CyberAmber,
                        onClick = { onTriggerQuickAction("Purge du cache temporaire", "STORAGE_INTELLIGENCE", true) },
                        modifier = Modifier.weight(1f).testTag("btn_quick_clean")
                    )
                }
            }
        }

        // Recent Audit Proofs Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REGISTRE DES PREUVES (AUDIT LEDGER)",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Voir Tout",
                    color = ElectricCyan,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onNavigateToScreen(NexusScreen.History) }
                )
            }
        }

        if (actionHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucune action journalisée pour le moment.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(actionHistory.take(4)) { action ->
                ActionHistoryItemCard(action = action)
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    subtitle: String,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(CardBackground, RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(text = title, color = TextSecondary, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = accentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = TextMuted, fontSize = 9.sp)
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = CardBackground,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = description, color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun ActionHistoryItemCard(action: ActionRecordEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(10.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = action.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Dept: ${action.departmentId} | Engine: ${action.engineId}", color = TextSecondary, fontSize = 11.sp)
                if (action.verificationProof.isNotEmpty()) {
                    Text(text = action.verificationProof, color = ElectricCyan, fontSize = 9.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(state = action.state)
        }
    }
}
