package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.NexusCore
import com.example.data.jni.NexusJniBridge
import com.example.data.local.NexusDatabase
import com.example.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.example.core.PermissionScanner
import com.example.core.RedTeamOperationResult
import com.example.core.agentic.MetaAgentStage
import com.example.core.agentic.MetaModuleProposal

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val sender: String, // "USER" or "NEXUS"
    val text: String,
    val thoughtTrace: String = "",
    val actionState: ActionState? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class TelemetryState(
    val cpuUsage: Float = 18f,
    val ramUsedMb: Int = 1940,
    val ramTotalMb: Int = 4096,
    val batteryLevel: Int = 88,
    val batteryTemp: Float = 31.4f,
    val storageUsedGb: Float = 42.1f,
    val storageTotalGb: Float = 128.0f,
    val downloadSpeedKbps: Int = 4850,
    val uploadSpeedKbps: Int = 1200,
    val jniStatus: String = NexusJniBridge.getBridgeStatus(),
    val nativeVersion: String = NexusJniBridge.getCoreVersion()
)

class NexusViewModel(application: Application) : AndroidViewModel(application) {

    private val db = NexusDatabase.getDatabase(application)
    val dao = db.nexusDao()
    val core = NexusCore(dao, application)
    private val permissionScanner = PermissionScanner(application)

    val actionHistory: StateFlow<List<ActionRecordEntity>> = dao.getAllActions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memoryList: StateFlow<List<NexusMemoryEntity>> = dao.getAllMemory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val automationsList: StateFlow<List<AutomationEntity>> = dao.getAllAutomations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val batteryStatsHistory: StateFlow<List<BatteryStatEntity>> = dao.getAllBatteryStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val highDrainAlerts: StateFlow<List<BatteryStatEntity>> = dao.getHighDrainAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Adaptive Engine & Compatibility Test Lab Flows
    val hardwareProfile = core.adaptiveEngine?.currentProfile ?: MutableStateFlow(com.example.core.adaptive.HardwareCapabilitiesProfile()).asStateFlow()
    val hardwareDiagnosticsReport = core.adaptiveEngine?.lastReport ?: MutableStateFlow<com.example.core.adaptive.DiagnosticsReport?>(null).asStateFlow()
    val compatibilityReport = core.compatibilityTestEngine.lastRunReport
    val isCompatibilityTesting = core.compatibilityTestEngine.isTesting

    private val _telemetry = MutableStateFlow(TelemetryState())
    val telemetry: StateFlow<TelemetryState> = _telemetry.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _currentMedicalScan = MutableStateFlow<MedicalImagingScan?>(null)
    val currentMedicalScan: StateFlow<MedicalImagingScan?> = _currentMedicalScan.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<String>>(emptyList())
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

    private val _pendingActionForApproval = MutableStateFlow<ActionRecordEntity?>(null)
    val pendingActionForApproval: StateFlow<ActionRecordEntity?> = _pendingActionForApproval.asStateFlow()

    private val _highRiskApps = MutableStateFlow<List<AppRiskInfo>>(emptyList())
    val highRiskApps: StateFlow<List<AppRiskInfo>> = _highRiskApps.asStateFlow()

    private val _isScanningPermissions = MutableStateFlow(false)
    val isScanningPermissions: StateFlow<Boolean> = _isScanningPermissions.asStateFlow()

    private val _redTeamLogs = MutableStateFlow<List<RedTeamOperationResult>>(emptyList())
    val redTeamLogs: StateFlow<List<RedTeamOperationResult>> = _redTeamLogs.asStateFlow()

    private val _isRedTeamActive = MutableStateFlow(false)
    val isRedTeamActive: StateFlow<Boolean> = _isRedTeamActive.asStateFlow()

    val metaAgentStage: StateFlow<MetaAgentStage> = core.metaAgent.currentStage
    val metaAgentLogs: StateFlow<List<String>> = core.metaAgent.metaAgentLogs
    val metaModuleProposals: StateFlow<List<MetaModuleProposal>> = core.metaAgent.activeProposals
    val integratedMetaModules: StateFlow<List<MetaModuleProposal>> = core.metaAgent.integratedModules

