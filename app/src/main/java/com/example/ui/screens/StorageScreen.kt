package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.cloud.DriveConnectionState
import com.example.ui.theme.*
import com.example.ui.viewmodel.NexusViewModel

@Composable
fun StorageScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val quotaStatus by viewModel.storageQuotaStatus.collectAsState()
    val systemItems by viewModel.systemStorageItems.collectAsState()
    val userItems by viewModel.userStorageItems.collectAsState()
    val governorLogs by viewModel.storageGovernorLogs.collectAsState()
    val driveConnectionState by viewModel.driveConnectionState.collectAsState()
    val lastDriveSyncProof by viewModel.lastDriveSyncProof.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Hybride Cloud Governor, 1 = Exploration Locale
    var oauthTokenInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, tint = ElectricCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "NEXUS CLOUD INTELLIGENCE & STORAGE GOVERNOR",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Architecture de Stockage Hybride à 2 Espaces Isolés (Plafond Système 1 Go strict)",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Tabs Toggle
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DeepObsidian,
                contentColor = ElectricCyan,
                modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Gouvernance Hybride Cloud", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Stockage Local Appareil", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
            }
        }

        if (selectedTab == 0) {
            // 0. GOOGLE DRIVE API v3 SERVICE LAYER CARD
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricBlue)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CloudQueue, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "SERVICE LAYER GOOGLE DRIVE API v3", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            val badgeColor = when (driveConnectionState) {
                                DriveConnectionState.CONNECTED_VERIFIED, DriveConnectionState.AUTHENTICATED_READY -> CyberGreen
                                DriveConnectionState.SYNC_IN_PROGRESS -> CyberAmber
                                DriveConnectionState.NETWORK_OR_API_ERROR -> CyberRed
                                DriveConnectionState.UNAUTHENTICATED_OAUTH_REQUIRED -> CyberYellow
                            }
                            Box(
                                modifier = Modifier
                                    .background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = driveConnectionState.name, color = badgeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "État Connecteur: ${quotaStatus.cloudConnectorState}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Projet Cloud: ${com.example.data.cloud.GoogleDriveStorageService.PROVISIONED_PROJECT_ID} | Client OAuth: ${com.example.data.cloud.GoogleDriveStorageService.PROVISIONED_OAUTH_CLIENT_ID.take(26)}...",
                            color = ElectricCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Scopes OAuth2: drive.appdata (Système 1 Go max) | drive.file (Dossier NEXUS_USER_STORAGE)",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (lastDriveSyncProof != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Dernière Preuve API: $lastDriveSyncProof",
                                color = CyberGreen,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = oauthTokenInput,
                            onValueChange = { oauthTokenInput = it },
                            label = { Text("Jeton OAuth2 Bearer Google Drive (optionnel en mode local)", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_drive_oauth_token"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.configureGoogleDriveAccessToken(oauthTokenInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_configure_drive_oauth")
                            ) {
                                Text("Configurer Jeton OAuth", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.synchronizeGoogleDriveStorage() },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DeepObsidian),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_sync_google_drive")
                            ) {
                                Text("Synchroniser Drive v3", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 1. NEXUS SYSTEM STORAGE CARD (1 GB Absolute Ceiling Cap Enforced)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricCyan)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "1. NEXUS SYSTEM STORAGE (appDataFolder)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .background(ElectricCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "PLAFOND STRICT 1 GO (1000 MO)", color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Contient exclusivement les configurations, index, journaux techniques et métadonnées dans l'espace isolé Google Drive appDataFolder. Règle absolue de non-dépassement (1000 Mo).",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Consommation Système:", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = "${"%.1f".format(quotaStatus.systemUsedMb)} Mo / 1000.0 Mo (${"%.1f".format(quotaStatus.systemUsagePercentage)}%)",
                                color = if (quotaStatus.isSystemCapExceeded) CyberRed else CyberGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (quotaStatus.systemUsedMb / 1000.0f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = if (quotaStatus.isSystemCapExceeded) CyberRed else ElectricCyan,
                            trackColor = CardBorder
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        systemItems.take(4).forEach { item ->
                            Text(
                                text = "• ${item.name} (${item.sizeMb} Mo) -> [${item.driveSpaceTarget}] ID:${item.remoteDriveId ?: "N/A"}",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // 2. NEXUS USER STORAGE CARD (Google Drive Personal Quota)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberGreen)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Cloud, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "2. NEXUS USER STORAGE (Google Drive)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .background(CyberGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "QUOTA GOOGLE DRIVE", color = CyberGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Contient les documents personnels, photos, vidéos, projets et archives dans le dossier isolé NEXUS_USER_STORAGE. Aucune limite fixe de 1 Go ne lui est appliquée.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Quota Drive Utilisateur:", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = "${"%.1f".format(quotaStatus.userDriveUsedGb)} GB / ${"%.1f".format(quotaStatus.userDriveTotalGb)} GB",
                                color = CyberGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        userItems.take(4).forEach { item ->
                            Text(
                                text = "• ${item.name} (${item.sizeMb} Mo) -> [${item.driveSpaceTarget}] ID:${item.remoteDriveId ?: "N/A"}",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // 3. STORAGE GOVERNOR CONTROLS
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "CONTRÔLES DU STORAGE GOVERNOR", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.writeSystemConfigData("test_sys_snapshot.db", 45f) },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_test_sys_write")
                            ) {
                                Text("+ Écrire Système (45 Mo)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.writeUserPersonalFile("Archive_Projet_3D.zip", 320f) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = DeepObsidian),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_test_user_write")
                            ) {
                                Text("+ Écrire Personnel Drive", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.verifyStorageIsolation() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberYellow, contentColor = DeepObsidian),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_verify_storage_isolation")
                        ) {
                            Icon(imageVector = Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AUDITER L'ISOLATION & PREUVE DE PLAFOND 1 GO", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // 4. STORAGE GOVERNOR LOGS
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "TRAÇABILITÉ GOVERNOR & DÉPARTS CLOUD", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        governorLogs.takeLast(8).forEach { log ->
                            Text(
                                text = log,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

        } else {
            // LOCAL STORAGE BREAKDOWN
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "OCCUPATION DU STOCKAGE INTERNE", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "${telemetry.storageUsedGb} GB / ${telemetry.storageTotalGb} GB", color = CyberAmber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { telemetry.storageUsedGb / telemetry.storageTotalGb },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = CyberAmber,
                            trackColor = CardBorder
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Espace disponible local: ${(telemetry.storageTotalGb - telemetry.storageUsedGb).toInt()} GB", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberAmber)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Purge du Cache Temporaire", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Nettoyage intelligent des résidus temporaires localement", color = TextSecondary, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.triggerQuickAction("Purge du cache temporaire", "STORAGE_INTELLIGENCE", true) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberAmber),
                            modifier = Modifier.testTag("btn_clean_cache")
                        ) {
                            Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, tint = DeepObsidian, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PURGER", color = DeepObsidian, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
