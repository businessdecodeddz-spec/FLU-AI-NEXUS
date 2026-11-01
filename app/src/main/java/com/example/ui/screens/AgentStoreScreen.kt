package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.monetization.NexusTier
import com.example.core.monetization.SpecializedAgentItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.NexusViewModel

@Composable
fun AgentStoreScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val accountState by viewModel.monetizationManager.accountState.collectAsState()
    val storeAgents by viewModel.monetizationManager.storeAgents.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Agent Store, 1: Abonnements

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NEXUS AGENT STORE & ABONNEMENTS",
                        color = ElectricCyan,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Boutique d'Agents Spécialisés & Offres Commerciales FLU-AI",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Surface(
                    color = NeonViolet.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonViolet)
                ) {
                    Text(
                        text = accountState.currentTier.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Tab Selector
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DeepObsidian,
                contentColor = ElectricCyan,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("NEXUS Agent Store (${storeAgents.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Grille des Offres & Tarifs", fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (selectedTab == 0) {
            // AGENT STORE VIEW
            item {
                Text(
                    text = "Agents IA Métier & Spécialisés",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Déployez des agents spécialisés dans l'orchestrateur central pour accomplir des missions complexes.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            items(storeAgents.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { agent ->
                        Box(modifier = Modifier.weight(1f)) {
                            AgentCard(
                                agent = agent,
                                onAcquire = { viewModel.monetizationManager.acquireAgent(agent.agentId) }
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            // SUBSCRIPTION TIERS VIEW
            item {
                Text(
                    text = "Formules d'Abonnement Commerciales",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            items(NexusTier.values()) { tier ->
                val isCurrent = accountState.currentTier == tier
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.monetizationManager.selectTier(tier) }
                        .testTag("tier_card_${tier.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) DeepObsidian else CardBackground
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isCurrent) ElectricCyan else CardBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tier.title,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "[ACTIF]",
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tier.description,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = tier.monthlyPriceLabel,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // NON-NEGOTIABLE FREE SECURITY GUARANTEE CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = "Shield", tint = EmeraldGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Garantie Éthique : Sécurité & Contrôle 100% Gratuits",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Les fonctions fondamentales de sécurité, l'accès à vos données, le contrôle des permissions, la transparence des preuves SHA-256 et le bouton d'urgence (Kill-Switch) restent GRATUITS SANS RESTRICTION.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AgentCard(
    agent: SpecializedAgentItem,
    onAcquire: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("agent_card_${agent.agentId}"),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (agent.isAcquired) EmeraldGreen.copy(alpha = 0.5f) else CardBorder
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = agent.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = agent.priceLabel,
                    color = if (agent.isFreeTier) EmeraldGreen else ElectricCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = agent.category,
                color = TextSecondary,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = agent.missionDescription,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onAcquire,
                enabled = !agent.isAcquired,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (agent.isAcquired) DeepObsidian else ElectricCyan,
                    contentColor = if (agent.isAcquired) EmeraldGreen else DeepObsidian
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (agent.isAcquired) "Déployé ✓" else "Activer l'Agent",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
