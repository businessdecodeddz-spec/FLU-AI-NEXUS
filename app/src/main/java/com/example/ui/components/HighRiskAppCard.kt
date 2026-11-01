package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.SecurityUpdateWarning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppRiskInfo
import com.example.data.model.RiskLevel
import com.example.ui.theme.*

@Composable
fun HighRiskAppCard(
    app: AppRiskInfo,
    onRemoveApp: (AppRiskInfo) -> Unit,
    onFreezeApp: (AppRiskInfo) -> Unit,
    onIgnoreApp: (AppRiskInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val (badgeBg, badgeText, badgeBorder) = when (app.riskLevel) {
        RiskLevel.CRITICAL -> Triple(CyberRed.copy(alpha = 0.2f), CyberRed, CyberRed)
        RiskLevel.HIGH -> Triple(CyberAmber.copy(alpha = 0.2f), CyberAmber, CyberAmber)
        RiskLevel.MEDIUM -> Triple(ElectricCyan.copy(alpha = 0.15f), ElectricCyan, ElectricCyan)
        RiskLevel.LOW -> Triple(CyberGreen.copy(alpha = 0.15f), CyberGreen, CyberGreen)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (app.riskLevel == RiskLevel.CRITICAL) CyberRed else CardBorder)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_risk_card_${app.packageName}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: App Name & Risk Level Badge
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
                            .background(badgeBg, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (app.riskLevel == RiskLevel.CRITICAL) Icons.Default.Dangerous else Icons.Default.SecurityUpdateWarning,
                            contentDescription = null,
                            tint = badgeText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = app.appName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = app.packageName,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(6.dp))
                        .border(1.dp, badgeBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${app.riskLevel.name} RISK",
                        color = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dangerous Permissions Chips
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "PERMISSIONS CRITIQUES DÉTECTÉES :",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    app.dangerousPermissions.forEach { permLabel ->
                        Box(
                            modifier = Modifier
                                .background(DarkNavy, RoundedCornerShape(4.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = permLabel,
                                color = CyberAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Risk Description & Action Suggestion Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = app.riskDescription,
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "CONSEIL SYSTEM GUARD : ${app.suggestedAction}",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Uninstall / Remove Button
                Button(
                    onClick = { onRemoveApp(app) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_remove_app_${app.packageName}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Supprimer",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Freeze Button
                OutlinedButton(
                    onClick = { onFreezeApp(app) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (app.isFrozen) GlowBlue else CardBackground,
                        contentColor = ElectricCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_freeze_app_${app.packageName}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AcUnit,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (app.isFrozen) "Gelé" else "Geler L'App",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Ignore Button
                TextButton(
                    onClick = { onIgnoreApp(app) },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Ignorer",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
