package com.example.core.monetization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class NexusTier(val id: String, val title: String, val monthlyPriceLabel: String, val description: String) {
    FREE("free", "NEXUS Free", "Gratuit", "Point d'entrée gratuit: Assistant IA, Diagnostic de base, Batterie, 1000 Mo Système"),
    PLUS("plus", "NEXUS Plus", "499 DA / mois", "Utilisateur individuel: Agent Autonome de base & Stockage Cloud étendu"),
    PREMIUM("premium", "NEXUS Premium", "1 200 DA / mois", "Utilisateur avancé: Multi-Agent Swarm, Recherche Avancée & Mémoire Contextuelle"),
    ULTIMATE("ultimate", "NEXUS Ultimate", "2 500 DA / mois", "Utilisateur expert: Automatisation inter-applications, IA offline & Priorité Cloud"),
    DEVELOPER("developer", "NEXUS Developer", "3 500 DA / mois", "Développeurs & Ingénieurs: Rust/C++/Kotlin, Git, Refactoring & Sandbox CLI"),
    BUSINESS("business", "NEXUS Business", "Sur Devis", "Entreprises & Équipes: Agents Métier sur-mesure, Rapports automatiques & Multi-appareil")
}

data class SpecializedAgentItem(
    val agentId: String,
    val name: String,
    val category: String,
    val missionDescription: String,
    val priceLabel: String,
    val iconName: String,
    val isAcquired: Boolean = false,
    val isFreeTier: Boolean = false
)

data class CommercialAccountState(
    val currentTier: NexusTier = NexusTier.PREMIUM,
    val availableCredits: Int = 1500,
    val acquiredAgentsCount: Int = 4,
    val isSecurityModuleUnlocked: Boolean = true,
    val isDeveloperModuleUnlocked: Boolean = true
)

class NexusMonetizationManager {

    private val _accountState = MutableStateFlow(CommercialAccountState())
    val accountState: StateFlow<CommercialAccountState> = _accountState.asStateFlow()

    private val _storeAgents = MutableStateFlow<List<SpecializedAgentItem>>(emptyList())
    val storeAgents: StateFlow<List<SpecializedAgentItem>> = _storeAgents.asStateFlow()

    private val _monetizationLogs = MutableStateFlow<List<String>>(emptyList())
    val monetizationLogs: StateFlow<List<String>> = _monetizationLogs.asStateFlow()

    init {
        initStoreCatalog()
    }

    private fun initStoreCatalog() {
        _storeAgents.value = listOf(
            SpecializedAgentItem("AGT-FIN", "Financial Agent", "Finance & Budget", "Analyse automatique des dépenses, factures et budgets personnels.", "Inclus Premium", "AttachMoney", isAcquired = true),
            SpecializedAgentItem("AGT-LEG", "Legal Assistant", "Droit & Contrats", "Analyse documentaire juridique et vérification de conformité des clauses.", "250 Crédits", "Gavel", isAcquired = false),
            SpecializedAgentItem("AGT-ENG", "Engineer Agent", "Ingénierie & Tech", "Assistance technique, schémas, calculs et conversion de projets industriels.", "Inclus Premium", "Engineering", isAcquired = true),
            SpecializedAgentItem("AGT-RES", "Research Agent", "Science & Recherche", "Recherche scientifique approfondie, synthèse d'articles et recoupement.", "Inclus Premium", "Science", isAcquired = true),
            SpecializedAgentItem("AGT-DEV", "Developer Agent", "Programmation", "Génération de code Rust/C++/Kotlin, audit de failles et refactoring.", "Inclus Developer", "Code", isAcquired = true),
            SpecializedAgentItem("AGT-CYB", "Cybersecurity Agent", "Sécurité Red Team", "Audit offensif/défensif contrôlé, analyse de failles et rapport SHA-256.", "Inclus Security", "Security", isAcquired = true),
            SpecializedAgentItem("AGT-BUS", "Business Agent", "Business & Ventes", "Analyse commerciale, étude concurrentielle et rédaction de propositions.", "Inclus Business", "BusinessCenter", isAcquired = false),
            SpecializedAgentItem("AGT-DOC", "Document Agent", "Gestion PDF/OCR", "Extraction structurée, classification sémantique et génération de résumés.", "Gratuit", "Description", isAcquired = true, isFreeTier = true),
            SpecializedAgentItem("AGT-PRD", "Productivity Agent", "Organisation", "Gestion avancée du calendrier, des tâches et automatisation de la routine.", "Gratuit", "TaskAlt", isAcquired = true, isFreeTier = true),
            SpecializedAgentItem("AGT-TRN", "Translator Agent", "Traduction Tech", "Traduction technique multilingue contextuelle de documents et rapports.", "Inclus Premium", "Translate", isAcquired = true)
        )

        log("NEXUS AGENT STORE: Catalogue d'agents spécialisés initialisé avec succès.")
    }

    fun selectTier(tier: NexusTier) {
        _accountState.value = _accountState.value.copy(currentTier = tier)
        log("OFFRE ABONNEMENT: Passage au plan '${tier.title}' (${tier.monthlyPriceLabel}).")
    }

    fun acquireAgent(agentId: String): Boolean {
        val list = _storeAgents.value.toMutableList()
        val index = list.indexOfFirst { it.agentId == agentId }
        if (index != -1) {
            val agent = list[index]
            list[index] = agent.copy(isAcquired = true)
            _storeAgents.value = list
            _accountState.value = _accountState.value.copy(
                acquiredAgentsCount = list.count { it.isAcquired }
            )
            log("NEXUS AGENT STORE: Agent '${agent.name}' débloqué et activé dans le centre d'orchestration.")
            return true
        }
        return false
    }

    fun isFeatureUnlocked(featureKey: String): Boolean {
        val currentTier = _accountState.value.currentTier
        // Non-negotiable free features always accessible
        val alwaysFreeFeatures = listOf(
            "CORE_SECURITY_DIAG", "PERSONAL_DATA_VIEW", "BASIC_FILE_ACCESS",
            "USER_DATA_WIPE", "PERMISSION_CONTROL", "ACTION_TRANSPARENCY",
            "PROOFS_VERIFICATION", "AUTONOMY_KILL_SWITCH"
        )
        if (alwaysFreeFeatures.contains(featureKey)) return true

        return when (currentTier) {
            NexusTier.FREE -> false
            NexusTier.PLUS -> listOf("AUTONOMOUS_AGENT", "SMART_CLOUD_STORAGE", "SMART_FILE_MANAGER").contains(featureKey)
            NexusTier.PREMIUM -> true
            NexusTier.ULTIMATE -> true
            NexusTier.DEVELOPER -> true
            NexusTier.BUSINESS -> true
        }
    }

    private fun log(msg: String) {
        _monetizationLogs.value = _monetizationLogs.value + "[${System.currentTimeMillis() % 100000}] $msg"
    }
}
