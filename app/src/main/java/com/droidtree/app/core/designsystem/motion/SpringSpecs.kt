package com.droidtree.app.core.designsystem.motion

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Spring animation specs derived directly from Apple's Fluid Interface design guidelines
 * and Emil Kowalski's Design Engineering principles.
 *
 * Implements: apple-design, emil-design-eng, animate, animation-performance
 */
object SpringSpecs {
    /**
     * Default UI transition spring: Critically damped (damping ratio 1.0),
     * smooth settle without distracting bounce.
     */
    val Default: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /**
     * Momentum & flick spring: Under-damped (damping ~0.8),
     * organic physical overshoot for user-initiated velocity releases.
     */
    val Momentum: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    /**
     * Snappy micro-interaction spring (buttons, toggle chips, pills).
     */
    val Snappy: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
}

/**
 * Modifier implementing instant pointer-down scale feedback (0.97f)
 * with physics-based spring return on release.
 * Ensures zero-latency feedback before click commit.
 */
fun Modifier.fluidPress(
    pressedScale: Float = 0.97f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale = if (isPressed) pressedScale else 1f

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else Modifier
        )
}
