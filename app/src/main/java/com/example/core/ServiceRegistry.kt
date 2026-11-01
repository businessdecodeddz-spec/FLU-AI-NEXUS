package com.example.core

import com.example.data.model.DepartmentInfo
import com.example.data.model.EngineInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 10. NEXUS SERVICE REGISTRY
 * Central registry tracking the 10 Core Engines and 20 Specialized Departments.
 */
class ServiceRegistry {

    private val _engines = MutableStateFlow<List<EngineInfo>>(emptyList())
    val engines: StateFlow<List<EngineInfo>> = _engines.asStateFlow()

    private val _departments = MutableStateFlow<List<DepartmentInfo>>(emptyList())
    val departments: StateFlow<List<DepartmentInfo>> = _departments.asStateFlow()

    init {
        initializeEngines()
        initializeDepartments()
    }

    private fun initializeEngines() {
        _engines.value = listOf(
            EngineInfo("CORE", "NEXUS CORE", "Orchestrateur Principal & Bus Système", "ACTIVE", 4),
            EngineInfo("COGNITIVE", "COGNITIVE ENGINE", "Raisonnement, Gemini & LLMs Locaux", "ACTIVE", 12),
            EngineInfo("MEMORY", "NEXUS MEMORY", "Mémoire Persistante, Contextuelle & Vectorielle", "ACTIVE", 8),
            EngineInfo("PLANNER", "NEXUS PLANNER", "Planification Séquentielle des Opérations", "ACTIVE", 15),
            EngineInfo("DECISION", "DECISION ENGINE", "Matrice de Décision & Évaluation des Risques", "ACTIVE", 6),
            EngineInfo("POLICY", "POLICY ENGINE", "Contrôle des Autorisations & Sécurité", "ACTIVE", 2),
            EngineInfo("EXECUTION", "EXECUTION ENGINE", "Exécution Native, Android & JNI", "ACTIVE", 18),
            EngineInfo("VERIFICATION", "VERIFICATION ENGINE", "Validation des Preuves & Résultats", "ACTIVE", 10),
            EngineInfo("LEARNING", "LEARNING ENGINE", "Optimisation des Stratégies & Télémétrie", "ACTIVE", 14),
            EngineInfo("REGISTRY", "SERVICE REGISTRY", "Registre des 20 Départements", "ACTIVE", 1)
        )
    }

