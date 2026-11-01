package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionState
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    state: ActionState,
    modifier: Modifier = Modifier
) {
    val (bg, textColor, borderColor) = when (state) {
        ActionState.PROPOSED -> Triple(CyberAmber.copy(alpha = 0.15f), CyberAmber, CyberAmber.copy(alpha = 0.4f))
        ActionState.AUTHORIZED -> Triple(ElectricBlue.copy(alpha = 0.2f), ElectricCyan, ElectricBlue)
        ActionState.EXECUTED -> Triple(NeonPurple.copy(alpha = 0.2f), ElectricCyan, NeonPurple)
        ActionState.VERIFIED -> Triple(CyberGreen.copy(alpha = 0.2f), CyberGreen, CyberGreen)
        ActionState.FAILED -> Triple(CyberRed.copy(alpha = 0.2f), CyberRed, CyberRed)
        ActionState.CANCELLED -> Triple(TextMuted.copy(alpha = 0.2f), TextSecondary, TextMuted)
    }

    Box(
        modifier = modifier
            .testTag("status_badge_${state.name.lowercase()}")
            .background(bg, shape = RoundedCornerShape(6.dp))
            .border(1.dp, borderColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = state.name,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