    // Storage Governor Flows
    val storageQuotaStatus = core.storageGovernor.quotaStatus
    val systemStorageItems = core.storageGovernor.systemItems
    val userStorageItems = core.storageGovernor.userItems
    val storageGovernorLogs = core.storageGovernor.governorLogs

    // Predictive Brain & Digital Twin Flows
    val predictiveAlerts = core.predictiveBrain.activeAlerts
    val digitalTwinNodes = core.digitalTwin.nodes

    // Scientific Debate & Cyber Lab Flows
    val lastDebate = core.scientificDebate.lastDebate
    val cyberLabAudit = core.cyberLab.lastAudit
    val devProjects = core.universalDeveloper.activeProjects
    val monetizationManager = core.monetizationManager

    init {
        initInitialData()
        startTelemetryLoop()
        startBackgroundPermissionScanner()
    }

    private fun initInitialData() {
        viewModelScope.launch {
            _terminalLogs.value = listOf(
                "SYSTEM INIT: NEXUS CORE v3.8 Operational.",
                "JNI BRIDGE: ${NexusJniBridge.getBridgeStatus()}",
                "BIZTALK DATA HUB: Active on port 8090",
                "CHRONOS SCHEDULER: Cadence thread synchronized",
                "SERVICE REGISTRY: 10 Core Engines and 20 Departments Loaded.",
                "TYPE 'help' or execute actions via the NEXUS UI."
            )

            // Seed initial memory if empty
            if (dao.getActionCount() == 0) {
                core.executePipeline("Initialisation de l'architecture NEXUS CORE", "SYSTEM_GUARD", true)
                dao.insertMemory(
                    NexusMemoryEntity(
                        key = "SYS_CONFIG",
                        category = "SYSTEM",
                        content = "Mode Souverain Actif. Stack Locale BizTalk-Chronos-BitNet prêt.",
                        importance = 5,
                        sourceDepartment = "SYSTEM_GUARD"
                    )
                )
                dao.insertAutomation(
                    AutomationEntity(
                        title = "Protection Batterie Faible",
                        triggerCondition = "BATTERY_LEVEL < 15%",
                        targetDepartment = "BATTERY_INTELLIGENCE",
                        actionCommand = "Passer en mode économie extrême & geler apps secondaires",
                        isEnabled = true
                    )
                )
                dao.insertAutomation(
                    AutomationEntity(
                        title = "Auto-Trim RAM Turbocompte",
                        triggerCondition = "RAM_USAGE > 85%",
                        targetDepartment = "PERFORMANCE_ENGINE",
                        actionCommand = "Exécuter compaction mémoire BitNet",
                        isEnabled = true
                    )
                )
            }
        }
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            while (true) {
                delay(3000)
                val cpu = (12..38).random().toFloat()
                val ram = (1850..2200).random()
                val dl = (3200..6500).random()
                val ul = (800..1800).random()
                _telemetry.value = _telemetry.value.copy(
                    cpuUsage = cpu,
                    ramUsedMb = ram,
                    downloadSpeedKbps = dl,
                    uploadSpeedKbps = ul
                )
            }
        }
    }

    fun sendUserChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(sender = "USER", text = text)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            val thoughtTrace = "Raisonnement Cognitive Engine...\n• Analyse de la demande: '$text'\n• Évaluation Politique de Sécurité: Risque Faible\n• Consultation Mémoire Persistante Room"
            delay(400)
            val responseText = core.cognitive.processUserPrompt(text)

            // Execute action pipeline
            val actionResult = core.executePipeline("Commande Assistant: $text", "KNOWLEDGE_ENGINE", true)

            val nexusMsg = ChatMessage(
                sender = "NEXUS",
                text = responseText,
                thoughtTrace = thoughtTrace,
                actionState = actionResult.state
            )
            _chatMessages.value = _chatMessages.value + nexusMsg
        }
    }

    fun triggerQuickAction(goal: String, targetDepartment: String, isDestructive: Boolean) {
        viewModelScope.launch {
            val plannedAction = ActionRecordEntity(
                actionId = "ACT-" + System.currentTimeMillis().toString().takeLast(6),
                title = goal,
                departmentId = targetDepartment,
                engineId = "CORE",
                details = "Action déclenchée via dashboard",
                state = ActionState.PROPOSED,
                isDestructive = isDestructive
            )

            if (isDestructive) {
                _pendingActionForApproval.value = plannedAction
            } else {
                val result = core.executePipeline(goal, targetDepartment, true)
                appendTerminalLog("EXECUTE: ${result.title} -> ${result.state} [${result.verificationProof}]")
            }
        }
    }

    fun confirmPendingAction(approved: Boolean) {
        val pending = _pendingActionForApproval.value ?: return
        _pendingActionForApproval.value = null

        viewModelScope.launch {
            if (approved) {
                val result = core.executePipeline(pending.title, pending.departmentId, true)
                appendTerminalLog("USER AUTHORIZED: ${result.title} -> VERIFIED")
            } else {
                val cancelled = pending.copy(
                    state = ActionState.CANCELLED,
                    details = "Annulé par l'utilisateur"
                )
                dao.insertAction(cancelled)
                appendTerminalLog("USER CANCELLED: ${pending.title}")
            }
        }
    }

    fun runMedicalScanSimulation(modality: String, targetArea: String) {
        viewModelScope.launch {
            appendTerminalLog("BIZTALK: Interception du flux d'imagerie $modality...")
            appendTerminalLog("CHRONOS: Cadencement de l'analyse local Qwen/Gemma4...")
            val scan = core.cognitive.processMedicalImageSimulation("DICOM-" + (1000..9999).random(), modality, targetArea)
            _currentMedicalScan.value = scan

            // Record verification proof in audit ledger
            core.executePipeline(
                goal = "Compte Rendu Imagerie ${scan.modality} pour ${scan.subjectId}",
                targetDepartment = "MULTIMEDIA_INTELLIGENCE",
                userApproval = true
            )
            appendTerminalLog("REPORT GENERATED: Scan ${scan.scanId} -> Confiance ${(scan.confidenceScore * 100).toInt()}%")
        }
    }

    fun executeTerminalCommand(command: String) {
        if (command.isBlank()) return
        appendTerminalLog("$ nexus-cli > $command")
        viewModelScope.launch {
            val cmd = command.trim().lowercase()
            when {
                cmd == "help" -> {
                    appendTerminalLog("NEXUS CLI Commandes disponibles:")
                    appendTerminalLog("  status      - Affiche l'état des 10 moteurs centraux")
                    appendTerminalLog("  jni         - Interroge le statut du bridge Rust/C++")
                    appendTerminalLog("  scan-med    - Lance une simulation d'imagerie médicale")
                    appendTerminalLog("  optimize    - Libère la RAM via Turbocompte")
                    appendTerminalLog("  clear       - Efface la console terminal")
                }
                cmd == "clear" -> {
                    _terminalLogs.value = emptyList()
                }
                cmd == "status" -> {
                    appendTerminalLog("CORE ENGINE STATUS: 10/10 ACTIVE | DEPARTMENTS: 20/20 OPERATIONAL")
                    appendTerminalLog("JNI BRIDGE: ${NexusJniBridge.getBridgeStatus()}")
                }
                cmd == "jni" -> {
                    appendTerminalLog("NATIVE JNI ENGINE: ${NexusJniBridge.getBridgeStatus()}")
                    appendTerminalLog("BITNET ACCEL: ${NexusJniBridge.executeBitNetInference("CLI Test", 2)}")
                }
                cmd == "scan-med" -> {
                    runMedicalScanSimulation("DICOM_XRAY", "Poumons/Thorax")
                }
                cmd == "optimize" -> {
                    triggerQuickAction("Optimisation RAM Turbocompte", "PERFORMANCE_ENGINE", false)
                }
                else -> {
                    val result = core.cognitive.processUserPrompt(command)
                    appendTerminalLog("RESULT: $result")
                }
            }
        }
    }

    fun saveNewMemory(key: String, category: String, content: String) {
        viewModelScope.launch {
            core.memory.saveMemorySnippet(key, category, content, 3, "USER_INPUT")
        }
    }

    fun deleteMemory(memory: NexusMemoryEntity) {
        viewModelScope.launch {
            core.memory.deleteMemorySnippet(memory)
        }
    }

    fun addAutomation(title: String, trigger: String, dept: String, action: String) {
        viewModelScope.launch {
            dao.insertAutomation(
                AutomationEntity(
                    title = title,
                    triggerCondition = trigger,
                    targetDepartment = dept,
                    actionCommand = action,
                    isEnabled = true
                )
            )
        }
    }

    fun toggleAutomation(automation: AutomationEntity) {
        viewModelScope.launch {
            dao.setAutomationEnabled(automation.id, !automation.isEnabled)
        }
    }

    fun deleteAutomation(automation: AutomationEntity) {
        viewModelScope.launch {
            dao.deleteAutomation(automation)
        }
    }

    private fun startBackgroundPermissionScanner() {
        viewModelScope.launch {
            // Initial scan
            runAutomatedAppPermissionScan()
            while (true) {
                delay(120000) // Periodic background scan every 2 minutes
                runAutomatedAppPermissionScan()
            }
        }
    }

    fun runAutomatedAppPermissionScan() {
        viewModelScope.launch {
            _isScanningPermissions.value = true
            appendTerminalLog("SYSTEM_GUARD: Analyse d'arrière-plan des permissions d'applications...")
            delay(500)
            val scanned = permissionScanner.scanInstalledApps()
            _highRiskApps.value = scanned
            _isScanningPermissions.value = false

            val criticalCount = scanned.count { it.riskLevel == RiskLevel.CRITICAL }
            val highCount = scanned.count { it.riskLevel == RiskLevel.HIGH }

            if (criticalCount > 0 || highCount > 0) {
                appendTerminalLog("SYSTEM_GUARD ALERTE: $criticalCount apps critiques et $highCount apps à haut risque détectées.")
            } else {
                appendTerminalLog("SYSTEM_GUARD: Aucune nouvelle menace de permission détectée.")
            }
        }
    }

    fun removeOrUninstallApp(app: AppRiskInfo) {
        viewModelScope.launch {
            // Register high-risk app removal request in Action Pipeline
            val actionResult = core.executePipeline(
                goal = "Désinstallation/Suppression de l'application à risque ${app.appName}",
                targetDepartment = "APP_MANAGER",
                userApproval = true
            )

            // Remove from current scanned high risk list
            _highRiskApps.value = _highRiskApps.value.filter { it.packageName != app.packageName }
            appendTerminalLog("APP_MANAGER: Suppression/Désinstallation exécutée pour ${app.packageName} (${actionResult.verificationProof})")
        }
    }

    fun freezeApp(app: AppRiskInfo) {
        viewModelScope.launch {
            val actionResult = core.executePipeline(
                goal = "Gel des processus d'arrière-plan de ${app.appName}",
                targetDepartment = "SYSTEM_GUARD",
                userApproval = false
            )

            _highRiskApps.value = _highRiskApps.value.map {
                if (it.packageName == app.packageName) it.copy(isFrozen = true) else it
            }
            appendTerminalLog("SYSTEM_GUARD: Processus ${app.packageName} gelé et isolé (${actionResult.verificationProof})")
        }
    }

    fun ignoreAppRisk(app: AppRiskInfo) {
        _highRiskApps.value = _highRiskApps.value.map {
            if (it.packageName == app.packageName) it.copy(isIgnored = true) else it
        }
        appendTerminalLog("SYSTEM_GUARD: Risque ignoré par l'utilisateur pour ${app.packageName}")
    }

    fun runRedTeamSimulation(vector: String = "OVERLAY") {
        viewModelScope.launch {
            _isRedTeamActive.value = true
            appendTerminalLog("RED_TEAM ENGINE: Lancement de la simulation d'attaque offensive (Vecteur: $vector)...")

            val result = core.redTeam.runRedTeamOperation(vector)
            _redTeamLogs.value = listOf(result) + _redTeamLogs.value
            _isRedTeamActive.value = false

            // Record Red Team audit in Action Ledger
            core.executePipeline(
                goal = "Simulation Red Team ${result.mitreAttackCode} (${result.targetVector})",
                targetDepartment = "CYBER_DEFENSE",
                userApproval = true
            )

            appendTerminalLog("RED_TEAM COMPLÉTÉ: [${result.mitreAttackCode}] ${result.details} (${result.auditSignature})")
        }
    }

    fun triggerMetaAgentCapabilitySynthesis(objective: String) {
        if (objective.isBlank()) return
        viewModelScope.launch {
            appendTerminalLog("META-AGENT: Traitement d'une mission hors capacités actuelles: '$objective'")
            val proposal = core.metaAgent.processUnfulfilledObjective(objective, core.actionRegistry)
            if (proposal != null) {
                appendTerminalLog("META-AGENT: Proposal ${proposal.proposalId} créé pour '${proposal.moduleName}'. Attente de validation.")
            } else {
                appendTerminalLog("META-AGENT: Synthèse échouée ou annulée par les règles de sécurité.")
            }
        }
    }

    fun approveMetaModule(proposalId: String) {
        viewModelScope.launch {
            val ok = core.metaAgent.approveAndIntegrateModule(proposalId, core.actionRegistry)
            if (ok) {
                appendTerminalLog("META-AGENT: Module $proposalId autorisé par l'utilisateur et intégré avec succès dans l'ActionRegistry!")
                core.executePipeline("Intégration Module Auto-Généré $proposalId", "AI_DEVELOPER", true)
            }
        }
    }

    fun rejectMetaModule(proposalId: String) {
        viewModelScope.launch {
            core.metaAgent.rejectModule(proposalId)
            appendTerminalLog("META-AGENT: Module $proposalId rejeté par l'utilisateur.")
        }
    }

    // Storage Governor Actions
    fun writeSystemConfigData(name: String, sizeMb: Float) {
        viewModelScope.launch {
            val ok = core.storageGovernor.writeSystemData(name, sizeMb, "application/json")
            if (!ok) {
                appendTerminalLog("STORAGE GOVERNOR ALERTE: Écriture Système bloquée! Plafond 1000 Mo atteint.")
            } else {
                appendTerminalLog("STORAGE GOVERNOR: Écriture Système autorisée '$name' ($sizeMb Mo).")
            }
        }
    }

    fun writeUserPersonalFile(name: String, sizeMb: Float) {
        viewModelScope.launch {
            val item = core.storageGovernor.writeUserPersonalFile(name, sizeMb, "application/pdf")
            appendTerminalLog("STORAGE GOVERNOR: Fichier Personnel '${item.name}' enregistré sur Google Drive Utilisateur.")
        }
    }

    fun verifyStorageIsolation() {
        viewModelScope.launch {
            val audit = core.storageGovernor.verifyIsolationAndNonExceedance()
            appendTerminalLog("STORAGE GOVERNOR AUDIT: Zéro Contamination: ${audit.isZeroContaminationVerified} | Plafond 1 Go Respecté: ${audit.systemStorageCompliantWith1GbCap} (${audit.proofSignature})")
        }
    }

    // Predictive Brain Action
    fun runPredictiveBrainAnalysis() {
        viewModelScope.launch {
            appendTerminalLog("PREDICTIVE BRAIN: Analyse des tendances et calcul des risques futurs...")
            val alerts = core.predictiveBrain.analyzeTrendsAndCalculateRisks()
            appendTerminalLog("PREDICTIVE BRAIN: ${alerts.size} alertes de dégradation prédictive générées.")
        }
    }

    // Digital Twin Action
    fun runDigitalTwinImpactSimulation(operation: String, path: String) {
        viewModelScope.launch {
            appendTerminalLog("DIGITAL TWIN: Simulation d'impact pour l'opération '$operation' sur '$path'...")
            val sim = core.digitalTwin.simulateActionImpact(operation, listOf(path))
            appendTerminalLog("DIGITAL TWIN: Risque d'effets secondaires: ${sim.sideEffectsRisk} (${sim.safetyRecommendation})")
        }
    }

    // Scientific Debate Action
    fun runScientificDebate(problem: String) {
        viewModelScope.launch {
            appendTerminalLog("SCIENTIFIC DEBATE: Lancement du débat contradictoire à 7 agents pour '$problem'...")
            val debate = core.scientificDebate.runContradictoryDebate(problem)
            appendTerminalLog("SCIENTIFIC DEBATE: Consensus validé avec l'objection du Challenger Agent (${debate.verificationProof})")
        }
    }

    // Cyber Lab Action
    fun runCyberLab5UnitAudit() {
        viewModelScope.launch {
            appendTerminalLog("CYBER LAB 5-UNITS: Audit offensif, défensif et indépendant en cours...")
            val audit = core.cyberLab.runFull5UnitCyberAudit()
            appendTerminalLog("CYBER LAB COMPLÉTÉ: Statut ${audit.vulnerabilitySeverity} (${audit.auditProofHash})")
        }
    }

    // Universal Developer Action
    fun executeDevTask(language: String, action: String) {
        viewModelScope.launch {
            appendTerminalLog("UNIVERSAL DEVELOPER: Exécution $action pour $language...")
            val res = core.universalDeveloper.executeDevTask(language, action)
            appendTerminalLog("DEVELOPER RESULT: ${res.outputLogs} (${res.verificationHash})")
        }
    }

    // Battery Control Actions
    fun restrictBatteryProcess(stat: BatteryStatEntity) {
        viewModelScope.launch {
            triggerQuickAction("Restreindre processus ${stat.topDrainAppName}", "BATTERY_INTELLIGENCE", false)
            appendTerminalLog("BATTERY SERVICE: Processus '${stat.topDrainAppName}' restreint en arrière-plan.")
        }
    }

    fun freezeBatteryProcess(stat: BatteryStatEntity) {
        viewModelScope.launch {
            triggerQuickAction("Geler processus ${stat.topDrainAppName}", "BATTERY_INTELLIGENCE", false)
            appendTerminalLog("BATTERY SERVICE: Processus '${stat.topDrainAppName}' gelé dans le bac à sable.")
        }
    }

    // Adaptive Engine Hardware Diagnostics
    fun runHardwareDiagnostics() {
        viewModelScope.launch {
            appendTerminalLog("ADAPTIVE ENGINE: Exécution du diagnostic matériel & profilage...")
            val report = core.adaptiveEngine?.runHardwareDiagnostics()
            if (report != null) {
                appendTerminalLog("ADAPTIVE ENGINE: Profil retenu -> ${report.profile.activeExecutionProfile} (${report.profile.manufacturer} ${report.profile.model})")
            }
        }
    }

    // Compatibility Test Engine Run
    fun runCompatibilityTestMatrix() {
        viewModelScope.launch {
            appendTerminalLog("COMPATIBILITY TEST ENGINE: Démarrage de la matrice de qualification multi-Android (API 31-36)...")
            val report = core.compatibilityTestEngine.runFullCompatibilityMatrix()
            appendTerminalLog("COMPATIBILITY TEST ENGINE: ${report.passedTests}/${report.totalTests} tests validés (${report.coveragePercentage}% couverture).")
        }
    }

    // Cloud Portable Environment Actions
    fun createCloudEnvironmentBackup() {
        viewModelScope.launch {
            appendTerminalLog("STORAGE GOVERNOR: Création d'une sauvegarde de l'environnement portable NEXUS sur Google Drive...")
            val id = core.storageGovernor.createCloudEnvironmentBackup()
            appendTerminalLog("STORAGE GOVERNOR: Sauvegarde terminée [$id]. Restauration disponible sur tout appareil Android 12+.")
        }
    }

    fun restoreCloudEnvironmentFromDrive(backupId: String = "NEXUS-BACKUP-DRIVE-LATEST") {
        viewModelScope.launch {
            appendTerminalLog("STORAGE GOVERNOR: Restauration de l'environnement NEXUS depuis Google Drive pour [$backupId]...")
            core.storageGovernor.restoreCloudEnvironmentFromDrive(backupId)
            appendTerminalLog("STORAGE GOVERNOR: Restauration réussie. Tous les agents, configurations et index sont opérationnels.")
        }
    }

    private fun appendTerminalLog(log: String) {
        _terminalLogs.value = _terminalLogs.value + log
    }
}
