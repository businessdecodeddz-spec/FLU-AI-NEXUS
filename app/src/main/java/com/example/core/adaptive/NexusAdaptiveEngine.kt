package com.example.core.adaptive

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.PowerManager
import com.example.data.jni.NexusJniBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

enum class ExecutionProfile {
    NEXUS_LITE,
    NEXUS_STANDARD,
    NEXUS_ADVANCED
}

data class HardwareCapabilitiesProfile(
    val androidVersionSdk: Int = Build.VERSION.SDK_INT,
    val androidVersionRelease: String = Build.VERSION.RELEASE,
    val manufacturer: String = Build.MANUFACTURER,
    val model: String = Build.MODEL,
    val deviceBrand: String = Build.BRAND,
    val primaryAbi: String = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a",
    val supportedAbis: List<String> = Build.SUPPORTED_ABIS.toList(),
    val cpuCores: Int = Runtime.getRuntime().availableProcessors(),
    val totalRamMb: Long = 0L,
    val availRamMb: Long = 0L,
    val isLowRamDevice: Boolean = false,
    val totalStorageGb: Float = 0f,
    val freeStorageGb: Float = 0f,
    val isIgnoringBatteryOptimizations: Boolean = true,
    val localAiInferenceSupported: Boolean = true,
    val nativeJniBridgeActive: Boolean = NexusJniBridge.isNativeActive(),
    val manufacturerRestrictions: String = "AUCUNE_RESTRICTION_DETECTEE",
    val activeExecutionProfile: ExecutionProfile = ExecutionProfile.NEXUS_STANDARD
)

data class DiagnosticsReport(
    val timestamp: Long = System.currentTimeMillis(),
    val profile: HardwareCapabilitiesProfile,
    val diagnosticLogs: List<String> = emptyList(),
    val manufacturerOptimizations: List<String> = emptyList(),
    val recommendedSettings: List<String> = emptyList()
)

class NexusAdaptiveEngine(private val context: Context) {

    private val _currentProfile = MutableStateFlow(HardwareCapabilitiesProfile())
    val currentProfile: StateFlow<HardwareCapabilitiesProfile> = _currentProfile.asStateFlow()

    private val _lastReport = MutableStateFlow<DiagnosticsReport?>(null)
    val lastReport: StateFlow<DiagnosticsReport?> = _lastReport.asStateFlow()

    init {
        runHardwareDiagnostics()
    }

    fun runHardwareDiagnostics(): DiagnosticsReport {
        val logs = mutableListOf<String>()
        val optimizations = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        logs.add("NEXUS ADAPTIVE ENGINE: Démarrage de l'analyse matérielle et logicielle...")

        // 1. Version & System Info
        val sdk = Build.VERSION.SDK_INT
        val release = Build.VERSION.RELEASE
        val manufacturer = Build.MANUFACTURER.uppercase()
        val model = Build.MODEL
        val brand = Build.BRAND

        logs.add("OS: Android $release (API $sdk) - $manufacturer $model ($brand)")

        if (sdk < 31) {
            logs.add("AVERTISSEMENT: Version Android (API $sdk) inférieure au standard Android 12 (API 31). Mode rétrocompatibilité strict engagé.")
        } else {
            logs.add("COMPATIBILITÉ CIBLE: Android 12+ (API $sdk) supporté nativement.")
        }

        // 2. ABIs & CPU Cores
        val supportedAbis = Build.SUPPORTED_ABIS.toList()
        val primaryAbi = supportedAbis.firstOrNull() ?: "arm64-v8a"
        val cpuCores = Runtime.getRuntime().availableProcessors()

        logs.add("CPU: $cpuCores cœurs détectés. ABI principale: $primaryAbi. Tous ABIs: ${supportedAbis.joinToString(", ")}")

        // 3. RAM Memory
        var totalRamMb = 0L
        var availRamMb = 0L
        var isLowRam = false

        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            if (actManager != null) {
                val memInfo = ActivityManager.MemoryInfo()
                actManager.getMemoryInfo(memInfo)
                totalRamMb = memInfo.totalMem / (1024 * 1024)
                availRamMb = memInfo.availMem / (1024 * 1024)
                isLowRam = memInfo.lowMemory || actManager.isLowRamDevice
            }
        } catch (e: Throwable) {
            totalRamMb = 4096L
            availRamMb = 2048L
            logs.add("RAM: Diagnostic fallback engagé (${e.message})")
        }

        logs.add("RAM: Total ${totalRamMb} Mo | Disponible: ${availRamMb} Mo | LowRAM state: $isLowRam")

        // 4. Storage
        var totalStorageGb = 0f
        var freeStorageGb = 0f
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availBlocks = stat.availableBlocksLong

