package com.example.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.navigation.NexusScreen
import com.example.ui.theme.*

@Composable
fun NexusBottomBar(
    currentScreen: NexusScreen,
    onScreenSelected: (NexusScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val mainScreens = listOf(
        NexusScreen.Dashboard,
        NexusScreen.Assistant,
        NexusScreen.Agents,
        NexusScreen.Security,
        NexusScreen.Settings
    )

    NavigationBar(
        containerColor = DeepObsidian,
        tonalElevation = 8.dp,
        modifier = modifier
    ) {
        mainScreens.forEach { screen ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(
                        text = screen.title.split(" ").first(),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ElectricCyan,
                    selectedTextColor = ElectricCyan,
                    indicatorColor = GlowBlue,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier.testTag("nav_item_${screen.route}")
            )
        }
    }
}
