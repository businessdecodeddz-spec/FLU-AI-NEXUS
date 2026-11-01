package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlowCyan
import com.example.ui.theme.NeonPurple
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Central Intelligent AI Core Visualizer with pulsing cyan core, rotating concentric rings,
 * particle nodes, and glowing aura.
 */
@Composable
fun CoreVisualizer(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    isActive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CoreAnimation")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val reverseRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ReverseRotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.width / 2.4f

            // Outer Glow Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GlowCyan, Color.Transparent),
                    center = center,
                    radius = baseRadius * 1.3f
                ),
                radius = baseRadius * 1.3f,
                center = center
            )

            // Outer Rotating Hexagon Ring
            rotate(rotationAngle, pivot = center) {
                drawCircle(
                    color = ElectricCyan.copy(alpha = 0.3f),
                    radius = baseRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // 6 Radial Node Dots
                for (i in 0 until 6) {
                    val angle = Math.toRadians((i * 60).toDouble())
                    val nodeX = center.x + (baseRadius * cos(angle)).toFloat()
                    val nodeY = center.y + (baseRadius * sin(angle)).toFloat()
                    drawCircle(
                        color = ElectricCyan,
                        radius = 4.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                }
            }

            // Inner Counter-Rotating Orbit Ring
            rotate(reverseRotation, pivot = center) {
                drawCircle(
                    color = ElectricBlue.copy(alpha = 0.5f),
                    radius = baseRadius * 0.72f,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )

                // 4 Orbit Nodes
                for (i in 0 until 4) {
                    val angle = Math.toRadians((i * 90).toDouble())
                    val nodeX = center.x + (baseRadius * 0.72f * cos(angle)).toFloat()
                    val nodeY = center.y + (baseRadius * 0.72f * sin(angle)).toFloat()
                    drawCircle(
                        color = NeonPurple,
                        radius = 5.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                }
            }

            // Pulsing Central Core Orb
            val coreRadius = baseRadius * 0.45f * pulseScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ElectricCyan, ElectricBlue, Color.Transparent),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = coreRadius * 0.35f,
                center = center
            )
        }
    }
}
