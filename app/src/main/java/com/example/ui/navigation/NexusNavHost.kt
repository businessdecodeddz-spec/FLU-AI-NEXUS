package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ActionDialog
import com.example.ui.components.NexusBottomBar
import com.example.ui.components.NexusHeader
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.NexusViewModel
import kotlinx.coroutines.launch

@Composable
fun NexusNavHost(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(NexusScreen.Dashboard) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val telemetry by viewModel.telemetry.collectAsState()
    val actionHistory by viewModel.actionHistory.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val memoryList by viewModel.memoryList.collectAsState()
    val automationsList by viewModel.automationsList.collectAsState()
    val currentMedicalScan by viewModel.currentMedicalScan.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val pendingAction by viewModel.pendingActionForApproval.collectAsState()
    val highRiskApps by viewModel.highRiskApps.collectAsState()
    val isScanningPermissions by viewModel.isScanningPermissions.collectAsState()
    val redTeamLogs by viewModel.redTeamLogs.collectAsState()
    val isRedTeamActive by viewModel.isRedTeamActive.collectAsState()
    val highDrainAlerts by viewModel.highDrainAlerts.collectAsState()
    val batteryStatsHistory by viewModel.batteryStatsHistory.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DeepObsidian,
                drawerContentColor = TextPrimary,
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "FLU-AI NEXUS", color = ElectricCyan, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text(text = "16 Navigation Consoles", color = TextSecondary, fontSize = 11.sp)
                        }
                        IconButton(onClick = { scope.launch { drawerState.close() } }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = CardBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(NexusScreen.entries.toTypedArray()) { screen ->
                            val isSelected = currentScreen == screen
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (isSelected) GlowBlue else androidx.compose.ui.graphics.Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) ElectricCyan else androidx.compose.ui.graphics.Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        currentScreen = screen
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("drawer_item_${screen.route}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    tint = if (isSelected) ElectricCyan else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = screen.title,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                NexusHeader(
                    telemetry = telemetry,
                    onOpenMenu = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = {
                NexusBottomBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it }
                )
            },
            containerColor = DarkNavy,
            modifier = modifier
        ) { innerPadding ->

            val contentModifier = Modifier.padding(innerPadding)

            when (currentScreen) {
                NexusScreen.Dashboard -> DashboardScreen(
                    telemetry = telemetry,
                    actionHistory = actionHistory,
                    onTriggerQuickAction = { goal, dept, isDestructive ->
                        viewModel.triggerQuickAction(goal, dept, isDestructive)
                    },
                    onNavigateToScreen = { currentScreen = it },
                    modifier = contentModifier
                )
                NexusScreen.Assistant -> AssistantScreen(
                    messages = chatMessages,
                    onSendMessage = { viewModel.sendUserChatMessage(it) },
                    modifier = contentModifier
                )
                NexusScreen.Agents -> AgentsScreen(
                    viewModel = viewModel,
                    modifier = contentModifier
                )
                NexusScreen.Status -> StatusScreen(
                    telemetry = telemetry,
                    modifier = contentModifier
                )
                NexusScreen.Security -> SecurityScreen(
                    highRiskApps = highRiskApps,
                    isScanning = isScanningPermissions,
                    redTeamLogs = redTeamLogs,
                    isRedTeamActive = isRedTeamActive,
                    onTriggerScan = { viewModel.triggerQuickAction("Audit complet de sécurité", "SYSTEM_GUARD", false) },
                    onTriggerPermissionScan = { viewModel.runAutomatedAppPermissionScan() },
                    onRunRedTeamSimulation = { viewModel.runRedTeamSimulation(it) },
                    onRemoveApp = { viewModel.removeOrUninstallApp(it) },
                    onFreezeApp = { viewModel.freezeApp(it) },
                    onIgnoreApp = { viewModel.ignoreAppRisk(it) },
                    modifier = contentModifier
                )
                NexusScreen.Optimization -> OptimizationScreen(
                    onOptimizeRam = { viewModel.triggerQuickAction("Optimisation RAM Turbocompte", "PERFORMANCE_ENGINE", false) },
                    modifier = contentModifier
                )
                NexusScreen.Storage -> StorageScreen(
                    viewModel = viewModel,
                    modifier = contentModifier
                )
                NexusScreen.Network -> NetworkScreen(
                    telemetry = telemetry,
                    modifier = contentModifier
                )
                NexusScreen.Battery -> BatteryScreen(
                    telemetry = telemetry,
                    highDrainAlerts = highDrainAlerts,
                    batteryStatsHistory = batteryStatsHistory,
                    onRestrictProcess = { viewModel.restrictBatteryProcess(it) },
                    onFreezeProcess = { viewModel.freezeBatteryProcess(it) },
                    modifier = contentModifier
                )
                NexusScreen.Apps -> AppManagerScreen(
                    highRiskApps = highRiskApps,
                    isScanning = isScanningPermissions,
                    onTriggerPermissionScan = { viewModel.runAutomatedAppPermissionScan() },
                    onRemoveApp = { viewModel.removeOrUninstallApp(it) },
                    onFreezeApp = { viewModel.freezeApp(it) },
                    onIgnoreApp = { viewModel.ignoreAppRisk(it) },
                    modifier = contentModifier
                )
                NexusScreen.Developer -> DeveloperScreen(
                    terminalLogs = terminalLogs,
                    onExecuteCommand = { viewModel.executeTerminalCommand(it) },
                    modifier = contentModifier
                )
                NexusScreen.Research -> ResearchScreen(
                    currentScan = currentMedicalScan,
                    onTriggerMedicalScan = { modality, area -> viewModel.runMedicalScanSimulation(modality, area) },
                    modifier = contentModifier
                )
                NexusScreen.Memory -> MemoryScreen(
                    memoryList = memoryList,
                    onSaveMemory = { k, cat, c -> viewModel.saveNewMemory(k, cat, c) },
                    onDeleteMemory = { viewModel.deleteMemory(it) },
                    modifier = contentModifier
                )
                NexusScreen.Automations -> AutomationsScreen(
                    automationsList = automationsList,
                    onAddAutomation = { t, trig, dept, act -> viewModel.addAutomation(t, trig, dept, act) },
                    onToggleAutomation = { viewModel.toggleAutomation(it) },
                    onDeleteAutomation = { viewModel.deleteAutomation(it) },
                    modifier = contentModifier
                )
                NexusScreen.AgentStore -> com.example.ui.screens.AgentStoreScreen(
                    viewModel = viewModel,
                    modifier = contentModifier
                )
                NexusScreen.History -> HistoryScreen(
                    actionHistory = actionHistory,
                    modifier = contentModifier
                )
                NexusScreen.Settings -> SettingsScreen(
                    modifier = contentModifier
                )
            }

            // High risk action approval dialog
            if (pendingAction != null) {
                ActionDialog(
                    action = pendingAction!!,
                    onConfirm = { viewModel.confirmPendingAction(true) },
                    onDismiss = { viewModel.confirmPendingAction(false) }
                )
            }
        }
    }
}
