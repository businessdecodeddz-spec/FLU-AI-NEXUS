package com.example.core

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.example.data.model.AppRiskInfo
import com.example.data.model.RiskLevel

/**
 * NEXUS PERMISSION SCANNER
 * Scans installed Android packages for sensitive permissions using real PackageManager telemetry.
 * Strictly distinguishes:
 *  - Granted permissions (REQUESTED_PERMISSION_GRANTED)
 *  - Requested (not yet granted) permissions
 *  - Special Android access permissions (Overlay, Accessibility, Usage Stats, Install Packages)
 *  - Declared background services
 *
 * RULE ENFORCED: Zero simulated/mock fallback. If 0 risky apps are present, returns an empty list.
 */
class PermissionScanner(private val context: Context) {

    private data class PermissionDefinition(
        val permission: String,
        val label: String,
        val isSpecialAccess: Boolean = false
    )

    private val trackedPermissions = listOf(
        PermissionDefinition("android.permission.SYSTEM_ALERT_WINDOW", "Superposition d'écran (Overlay)", isSpecialAccess = true),
        PermissionDefinition("android.permission.BIND_ACCESSIBILITY_SERVICE", "Service d'Accessibilité", isSpecialAccess = true),
        PermissionDefinition("android.permission.READ_SMS", "Lecture des SMS"),
        PermissionDefinition("android.permission.RECEIVE_SMS", "Réception des SMS"),
        PermissionDefinition("android.permission.RECORD_AUDIO", "Microphone"),
        PermissionDefinition("android.permission.CAMERA", "Appareil Photo"),
        PermissionDefinition("android.permission.ACCESS_FINE_LOCATION", "Géolocalisation précise"),
        PermissionDefinition("android.permission.WRITE_SETTINGS", "Modification des paramètres système", isSpecialAccess = true),
        PermissionDefinition("android.permission.PACKAGE_USAGE_STATS", "Statistiques d'utilisation", isSpecialAccess = true),
        PermissionDefinition("android.permission.REQUEST_INSTALL_PACKAGES", "Installation d'APK tierces", isSpecialAccess = true)
    )

    fun scanInstalledApps(): List<AppRiskInfo> {
        val detectedRisks = mutableListOf<AppRiskInfo>()
        val pm = context.packageManager

        try {
            val flags = PackageManager.GET_PERMISSIONS or PackageManager.GET_SERVICES
            val packages = pm.getInstalledPackages(flags)

            for (pkg in packages) {
                if (pkg.packageName == context.packageName) continue

                val appInfo = pkg.applicationInfo
                val isSystem = appInfo != null && ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0)

                val requestedPerms = pkg.requestedPermissions ?: emptyArray()
                val permFlags = pkg.requestedPermissionsFlags ?: IntArray(0)
                val declaredServicesCount = pkg.services?.size ?: 0

                val grantedSensitive = mutableListOf<String>()
                val requestedOnlySensitive = mutableListOf<String>()
                var hasSpecialAccess = false

                for (i in requestedPerms.indices) {
                    val permName = requestedPerms[i]
                    val def = trackedPermissions.find { it.permission == permName } ?: continue
                    val isGranted = if (i < permFlags.size) {
                        (permFlags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
                    } else {
                        false
                    }

                    if (def.isSpecialAccess) {
                        hasSpecialAccess = true
                    }

                    val statusTag = when {
                        def.isSpecialAccess && isGranted -> "[ACCÈS SPÉCIAL ACCORDÉ]"
                        def.isSpecialAccess -> "[ACCÈS SPÉCIAL DEMANDÉ]"
                        isGranted -> "[ACCORDÉE]"
                        else -> "[DEMANDÉE - NON ACCORDÉE]"
                    }

                    val formatted = "${def.label} $statusTag"
                    if (isGranted) {
                        grantedSensitive.add(formatted)
                    } else {
                        requestedOnlySensitive.add(formatted)
                    }
                }

                val allFindings = grantedSensitive + requestedOnlySensitive
                if (allFindings.isNotEmpty()) {
                    val appLabel = appInfo?.loadLabel(pm)?.toString() ?: pkg.packageName
                    val level = when {
                        grantedSensitive.size >= 3 || (hasSpecialAccess && grantedSensitive.isNotEmpty()) -> RiskLevel.CRITICAL
                        grantedSensitive.size >= 2 || hasSpecialAccess -> RiskLevel.HIGH
                        grantedSensitive.size == 1 -> RiskLevel.MEDIUM
                        else -> RiskLevel.LOW
                    }

                    val desc = buildString {
                        append("${grantedSensitive.size} accordée(s), ${requestedOnlySensitive.size} simplement demandée(s)")
                        if (declaredServicesCount > 0) {
                            append(", $declaredServicesCount service(s) déclaré(s)")
                        }
                        append(". Détail : ${allFindings.joinToString(", ")}.")
                    }

                    val action = when {
                        isSystem -> "Vérifier ou révoquer les permissions accordées dans les paramètres Android"
                        grantedSensitive.isNotEmpty() -> "Révoquer les permissions accordées ou désinstaller l'application"
                        else -> "Aucune permission sensible accordée (surveillance passive)"
                    }

                    detectedRisks.add(
                        AppRiskInfo(
                            packageName = pkg.packageName,
                            appName = appLabel,
                            riskLevel = level,
                            isSystemApp = isSystem,
                            dangerousPermissions = allFindings,
                            riskDescription = desc,
                            suggestedAction = action
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Return only real detected entries; never inject fake fallback apps
        }

        return detectedRisks.sortedBy { it.riskLevel }
    }
}
