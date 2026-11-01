package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppRiskInfo
import com.example.ui.components.HighRiskAppCard
import com.example.ui.theme.*

@Composable
fun AppManagerScreen(
    highRiskApps: List<AppRiskInfo>,
    isScanning: Boolean,
    onTriggerPermissionScan: () -> Unit,
    onRemoveApp: (AppRiskInfo) -> Unit,
    onFreezeApp: (AppRiskInfo) -> Unit,
    onIgnoreApp: (AppRiskInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeRiskApps = highRiskApps.filter { !it.isIgnored }

    val sampleApps = listOf(
        Triple("FLU-AI NEXUS Core", "com.aistudio.flunexus", "Système / Souverain"),
        Triple("Navigateur Web", "com.android.chrome", "Internet / Transmissions"),
        Triple("Appareil Photo", "com.android.camera2", "Média / Capteurs"),
        Triple("Services Système", "com.android.systemui", "Système / Prioritaire")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Apps, contentDescription = null, tint = ElectricCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "APP MANAGER & PERMISSION AUDIT", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Analyse d'arrière-plan des applications suspectes", color = TextSecondary, fontSize = 11.sp)
                    }
                }

                IconButton(
                    onClick = onTriggerPermissionScan,
                    modifier = Modifier.testTag("btn_app_manager_rescan")
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ElectricCyan, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Scan", tint = ElectricCyan)
                    }
                }
            }
        }

        // Section: High-Risk Permission Apps
        item {
            Text(
                text = "APPLICATIONS À HAUT RISQUE DÉTECTÉES (${activeRiskApps.size})",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (activeRiskApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucune application présentant des permissions anormales.", color = TextMuted, fontSize = 12.sp)
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

        // General System Applications Section
        item {
            Text(
                text = "APPLICATIONS D'INFRASTRUCTURE SYSTÈME",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        items(sampleApps.size) { index ->
            val (appName, pkg, cat) = sampleApps[index]
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
                        Text(text = appName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = pkg, color = TextSecondary, fontSize = 11.sp)
                        Text(text = cat, color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepObsidian),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Text("INSPECTER", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
