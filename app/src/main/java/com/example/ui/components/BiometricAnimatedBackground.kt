package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import kotlin.math.cos
import kotlin.math.sin

private data class AnimatedParticle(
    val baseNormalizedX: Float,
    val baseNormalizedY: Float,
    val radius: Float,
    val phase: Float,
    val speedFactor: Float,
    val color: Color
)

@Composable
fun BiometricAnimatedLoopBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BiometricBackgroundTransition")

    // Continuous loop rotation and wave pulses (0f to 360f / 1f)
    val animationProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loopProgress"
    )

    val pulseScale = infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Pre-calculate fixed particle seeds
    val particles = remember {
        listOf(
            AnimatedParticle(0.20f, 0.25f, 6f, 0.0f, 1.2f, EmeraldPrimary.copy(alpha = 0.35f)),
            AnimatedParticle(0.80f, 0.22f, 5f, 1.2f, 0.9f, EmeraldSecondary.copy(alpha = 0.30f)),
            AnimatedParticle(0.15f, 0.70f, 8f, 2.4f, 1.1f, EmeraldPrimary.copy(alpha = 0.28f)),
            AnimatedParticle(0.85f, 0.75f, 7f, 3.6f, 1.3f, EmeraldSecondary.copy(alpha = 0.32f)),
            AnimatedParticle(0.30f, 0.85f, 5f, 4.2f, 0.8f, EmeraldPrimary.copy(alpha = 0.25f)),
            AnimatedParticle(0.70f, 0.88f, 6f, 5.0f, 1.0f, EmeraldSecondary.copy(alpha = 0.30f)),
            AnimatedParticle(0.50f, 0.15f, 7f, 0.8f, 1.4f, EmeraldPrimary.copy(alpha = 0.35f)),
            AnimatedParticle(0.50f, 0.92f, 5f, 2.0f, 0.7f, EmeraldSecondary.copy(alpha = 0.25f)),
            AnimatedParticle(0.12f, 0.45f, 6f, 3.1f, 1.0f, EmeraldPrimary.copy(alpha = 0.30f)),
            AnimatedParticle(0.88f, 0.48f, 7f, 4.5f, 1.2f, EmeraldSecondary.copy(alpha = 0.28f))
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f
        val maxDim = maxOf(width, height)

        val progress = animationProgress.value
        val twoPi = (2.0 * Math.PI).toFloat()

        // Background subtle radial glow
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    EmeraldPrimary.copy(alpha = 0.08f * pulseScale.value),
                    Color(0xFF0F1E17),
                    Color(0xFF0A120E)
                ),
                center = Offset(centerX, centerY),
                radius = maxDim * 0.75f
            )
        )

        // Draw animated concentric biometric ripple waves
        for (i in 1..4) {
            val wavePhase = (progress + (i * 0.25f)) % 1f
            val waveRadius = 70f + wavePhase * (maxDim * 0.45f)
            val waveAlpha = (1f - wavePhase).coerceIn(0f, 1f) * 0.25f
            drawCircle(
                color = EmeraldPrimary.copy(alpha = waveAlpha),
                radius = waveRadius,
                center = Offset(centerX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
            )
        }

        // Draw connecting mesh lines between nearby particles
        val calculatedParticlePositions = particles.map { p ->
            val angle = (progress * twoPi * p.speedFactor + p.phase)
            val offsetX = cos(angle.toDouble()).toFloat() * 24f
            val offsetY = sin(angle.toDouble()).toFloat() * 24f
            val x = (p.baseNormalizedX * width) + offsetX
            val y = (p.baseNormalizedY * height) + offsetY
            Offset(x, y) to p
        }

        for (i in calculatedParticlePositions.indices) {
            for (j in i + 1 until calculatedParticlePositions.size) {
                val (pos1) = calculatedParticlePositions[i]
                val (pos2) = calculatedParticlePositions[j]
                val dx = pos1.x - pos2.x
                val dy = pos1.y - pos2.y
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                if (dist < width * 0.38f) {
                    val lineAlpha = (1f - (dist / (width * 0.38f))).coerceIn(0f, 1f) * 0.15f
                    drawLine(
                        color = EmeraldPrimary.copy(alpha = lineAlpha),
                        start = pos1,
                        end = pos2,
                        strokeWidth = 1.2f
                    )
                }
            }
        }

        // Draw particles with glowing core
        calculatedParticlePositions.forEach { (pos, p) ->
            drawCircle(
                color = p.color.copy(alpha = p.color.alpha * 0.5f),
                radius = p.radius * pulseScale.value * 1.5f,
                center = pos
            )
            drawCircle(
                color = p.color,
                radius = p.radius,
                center = pos
            )
        }
    }
}