    private fun initializeDepartments() {
        _departments.value = listOf(
            DepartmentInfo(
                id = "SYSTEM_GUARD",
                name = "System Guard",
                description = "Protection temps réel de l'intégrité système & root check",
                iconName = "shield",
                isOperational = true,
                loadPercentage = 12f,
                activeTasks = 2,
                capabilities = listOf("Intégrité SEAndroid", "Détection Anomalies Process", "Garde-fou Système")
            ),
            DepartmentInfo(
                id = "PERFORMANCE_ENGINE",
                name = "Performance Engine",
                description = "Optimisation du CPU, de la RAM et de la réactivité",
                iconName = "speed",
                isOperational = true,
                loadPercentage = 24f,
                activeTasks = 4,
                capabilities = listOf("Turbocompte Acceleration", "RAM Auto-Trim", "Gouverneur CPU")
            ),
            DepartmentInfo(
                id = "STORAGE_INTELLIGENCE",
                name = "Storage Intelligence",
                description = "Analyse intelligente du stockage, doublons et fichiers temporaires",
                iconName = "storage",
                isOperational = true,
                loadPercentage = 8f,
                activeTasks = 1,
                capabilities = listOf("Nettoyage Smart Cache", "Détection Fichiers Volumineux", "Indexation")
            ),
            DepartmentInfo(
                id = "CYBER_DEFENSE",
                name = "Cyber Defense",
                description = "Analyse comportementale des menaces et blocage des connexions suspectes",
                iconName = "security",
                isOperational = true,
                loadPercentage = 18f,
                activeTasks = 3,
                capabilities = listOf("Pare-feu Applicatif", "Détection Malware", "Audit de Signature")
            ),
            DepartmentInfo(
                id = "NETWORK_COMMAND",
                name = "Network Command",
                description = "Gestion prioritaire du Wi-Fi, 5G/4G et sockets réseau",
                iconName = "wifi",
                isOperational = true,
                loadPercentage = 15f,
                activeTasks = 2,
                capabilities = listOf("Test de Débit Live", "Analyse de Sockets", "Routage BizTalk")
            ),
            DepartmentInfo(
                id = "BATTERY_INTELLIGENCE",
                name = "Battery Intelligence",
                description = "Gestion prédictive de l'énergie et santé de la batterie",
                iconName = "battery",
                isOperational = true,
                loadPercentage = 6f,
                activeTasks = 1,
                capabilities = listOf("Profils Économie", "Courbe Thermique", "Comportement Énergie")
            ),
            DepartmentInfo(
                id = "APP_MANAGER",
                name = "App Manager",
                description = "Contrôle des applications installées et gestion des permissions",
                iconName = "apps",
                isOperational = true,
                loadPercentage = 10f,
                activeTasks = 1,
                capabilities = listOf("Gel d'Applications", "Audit des Permissions", "Extraction APK")
            ),
            DepartmentInfo(
                id = "FILE_COMMANDER",
                name = "File Commander",
                description = "Exploration, organisation et chiffrement sécurisé des fichiers",
                iconName = "folder",
                isOperational = true,
                loadPercentage = 5f,
                activeTasks = 0,
                capabilities = listOf("Chiffrement Local", "Gestion de Fichiers", "Compression")
            ),
            DepartmentInfo(
                id = "AI_DEVELOPER",
                name = "AI Developer",
                description = "Génération de scripts, intégration JNI et outils de développement",
                iconName = "code",
                isOperational = true,
                loadPercentage = 20f,
                activeTasks = 2,
                capabilities = listOf("Compilateur JNI Bridge", "Terminal Shell CLI", "Générateur de Code")
            ),
            DepartmentInfo(
                id = "TERMINAL_ENGINE",
                name = "Terminal Engine",
                description = "Console de commandes autonome et interpréteur de scripts NEXUS",
                iconName = "terminal",
                isOperational = true,
                loadPercentage = 14f,
                activeTasks = 1,
                capabilities = listOf("Interpréteur Shell nexus-cli", "Logs Système", "Diagnostic Bas Niveau")
            ),
            DepartmentInfo(
                id = "KNOWLEDGE_ENGINE",
                name = "Knowledge Engine",
                description = "Extraction et structuration des connaissances de l'utilisateur",
                iconName = "psychology",
                isOperational = true,
                loadPercentage = 11f,
                activeTasks = 2,
                capabilities = listOf("Graphe de Connaissances", "Base SQLite Vectorielle", "Recherche Contextuelle")
            ),
            DepartmentInfo(
                id = "PERSONAL_SECRETARY",
                name = "Personal Secretary",
                description = "Gestion des tâches, rappels intelligentes et organisation du temps",
                iconName = "event",
                isOperational = true,
                loadPercentage = 9f,
                activeTasks = 1,
                capabilities = listOf("Rappels Contextuels", "Ordonnancement Chronos", "Planificateur Proactif")
            ),
            DepartmentInfo(
                id = "COMMUNICATION_CENTER",
                name = "Communication Center",
                description = "Synthèse et filtrage des notifications et messages importants",
                iconName = "message",
                isOperational = true,
                loadPercentage = 13f,
                activeTasks = 2,
                capabilities = listOf("Filtrage Anti-Spam", "Synthèse de Notifications", "Canal Urgence")
            ),
            DepartmentInfo(
                id = "MULTIMEDIA_INTELLIGENCE",
                name = "Multimedia Intelligence",
                description = "Analyse d'images médicales/industrielles, vision IA et traitement média",
                iconName = "image",
                isOperational = true,
                loadPercentage = 30f,
                activeTasks = 3,
                capabilities = listOf("Imagerie Médicale DICOM/XRay", "Pipeline BizTalk-Chronos", "Gemma-4/Qwen BitNet")
            ),
            DepartmentInfo(
                id = "FINANCIAL_ASSISTANT",
                name = "Financial Assistant",
                description = "Suivi autonome des dépenses locales et alertes budgétaires",
                iconName = "attach_money",
                isOperational = true,
                loadPercentage = 4f,
                activeTasks = 0,
                capabilities = listOf("Analyse Budgétaire", "Rapports Financiers", "Confidentialité Totale")
            ),
            DepartmentInfo(
                id = "AUTOMATION_ENGINE",
                name = "Automation Engine",
                description = "Moteur de règles 'Si Déclencheur -> Alors Action' entièrement personnalisable",
                iconName = "auto_mode",
                isOperational = true,
                loadPercentage = 16f,
                activeTasks = 4,
                capabilities = listOf("Règles Conditionnelles", "Déclencheurs Énergie/Réseau", "Actions Automatiques")
            ),
            DepartmentInfo(
                id = "PRIVACY_GUARD",
                name = "Privacy Guard",
                description = "Audit strict des trackers, des micros et caméras en arrière-plan",
                iconName = "lock",
                isOperational = true,
                loadPercentage = 7f,
                activeTasks = 1,
                capabilities = listOf("Détection Télémétrie Indésirable", "Killswitch Capteurs", "Audit Données")
            ),
            DepartmentInfo(
                id = "SELF_DIAGNOSTIC",
                name = "Self Diagnostic",
                description = "Autodiagnostic continu des 10 moteurs et détection d'erreurs",
                iconName = "health_and_safety",
                isOperational = true,
                loadPercentage = 5f,
                activeTasks = 1,
                capabilities = listOf("Inspection Santé Moteurs", "Auto-Réparation", "Audit Journalier")
            ),
            DepartmentInfo(
                id = "EVOLUTION_ENGINE",
                name = "Evolution Engine",
                description = "Apprentissage continu et adaptation des modèles selon l'usage",
                iconName = "trending_up",
                isOperational = true,
                loadPercentage = 12f,
                activeTasks = 1,
                capabilities = listOf("Ajustement des Poids", "Optimisation BitNet", "Métriques de Performance")
            ),
            DepartmentInfo(
                id = "RESEARCH_INTELLIGENCE",
                name = "Research Intelligence",
                description = "Synthèse scientifique, recherche de données cliniques & rédaction de rapports",
                iconName = "science",
                isOperational = true,
                loadPercentage = 22f,
                activeTasks = 2,
                capabilities = listOf("Rédaction Rapports Médicaux", "Recherche Bibliographique", "Extraction Formelle")
            )
        )
    }

    fun getDepartment(id: String): DepartmentInfo? {
        return _departments.value.find { it.id.equals(id, ignoreCase = true) }
    }
}
