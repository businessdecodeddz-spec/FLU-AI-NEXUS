package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.RedTeamOperationResult
import com.example.data.model.AppRiskInfo
import com.example.ui.components.HighRiskAppCard
import com.example.ui.theme.*

@Composable
fun SecurityScreen(
    highRiskApps: List<AppRiskInfo>,
    isScanning: Boolean,
    redTeamLogs: List<RedTeamOperationResult>,
    isRedTeamActive: Boolean,
    onTriggerScan: () -> Unit,
    onTriggerPermissionScan: () -> Unit,
    onRunRedTeamSimulation: (String) -> Unit,
    onRemoveApp: (AppRiskInfo) -> Unit,
    onFreezeApp: (AppRiskInfo) -> Unit,
    onIgnoreApp: (AppRiskInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeRiskApps = highRiskApps.filter { !it.isIgnored }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = CyberGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "CENTRE DE SÉCURITÉ & CYBER RED TEAM", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Simulateur d'Attaque Offensive MITRE ATT&CK + Protection Zero-Trust", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // Overall Shield Status
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (activeRiskApps.isNotEmpty()) CyberRed else CyberGreen)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (activeRiskApps.isNotEmpty()) Icons.Default.BugReport else Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = if (activeRiskApps.isNotEmpty()) CyberRed else CyberGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (activeRiskApps.isNotEmpty()) "${activeRiskApps.size} APPS À HAUT RISQUE DÉTECTÉES" else "SYSTÈME INTÈGRE & SÉCURISÉ",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (activeRiskApps.isNotEmpty()) "Analyse d'arrière-plan: Permissions dangereuses détectées." else "Analyse en arrière-plan active. Aucune menace.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onTriggerScan,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                            modifier = Modifier.testTag("security_scan_button")
                        ) {
                            Text("AUDIT COMPLET", color = DeepObsidian, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onTriggerPermissionScan,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                            modifier = Modifier.testTag("btn_permission_rescan")
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ElectricCyan, strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SCAN PERMISSIONS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Cybersecurity Red Team Offensive Simulation Console
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberRed)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = CyberRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "CONSOLE ADVERSAIRE RED TEAM (CYBER ATTACK SIMULATOR)", color = CyberRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        if (isRedTeamActive) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyberRed, strokeWidth = 2.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Simule des tactiques d'attaque réelles (Phishing Overlay, Accessibilité Keylogger, Exfiltration Réseau) pour éprouver la résistance du smartphone.", color = TextSecondary, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onRunRedTeamSimulation("OVERLAY") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberAmber),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberAmber),
                            modifier = Modifier.weight(1f).testTag("btn_redteam_overlay")
                        ) {
                            Text("Test Overlay", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onRunRedTeamSimulation("ACCESSIBILITY") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberRed),
                            modifier = Modifier.weight(1f).testTag("btn_redteam_access")
                        ) {
                            Text("Test Keylogger", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onRunRedTeamSimulation("EXFILTRATION") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan),
                            modifier = Modifier.weight(1f).testTag("btn_redteam_exfil")
                        ) {
                            Text("Test Exfiltration", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (redTeamLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val lastResult = redTeamLogs.first()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkNavy, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = lastResult.mitreAttackCode, color = CyberRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "RISQUE: ${lastResult.riskLevel.name}", color = CyberAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = lastResult.details, color = TextPrimary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "CONTRE-MESURE: ${lastResult.mitigationSuggested}", color = CyberGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "Signature Audit: ${lastResult.auditSignature}", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        // Section: Scanned Apps with High-Risk Permissions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "APPLICATION À RISQUE DE PERMISSION (${activeRiskApps.size})",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        if (activeRiskApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Toutes les applications analysées respectent les règles de sécurité.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(activeRiskApps) { app ->
                HighRiskAppCard(
                    app = app,
                    onRemoveApp = onRemoveApp,
                    onFreezeApp = onFreezeApp,
                    onIgnoreApp = onIgnoreApp
                )
            }
        }

        // Security Modules Breakdown
        item {
            Text(text = "MODULES DE PROTECTION RUST / KOTLIN", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        item {
            SecurityModuleCard(
                name = "System Guard",
                description = "Intégrité du noyau SEAndroid & vérification des binaire système",
                status = "PROTECTION ACTIVE",
                icon = Icons.Default.Shield,
                accentColor = CyberGreen
            )
        }

        item {
            SecurityModuleCard(
                name = "Cyber Defense",
                description = "Pare-feu applicatif, détection des signatures malveillantes et blocage d'exfiltration",
                status = "FILTRAGE SOCKS5 EN LIGNE",
                icon = Icons.Default.Lock,
                accentColor = ElectricCyan
            )
        }

        item {
            SecurityModuleCard(
                name = "Privacy Guard",
                description = "Surveillance du microphone, caméra et accès données sensibles par les applications tiers",
                status = "KILLSWITCH OPÉRATIONNEL",
                icon = Icons.Default.Lock,
                accentColor = NeonPurple
            )
        }
    }
}

@Composable
private fun SecurityModuleCard(
    name: String,
    description: String,
    status: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = description, color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = status, color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
