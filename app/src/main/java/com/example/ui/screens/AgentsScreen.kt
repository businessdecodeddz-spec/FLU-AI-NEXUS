package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.core.ServiceRegistry
import com.example.core.agentic.MetaAgentStage
import com.example.core.agentic.MetaModuleProposal
import com.example.data.model.DepartmentInfo
import com.example.data.model.EngineInfo
import com.example.ui.theme.*
import com.example.ui.viewmodel.NexusViewModel

@Composable
fun AgentsScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val engines by viewModel.core.registry.engines.collectAsState()
    val departments by viewModel.core.registry.departments.collectAsState()
    val metaStage by viewModel.metaAgentStage.collectAsState()
    val metaLogs by viewModel.metaAgentLogs.collectAsState()
    val proposals by viewModel.metaModuleProposals.collectAsState()
    val integratedModules by viewModel.integratedMetaModules.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = 20 Départements, 1 = 10 Moteurs Centraux, 2 = Meta-Agent
    var unfulfilledInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(imageVector = Icons.Default.Hub, contentDescription = null, tint = ElectricCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "CENTRE DES AGENTS & DÉPARTEMENTS", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "10 Moteurs + 20 Départements + NEXUS META-AGENT", color = TextSecondary, fontSize = 11.sp)
            }
        }

        // Tabs Toggle
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DeepObsidian,
            contentColor = ElectricCyan,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("20 Départements (${departments.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("10 Moteurs (${engines.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("META-AGENT", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = CyberYellow) }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedTab == 0) {
                items(departments) { dept ->
                    DepartmentCardItem(dept = dept)
                }
            } else if (selectedTab == 1) {
                items(engines) { eng ->
                    EngineCardItem(eng = eng)
                }
            } else {
                // META-AGENT TAB
                item {
                    MetaAgentHeaderCard(stage = metaStage)
                }

                item {
                    MetaAgentTriggerCard(
                        inputText = unfulfilledInput,
                        onInputChange = { unfulfilledInput = it },
                        onTrigger = {
                            if (unfulfilledInput.isNotBlank()) {
                                viewModel.triggerMetaAgentCapabilitySynthesis(unfulfilledInput)
                                unfulfilledInput = ""
                            }
                        }
                    )
                }

                item {
                    MetaAgent9StepTracker(stage = metaStage)
                }

                if (proposals.isNotEmpty()) {
                    item {
                        Text(
                            text = "MODULES PROPOSÉS EN ATTENTE DE VALIDATION (${proposals.size})",
                            color = CyberYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    items(proposals) { proposal ->
                        MetaProposalCardItem(
                            proposal = proposal,
                            onApprove = { viewModel.approveMetaModule(proposal.proposalId) },
                            onReject = { viewModel.rejectMetaModule(proposal.proposalId) }
                        )
                    }
                }

                if (integratedModules.isNotEmpty()) {
                    item {
                        Text(
                            text = "MODULES INTÉGRÉS DANS L'ACTION REGISTRY (${integratedModules.size})",
                            color = CyberGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    items(integratedModules) { module ->
                        MetaIntegratedCardItem(module = module)
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "LOGS DU NEXUS META-AGENT", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            metaLogs.takeLast(10).forEach { log ->
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
            }
        }
    }
}

@Composable
private fun MetaAgentHeaderCard(stage: MetaAgentStage) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberYellow)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = CyberYellow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "NEXUS META-AGENT", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Box(
                    modifier = Modifier
                        .background(CyberYellow.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = stage.name, color = CyberYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Comprend ses propres limites fonctionnelles et conçoit de nouveaux modules logiciels en bac à sable. Ne contourne pas les protections Android et ne modifie pas ses propres privilèges.",
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "GARDE-FOU ENFORCED: Strict Android Permission Compliance / No Root Bypass",
                    color = CyberGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MetaAgentTriggerCard(
    inputText: String,
    onInputChange: (String) -> Unit,
    onTrigger: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepObsidian),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "SIMULATION DE CAPACITÉ MANQUANTE", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
                text = "Entrez un objectif que NEXUS ne sait pas accomplir (ex: 'Analyser spectre audio micro', 'Scanner appareils Bluetooth LE').",
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                placeholder = { Text("Ex: Capturer flux infrarouge caméra", color = TextMuted, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberYellow,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("meta_agent_input")
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onTrigger,
                colors = ButtonDefaults.buttonColors(containerColor = CyberYellow, contentColor = DeepObsidian),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_trigger_meta_agent")
            ) {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "DÉCLENCHER AUTO-CONCEPTION META-AGENT (9 ÉTAPES)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun MetaAgent9StepTracker(stage: MetaAgentStage) {
    val steps = listOf(
        "1. Identifier la capacité manquante",
        "2. Rechercher bibliothèque/méthode",
        "3. Étude de faisabilité & sécurité Android",
        "4. Concevoir le nouveau module",
        "5. Générer le code Kotlin",
        "6. Compilation bac à sable (Sandbox)",
        "7. Tests automatisés (3/3 PASS)",
        "8. Soumettre à la validation utilisateur",
        "9. Intégration dans ActionRegistry"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricBlue)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "WORKFLOW AGENTIQUE DES 9 ÉTAPES", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))

            steps.forEachIndexed { index, stepName ->
                val stepIndex = index + 1
                val isCompleted = stage.ordinal > stepIndex || stage == MetaAgentStage.INTEGRATED
                val isCurrent = stage.ordinal == stepIndex

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else if (isCurrent) Icons.Default.HourglassTop else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isCompleted) CyberGreen else if (isCurrent) CyberYellow else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stepName,
                        color = if (isCompleted) TextPrimary else if (isCurrent) CyberYellow else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaProposalCardItem(
    proposal: MetaModuleProposal,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepObsidian),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberYellow)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("proposal_card_${proposal.proposalId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = proposal.moduleName, color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Box(
                    modifier = Modifier
                        .background(CyberYellow.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = proposal.proposalId, color = CyberYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = proposal.description, color = TextSecondary, fontSize = 11.sp)
            Text(text = "Bibliothèque: ${proposal.libraryName}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)

            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = proposal.auditReport.auditSummary, color = CyberGreen, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Generated Code Box
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "Aperçu du Code Kotlin Généré (Sandbox Build Validé):", color = TextMuted, fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = proposal.generatedCode.take(200) + "...",
                        color = TextPrimary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = DeepObsidian),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_approve_proposal")
                ) {
                    Text(text = "AUTORISER & INTÉGRER", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberRed),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_reject_proposal")
                ) {
                    Text(text = "REFUSER", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun MetaIntegratedCardItem(module: MetaModuleProposal) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberGreen)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = module.moduleName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Text(text = "Capacité: ${module.capabilityName} | Ref: ${module.proposalId}", color = TextSecondary, fontSize = 10.sp)
            }
            Box(
                modifier = Modifier
                    .background(CyberGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = "OPÉRATIONNEL", color = CyberGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DepartmentCardItem(dept: DepartmentInfo) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dept_card_${dept.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = dept.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .background(ElectricBlue.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "Charge ${dept.loadPercentage.toInt()}%", color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = dept.description, color = TextSecondary, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { dept.loadPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = ElectricCyan,
                trackColor = CardBorder
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dept.capabilities.forEach { cap ->
                    Box(
                        modifier = Modifier
                            .background(DarkNavy, RoundedCornerShape(4.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = cap, color = TextMuted, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EngineCardItem(eng: EngineInfo) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepObsidian),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricBlue)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Memory, contentDescription = null, tint = ElectricCyan)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = eng.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = eng.role, color = TextSecondary, fontSize = 11.sp)
                }
            }
            Box(
                modifier = Modifier
                    .background(CyberGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = eng.status, color = CyberGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