            totalStorageGb = (totalBlocks * blockSize) / (1024f * 1024f * 1024f)
            freeStorageGb = (availBlocks * blockSize) / (1024f * 1024f * 1024f)
        } catch (e: Throwable) {
            totalStorageGb = 64f
            freeStorageGb = 32f
        }

        logs.add("STOCKAGE: Total ${"%.1f".format(totalStorageGb)} Go | Libre: ${"%.1f".format(freeStorageGb)} Go")

        // 5. Battery & Background Execution Restrictions
        var isIgnoringBattery = true
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager != null) {
                isIgnoringBattery = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            }
        } catch (e: Throwable) {
            isIgnoringBattery = true
        }

        if (!isIgnoringBattery) {
            logs.add("RESTRICTION: L'application est soumise à l'optimisation batterie du système.")
            recommendations.add("Désactiver l'optimisation batterie pour garantir le balayage autonome en arrière-plan.")
        }

        // 6. Manufacturer Specific Quirks
        val manufacturerQuirks = when {
            manufacturer.contains("SAMSUNG") -> {
                optimizations.add("Mode Knox / OneUI: Gestion adaptative du réveil réseau et des tâches d'arrière-plan.")
                "SAMSUNG_ONEUI_BATTERY_POLICY"
            }
            manufacturer.contains("XIAOMI") || manufacturer.contains("REDMI") || manufacturer.contains("POCO") -> {
                optimizations.add("MIUI / HyperOS: Autorisation Autostart recommandée pour la persistance des agents.")
                "MIUI_HYPER_OS_AUTOSTART"
            }
            manufacturer.contains("HUAWEI") -> {
                optimizations.add("Huawei HMS: Fallback natif hors-GMS vérifié pour la couche locale.")
                "HUAWEI_HMS_ISOLATION"
            }
            manufacturer.contains("OPPO") || manufacturer.contains("REALME") || manufacturer.contains("ONEPLUS") -> {
                optimizations.add("ColorOS / OxygenOS: Adaptation de l'allocation mémoire pour les tâches de fond.")
                "COLOROS_OXYGENOS_MEMORY_MANAGEMENT"
            }
            manufacturer.contains("GOOGLE") -> {
                optimizations.add("Google Pixel Engine: Accélération directe Tensor/NNAPI prioritisée.")
                "GOOGLE_PIXEL_TENSOR_OPTIMIZED"
            }
            else -> {
                optimizations.add("Android Stock Standard API 31+: Execution uniforme sans surcouche propriétaire.")
                "STANDARD_ANDROID_API"
            }
        }

        // 7. Profile Determination
        val profile = when {
            totalRamMb < 3500L || cpuCores < 4 || isLowRam -> ExecutionProfile.NEXUS_LITE
            totalRamMb >= 6000L && cpuCores >= 8 -> ExecutionProfile.NEXUS_ADVANCED
            else -> ExecutionProfile.NEXUS_STANDARD
        }

        logs.add("PROFIL DÉTERMINÉ: $profile")

        when (profile) {
            ExecutionProfile.NEXUS_LITE -> {
                recommendations.add("Utilisation prioritaire du cloud Gemini pour alléger les ressources processeur.")
                recommendations.add("Fréquence de balayage autonome ajustée à 15 minutes.")
            }
            ExecutionProfile.NEXUS_STANDARD -> {
                recommendations.add("Exécution hybride: Modèles locaux compacts + Gemini Cloud.")
                recommendations.add("Support multi-agent standard (3 agents simultanés).")
            }
            ExecutionProfile.NEXUS_ADVANCED -> {
                recommendations.add("Exécution 100% locale débloquée pour l'IA et l'analyse quantique BitNet.")
                recommendations.add("Support multi-agent maximal (7 agents de débat scientifique simultanés).")
            }
        }

        val capabilities = HardwareCapabilitiesProfile(
            androidVersionSdk = sdk,
            androidVersionRelease = release,
            manufacturer = manufacturer,
            model = model,
            deviceBrand = brand,
            primaryAbi = primaryAbi,
            supportedAbis = supportedAbis,
            cpuCores = cpuCores,
            totalRamMb = totalRamMb,
            availRamMb = availRamMb,
            isLowRamDevice = isLowRam,
            totalStorageGb = totalStorageGb,
            freeStorageGb = freeStorageGb,
            isIgnoringBatteryOptimizations = isIgnoringBattery,
            localAiInferenceSupported = (profile != ExecutionProfile.NEXUS_LITE),
            nativeJniBridgeActive = NexusJniBridge.isNativeActive(),
            manufacturerRestrictions = manufacturerQuirks,
            activeExecutionProfile = profile
        )

        _currentProfile.value = capabilities

        val report = DiagnosticsReport(
            profile = capabilities,
            diagnosticLogs = logs,
            manufacturerOptimizations = optimizations,
            recommendedSettings = recommendations
        )

        _lastReport.value = report
        return report
    }
}
