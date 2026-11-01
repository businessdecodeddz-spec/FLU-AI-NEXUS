package com.example.core

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.StatFs
import com.example.data.model.ActionResult
import com.example.data.model.ActionTool
import com.example.data.model.OperationClassification
import com.example.data.model.RiskLevel
import java.io.File
import java.net.NetworkInterface
import java.security.MessageDigest
import java.util.Collections

/**
 * NEXUS ACTION REGISTRY
 * Centralized registry of operational tools with real Android OS & filesystem execution.
 * Enforces the rule: No simulated operation may be presented as a real operation,
 * and every execution produces an observable proof (real file hash, real OS telemetry, or real package list hash).
 */
class ActionRegistry(private val context: Context? = null) {

    private val tools = mutableMapOf<String, ActionTool>()

    private val workspaceRoot: File by lazy {
        val base = context?.filesDir ?: File(System.getProperty("java.io.tmpdir", "."), "nexus_operational_workspace")
        if (!base.exists()) {
            base.mkdirs()
        }
        base
    }

    init {
        registerDefaultTools()
    }

    private fun computeSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }.take(16).uppercase()
    }

    private fun computeFileSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(8192)
            var read = input.read(buffer)
            while (read != -1) {
                digest.update(buffer, 0, read)
                read = input.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }.take(16).uppercase()
    }

    private fun ensureWorkspaceInitialized() {
        val docsDir = File(workspaceRoot, "documents")
        if (!docsDir.exists()) {
            docsDir.mkdirs()
            File(docsDir, "nexus_system_manifest.json").writeText(
                """{"engine":"FLU-AI NEXUS","version":"3.8","createdAt":${System.currentTimeMillis()}}"""
            )
            File(docsDir, "audit_policy_rules.txt").writeText(
                "RULE-01: Zero simulated operation presented as real.\nRULE-02: Observable SHA-256 proof mandatory."
            )
        }
    }

    private fun registerDefaultTools() {
        // 1. Inventaire des applications installées (Réel via PackageManager)
        registerTool(
            ActionTool(
                id = "app_inventory",
                name = "Inventaire des Applications",
                description = "Recense les applications réellement installées via Android PackageManager.",
                requiredPermissions = emptyList(),
                riskLevel = RiskLevel.LOW,
                isDestructive = false,
                classification = OperationClassification.AVAILABLE_REAL,
                execute = {
                    val startMs = System.currentTimeMillis()
                    if (context != null) {
                        val pm = context.packageManager
                        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                        val totalCount = apps.size
                        val systemCount = apps.count { (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0 }
                        val userCount = totalCount - systemCount
                        val sortedPackages = apps.map { it.packageName }.sorted().joinToString(",")
                        val hash = computeSha256(sortedPackages.toByteArray())
                        val duration = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)

                        ActionResult(
                            toolId = "app_inventory",
                            success = true,
                            dataSummary = "Inventaire réel PackageManager : $totalCount packages détectés ($userCount utilisateur, $systemCount système).",
                            verificationProof = "REAL-PKG-SHA256:$hash",
                            executionTimeMs = duration,
                            classification = OperationClassification.AVAILABLE_REAL,
                            observableMetrics = mapOf(
                                "totalPackages" to totalCount.toString(),
                                "userPackages" to userCount.toString(),
                                "systemPackages" to systemCount.toString()
                            )
                        )
                    } else {
                        val classpath = System.getProperty("java.class.path", "")
                        val entries = classpath.split(File.pathSeparator).filter { it.isNotBlank() }
                        val hash = computeSha256(entries.sorted().joinToString(",").toByteArray())
                        val duration = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)

                        ActionResult(
                            toolId = "app_inventory",
                            success = true,
                            dataSummary = "[MODE: JVM_CLASSPATH_INSPECTION] Contexte Android absent : ${entries.size} modules/bibliothèques JVM recensés.",
                            verificationProof = "REAL-JVM-PKG-SHA256:$hash",
                            executionTimeMs = duration,
                            classification = OperationClassification.EXPERIMENTAL_SANDBOX,
                            observableMetrics = mapOf("jvmModules" to entries.size.toString())
                        )
                    }
                }
            )
        )

        // 2. Analyse de l'espace de stockage (Réel via StatFs / File partition metrics)
        registerTool(
            ActionTool(
                id = "storage_analysis",
                name = "Analyse du Stockage",
                description = "Mesure réelle des octets totaux, libres et utilisés sur la partition de stockage.",
                requiredPermissions = emptyList(),
                riskLevel = RiskLevel.LOW,
                isDestructive = false,
                classification = OperationClassification.AVAILABLE_REAL,
                execute = {
                    val startMs = System.currentTimeMillis()
                    ensureWorkspaceInitialized()
                    val targetDir = context?.filesDir ?: workspaceRoot
                    var totalBytes = targetDir.totalSpace
                    var freeBytes = targetDir.usableSpace

                    if (context != null) {
                        try {
                            val statFs = StatFs(targetDir.absolutePath)
                            if (statFs.totalBytes > 0L) {
                                totalBytes = statFs.totalBytes
                                freeBytes = statFs.availableBytes
                            }
                        } catch (_: Throwable) {
                            // Use File.totalSpace / usableSpace fallback
                        }
                    }

                    val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
                    val totalGb = totalBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
                    val usedGb = usedBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
                    val freeGb = freeBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)

                    val rawMetric = "PATH=${targetDir.absolutePath}:TOTAL=$totalBytes:USED=$usedBytes:FREE=$freeBytes"
                    val hash = computeSha256(rawMetric.toByteArray())
                    val duration = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)

                    ActionResult(
                        toolId = "storage_analysis",
                        success = true,
                        dataSummary = String.format(
                            java.util.Locale.US,
                            "Partition réelle (%s) : %.2f Go utilisés sur %.2f Go (%.2f Go disponibles).",
                            targetDir.name,
                            usedGb,
                            totalGb,
                            freeGb
                        ),
                        verificationProof = "REAL-STATFS-SHA256:$hash",
                        executionTimeMs = duration,
                        classification = OperationClassification.AVAILABLE_REAL,
                        observableArtifactPath = targetDir.absolutePath,
                        observableMetrics = mapOf(
                            "totalBytes" to totalBytes.toString(),
                            "usedBytes" to usedBytes.toString(),
                            "freeBytes" to freeBytes.toString()
                        )
                    )
                }
            )
        )

        // 3. Recherche de doublons par empreinte SHA-256 réelle des fichiers
        registerTool(
            ActionTool(
                id = "duplicate_search",
                name = "Recherche de Doublons",
                description = "Parcourt l'espace de fichiers local et compare les condensats SHA-256 réels.",
                requiredPermissions = emptyList(),
                riskLevel = RiskLevel.LOW,
                isDestructive = false,
                classification = OperationClassification.AVAILABLE_REAL,
                execute = {
                    val startMs = System.currentTimeMillis()
                    ensureWorkspaceInitialized()
                    val allFiles = workspaceRoot.walkTopDown().filter { it.isFile && it.canRead() }.toList()
                    val byHash = mutableMapOf<String, MutableList<File>>()

                    for (file in allFiles) {
                        val fileHash = "${file.length()}-${computeFileSha256(file)}"
                        byHash.getOrPut(fileHash) { mutableListOf() }.add(file)
                    }

                    val duplicateGroups = byHash.values.filter { it.size > 1 }
                    val duplicateFileCount = duplicateGroups.sumOf { it.size - 1 }
                    val recoverableBytes = duplicateGroups.sumOf { group ->
                        group.drop(1).sumOf { it.length() }
                    }

                    val reportFile = File(workspaceRoot, "duplicate_scan_report.json")
                    val reportJson = buildString {
                        append("{")
                        append("\"scannedFiles\":${allFiles.size},")
                        append("\"duplicateFiles\":$duplicateFileCount,")
                        append("\"recoverableBytes\":$recoverableBytes,")
                        append("\"timestamp\":${System.currentTimeMillis()}")
                        append("}")
                    }
                    reportFile.writeText(reportJson)
                    val hash = computeFileSha256(reportFile)
                    val duration = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)

                    ActionResult(
                        toolId = "duplicate_search",
                        success = true,
                        dataSummary = "Scan SHA-256 réel sur ${allFiles.size} fichiers : $duplicateFileCount doublons détectés ($recoverableBytes octets récupérables).",
                        verificationProof = "REAL-DUP-SHA256:$hash",
                        executionTimeMs = duration,
                        classification = OperationClassification.AVAILABLE_REAL,
                        observableArtifactPath = reportFile.absolutePath,
                        observableMetrics = mapOf(
                            "scannedFiles" to allFiles.size.toString(),
                            "duplicateFiles" to duplicateFileCount.toString(),
                            "recoverableBytes" to recoverableBytes.toString()
                        )
                    )
                }
            )
        )

        // 4. Analyse des fichiers volumineux (Mesure réelle sur disque)
        registerTool(
            ActionTool(
                id = "large_file_analysis",
                name = "Analyse des Fichiers Volumineux",
                description = "Classe les fichiers réels de l'espace applicatif par taille décroissante.",
                requiredPermissions = emptyList(),
                riskLevel = RiskLevel.LOW,
                isDestructive = false,
                classification = OperationClassification.AVAILABLE_REAL,
                execute = {
                    val startMs = System.currentTimeMillis()
                    ensureWorkspaceInitialized()
                    val roots = listOfNotNull(workspaceRoot, context?.cacheDir).distinct()
                    val allFiles = roots.flatMap { root ->
                        if (root.exists()) root.walkTopDown().filter { it.isFile }.toList() else emptyList()
                    }.distinctBy { it.absolutePath }.sortedByDescending { it.length() }

                    val totalBytes = allFiles.sumOf { it.length() }
                    val topFiles = allFiles.take(5)
                    val topSummary = if (topFiles.isEmpty()) {
                        "Aucun fichier"
                    } else {
                        topFiles.joinToString(", ") { "${it.name} (${it.length()} o)" }
                    }

                    val manifestString = allFiles.joinToString("|") { "${it.name}:${it.length()}" }
                    val hash = computeSha256(manifestString.toByteArray())
                    val duration = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)

                    ActionResult(
                        toolId = "large_file_analysis",
                        success = true,
                        dataSummary = "${allFiles.size} fichiers analysés (Total: $totalBytes octets). Plus volumineux : $topSummary.",
                        verificationProof = "REAL-LARGEFILE-SHA256:$hash",
                        executionTimeMs = duration,
                        classification = OperationClassification.AVAILABLE_REAL,
                        observableArtifactPath = workspaceRoot.absolutePath,
                        observableMetrics = mapOf(
                            "fileCount" to allFiles.size.toString(),
                            "totalBytes" to totalBytes.toString()
                        )
                    )
                }
            )
        )

        // 5. Organisation automatique de documents (Création d'index et classement réel)
        registerTool(
            ActionTool(
                id = "document_organizer",
                name = "Organisateur de Documents",
                description = "Classe les fichiers du répertoire local par catégorie MIME/extension et génère un index vérifiable.",
                requiredPermissions = emptyList(),
                riskLevel = RiskLevel.MEDIUM,
                isDestructive = false,
                classification = OperationClassification.AVAILABLE_REAL,
                execute = {
                    val startMs = System.currentTimeMillis()
                    ensureWorkspaceInitialized()
                    val docsDir = File(workspaceRoot, "documents")
                    val files = docsDir.walkTopDown().filter { it.isFile }.toList()
                    val byExt = files.groupBy { it.extension.lowercase().ifBlank { "sans_ext" } }

                    val indexFile = File(workspaceRoot, "document_organization_index.json")
                    val categoriesSummary = byExt.entries.joinToString(", ") { "${it.key.uppercase()}: ${it.value.size}" }
                    indexFile.writeText(
                        """{"organizedFiles":${files.size},"categories":"$categoriesSummary","timestamp":${System.currentTimeMillis()}}"""
                    )

                    val hash = computeFileSha256(indexFile)
                    val duration = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)

                    ActionResult(
                        toolId = "document_organizer",
                        success = true,
                        dataSummary = "${files.size} fichiers indexés et classés ($categoriesSummary). Index écrit dans ${indexFile.name}.",
                        verificationProof = "REAL-DOCINDEX-SHA256:$hash",
                        executionTimeMs = duration,
                        classification = OperationClassification.AVAILABLE_REAL,
                        observableArtifactPath = indexFile.absolutePath,
                        observableMetrics = mapOf("organizedCount" to files.size.toString())
                    )
                }
            )
        )

        // 6. Diagnostic batterie (Lecture réelle via BatteryManager / ACTION_BATTERY_CHANGED)
        registerTool(
            ActionTool(
                id = "battery_diagnostic",
                name = "Diagnostic Batterie",
                description = "Lit le niveau, la température et la tension réels via Android BatteryManager.",
                requiredPermissions = emptyList(),
                riskLevel = RiskLevel.LOW,
                isDestructive = false,
                classification = OperationClassification.AVAILABLE_REAL,
                execute = {
                    val startMs = System.currentTimeMillis()
                    if (context != null) {
                        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                        val capacityProp = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
                        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                        val levelExtra = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                        val scaleExtra = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
                        val computedPct = if (levelExtra >= 0 && scaleExtra > 0) (levelExtra * 100) / scaleExtra else capacityProp
                        val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
                        val tempC = tempTenths / 10.0f
                        val voltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0

                        val rawTelemetry = "LEVEL=$computedPct:TEMP=$tempC:VOLTAGE=$voltageMv"
                        val hash = computeSha256(rawTelemetry.toByteArray())
                        val duration = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)

                        ActionResult(
                            toolId = "battery_diagnostic",
                            success = true,
                            dataSummary = "Capteur BatteryManager réel : Niveau $computedPct%, Température ${tempC}°C, Tension ${voltageMv}mV.",
                            verificationProof = "REAL-BAT-SHA256:$hash",
                            executionTimeMs = duration,
                            classification = OperationClassification.AVAILABLE_REAL,
                            observableMetrics = mapOf(
                                "batteryPercent" to computedPct.toString(),
                                "temperatureC" to tempC.toString(),
                                "voltageMv" to voltageMv.toString()
                            )
                        )
                    } else {
                        val linuxCapacityFile = File("/sys/class/power_supply/battery/capacity")
                        val linuxCapacity = if (linuxCapacityFile.exists()) linuxCapacityFile.readText().trim() else "N/A (Hôte JVM sans batterie)"
                        val raw = "JVM_HOST_POWER:$linuxCapacity:${Runtime.getRuntime().availableProcessors()}"
                        val hash = computeSha256(raw.toByteArray())
                        val duration = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)

                        ActionResult(
                            toolId = "battery_diagnostic",
                            success = true,
                            dataSummary = "[MODE: SANS_CAPTEUR_ANDROID_JVM] Alimentation hôte : $linuxCapacity (Aucune valeur fictive injectée).",
                            verificationProof = "REAL-HOSTPWR-SHA256:$hash",
                            executionTimeMs = duration,
                            classification = OperationClassification.EXPERIMENTAL_SANDBOX,
                            observableMetrics = mapOf("hostPowerState" to linuxCapacity)
                        )
                    }
                }
            )
        )

        // 7. Diagnostic réseau (Inspection réelle via NetworkInterface & ConnectivityManager)
        registerTool(
            ActionTool(
                id = "network_diagnostic",
                name = "Diagnostic Réseau",
                description = "Inspecte les interfaces réseau réelles du noyau et les capacités ConnectivityManager.",
                requiredPermissions = listOf("android.permission.ACCESS_NETWORK_STATE"),
                riskLevel = RiskLevel.LOW,
                isDestructive = false,
                classification = OperationClassification.AVAILABLE_REAL,
                execute = {
                    val startMs = System.currentTimeMillis()
                    val interfaces = try {
                        Collections.list(NetworkInterface.getNetworkInterfaces()).filter { it.isUp }
                    } catch (_: Exception) {
                        emptyList()
                    }
                    val ifaceNames = interfaces.map { it.name }
                    var downKbps = 0
                    var upKbps = 0

                    if (context != null) {
                        try {
                            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                            val activeNet = cm?.activeNetwork
                            val caps = if (activeNet != null) cm.getNetworkCapabilities(activeNet) else null
                            downKbps = caps?.linkDownstreamBandwidthKbps ?: 0
                            upKbps = caps?.linkUpstreamBandwidthKbps ?: 0
                        } catch (_: Throwable) {}
                    }

                    val rawNet = "IFACES=${ifaceNames.joinToString(",")}:DOWN=$downKbps:UP=$upKbps"
                    val hash = computeSha256(rawNet.toByteArray())
                    val duration = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)

                    ActionResult(
                        toolId = "network_diagnostic",
                        success = true,
                        dataSummary = "Interfaces réseau actives (${ifaceNames.size}) : ${ifaceNames.joinToString(", ").ifBlank { "lo" }} | Bande passante système : ↓${downKbps} kbps / ↑${upKbps} kbps.",
                        verificationProof = "REAL-NET-SHA256:$hash",
                        executionTimeMs = duration,
                        classification = OperationClassification.AVAILABLE_REAL,
                        observableMetrics = mapOf(
                            "activeInterfaces" to ifaceNames.joinToString(","),
                            "downstreamKbps" to downKbps.toString(),
                            "upstreamKbps" to upKbps.toString()
                        )
                    )
                }
            )
        )

        // 8. Audit des permissions (Réel via PermissionScanner sans injection fictive)
        registerTool(
            ActionTool(
                id = "permission_audit",
                name = "Audit des Permissions",
                description = "Vérifie les permissions sensibles réellement accordées et demandées sur l'appareil.",
                requiredPermissions = emptyList(),
                riskLevel = RiskLevel.LOW,
                isDestructive = false,
                classification = OperationClassification.AVAILABLE_REAL,
                execute = {
                    val startMs = System.currentTimeMillis()
                    if (context != null) {
                        val scanner = PermissionScanner(context)
                        val risks = scanner.scanInstalledApps()
                        val criticalCount = risks.count { it.riskLevel == RiskLevel.CRITICAL }
                        val highCount = risks.count { it.riskLevel == RiskLevel.HIGH }
                        val raw = risks.joinToString("|") { "${it.packageName}:${it.riskLevel}:${it.dangerousPermissions.size}" }
                        val hash = computeSha256(raw.toByteArray())
                        val duration = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)

                        ActionResult(
                            toolId = "permission_audit",
                            success = true,
                            dataSummary = "Audit PackageManager réel : ${risks.size} applications avec permissions sensibles ($criticalCount critiques, $highCount élevées). Zéro donnée fictive.",
                            verificationProof = "REAL-PERM-SHA256:$hash",
                            executionTimeMs = duration,
                            classification = OperationClassification.AVAILABLE_REAL,
                            observableMetrics = mapOf(
                                "riskyAppsCount" to risks.size.toString(),
                                "criticalCount" to criticalCount.toString(),
                                "highCount" to highCount.toString()
                            )
                        )
                    } else {
                        val securityManagerPresent = System.getSecurityManager() != null
                        val raw = "JVM_SECURITY_AUDIT:SM=$securityManagerPresent:USER=${System.getProperty("user.name", "jvm")}"
                        val hash = computeSha256(raw.toByteArray())
                        val duration = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)

                        ActionResult(
                            toolId = "permission_audit",
                            success = true,
                            dataSummary = "[MODE: JVM_SANDBOX_AUDIT] Contexte Android absent. Aucune application fictive injectée (0 fausse alerte).",
                            verificationProof = "REAL-JVMPERM-SHA256:$hash",
                            executionTimeMs = duration,
                            classification = OperationClassification.EXPERIMENTAL_SANDBOX,
                            observableMetrics = mapOf("riskyAppsCount" to "0")
                        )
                    }
                }
            )
        )

        // 9. Surveillance des ressources (RAM & CPU réels via Runtime & ActivityManager)
        registerTool(
            ActionTool(
                id = "resource_monitor",
                name = "Moniteur de Ressources",
                description = "Mesure la mémoire vive réelle (JVM Heap & ActivityManager) et les cœurs CPU.",
                requiredPermissions = emptyList(),
                riskLevel = RiskLevel.LOW,
                isDestructive = false,
                classification = OperationClassification.AVAILABLE_REAL,
                execute = {
                    val startMs = System.currentTimeMillis()
                    val runtime = Runtime.getRuntime()
                    val cores = runtime.availableProcessors()
                    val jvmTotalMb = runtime.totalMemory() / (1024 * 1024)
                    val jvmFreeMb = runtime.freeMemory() / (1024 * 1024)
                    val jvmUsedMb = jvmTotalMb - jvmFreeMb
                    val jvmMaxMb = runtime.maxMemory() / (1024 * 1024)

                    var sysAvailMb = jvmFreeMb
                    var sysTotalMb = jvmMaxMb
                    if (context != null) {
                        try {
                            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                            val memInfo = ActivityManager.MemoryInfo()
                            am?.getMemoryInfo(memInfo)
                            if (memInfo.totalMem > 0) {
                                sysAvailMb = memInfo.availMem / (1024 * 1024)
                                sysTotalMb = memInfo.totalMem / (1024 * 1024)
                            }
                        } catch (_: Throwable) {}
                    }

                    val sysUsedMb = (sysTotalMb - sysAvailMb).coerceAtLeast(0L)
                    val raw = "CORES=$cores:JVM_USED=$jvmUsedMb:SYS_USED=$sysUsedMb:SYS_TOTAL=$sysTotalMb"
                    val hash = computeSha256(raw.toByteArray())
                    val duration = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)

                    ActionResult(
                        toolId = "resource_monitor",
                        success = true,
                        dataSummary = "Ressources réelles : $cores cœurs CPU | Heap JVM: $jvmUsedMb/$jvmMaxMb Mo | RAM Système: $sysUsedMb/$sysTotalMb Mo utilisés.",
                        verificationProof = "REAL-RES-SHA256:$hash",
                        executionTimeMs = duration,
                        classification = OperationClassification.AVAILABLE_REAL,
                        observableMetrics = mapOf(
                            "cpuCores" to cores.toString(),
                            "jvmUsedMb" to jvmUsedMb.toString(),
                            "systemUsedMb" to sysUsedMb.toString(),
                            "systemTotalMb" to sysTotalMb.toString()
                        )
                    )
                }
            )
        )

        // 10. Création de sauvegarde locale (Écriture réelle d'un fichier snapshot sur disque + hash SHA-256 du fichier)
        registerTool(
            ActionTool(
                id = "backup_creator",
                name = "Créateur de Sauvegarde",
                description = "Écrit une sauvegarde JSON réelle et vérifiable sur le système de fichiers local.",
                requiredPermissions = emptyList(),
                riskLevel = RiskLevel.MEDIUM,
                isDestructive = false,
                classification = OperationClassification.AVAILABLE_REAL,
                execute = { params ->
                    val startMs = System.currentTimeMillis()
                    ensureWorkspaceInitialized()
                    val backupDir = File(workspaceRoot, "backups")
                    if (!backupDir.exists()) backupDir.mkdirs()

                    val timestamp = System.currentTimeMillis()
                    val backupFile = File(backupDir, "nexus_snapshot_$timestamp.json")
                    val registeredToolsList = tools.keys.sorted().joinToString(",")
                    val payload = buildString {
                        append("{\n")
                        append("  \"snapshotId\": \"SNAP-$timestamp\",\n")
                        append("  \"createdAtMs\": $timestamp,\n")
                        append("  \"registeredToolsCount\": ${tools.size},\n")
                        append("  \"registeredTools\": \"$registeredToolsList\",\n")
                        append("  \"parameters\": \"${params.entries.joinToString(",") { "${it.key}=${it.value}" }}\"\n")
                        append("}")
                    }
                    backupFile.writeText(payload)

                    val existsAndNonEmpty = backupFile.exists() && backupFile.length() > 0L
                    val fileHash = if (existsAndNonEmpty) computeFileSha256(backupFile) else "INVALID"
                    val duration = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)

                    ActionResult(
                        toolId = "backup_creator",
                        success = existsAndNonEmpty,
                        dataSummary = "Sauvegarde réelle écrite sur disque : ${backupFile.name} (${backupFile.length()} octets).",
                        verificationProof = "REAL-FILE-SHA256:$fileHash",
                        executionTimeMs = duration,
                        classification = OperationClassification.AVAILABLE_REAL,
                        observableArtifactPath = backupFile.absolutePath,
                        observableMetrics = mapOf(
                            "filePath" to backupFile.absolutePath,
                            "fileSizeBytes" to backupFile.length().toString(),
                            "sha256" to fileHash
                        )
                    )
                }
            )
        )
    }

    fun registerTool(tool: ActionTool) {
        tools[tool.id] = tool
    }

    fun getTool(id: String): ActionTool? = tools[id]

    fun getAllTools(): List<ActionTool> = tools.values.toList()
}
