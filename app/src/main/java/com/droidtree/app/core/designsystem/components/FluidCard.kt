package com.droidtree.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.droidtree.app.core.designsystem.motion.fluidPress
import com.droidtree.app.core.designsystem.theme.TranslucentBorder
import com.droidtree.app.core.designsystem.theme.TranslucentGlass

/**
 * Fluid interactive card implementing apple-design and emil-design-eng:
 * - Translucent glass background
 * - Subtle 1px border highlight reflecting physical material depth
 * - Zero-latency spring-press micro-interaction
 */
@Composable
fun FluidCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    borderColor: Color = TranslucentBorder,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val pressModifier = if (onClick != null) {
        Modifier.fluidPress(pressedScale = 0.98f, onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .then(pressModifier)
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape),
        content = content
    )
}
