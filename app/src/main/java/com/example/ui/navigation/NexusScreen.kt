package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class NexusScreen(val route: String, val title: String, val icon: ImageVector) {
    Dashboard("dashboard", "Dashboard Central", Icons.Default.Dashboard),
    Assistant("assistant", "Assistant NEXUS", Icons.Default.SmartToy),
    Agents("agents", "Centre des Agents", Icons.Default.Hub),
    Status("status", "État du Smartphone", Icons.Default.Analytics),
    Security("security", "Centre Sécurité", Icons.Default.Security),
    Optimization("optimization", "Optimisation", Icons.Default.Speed),
    Storage("storage", "Stockage & Fichiers", Icons.Default.Folder),
    Network("network", "Commandes Réseau", Icons.Default.Wifi),
    Battery("battery", "Intelligence Batterie", Icons.Default.BatteryChargingFull),
    Apps("apps", "Gestion Applications", Icons.Default.Apps),
    Developer("developer", "Dev & Terminal CLI", Icons.Default.Terminal),
    Research("research", "Recherche & Imagerie", Icons.Default.Science),
    Memory("memory", "Mémoire Intelligente", Icons.Default.Psychology),
    Automations("automations", "Automatisations", Icons.Default.AutoMode),
    AgentStore("store", "Boutique & Agents Store", Icons.Default.Storefront),
    History("history", "Historique & Preuves", Icons.Default.FactCheck),
    Settings("settings", "Paramètres & Perms", Icons.Default.Settings)
}
