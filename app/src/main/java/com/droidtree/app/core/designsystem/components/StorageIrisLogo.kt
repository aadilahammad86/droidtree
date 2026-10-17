package com.droidtree.app.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Storage Iris Brand Logo in macOS Glass styling.
 * Pure vector foreground icon without container clipping.
 */
@Composable
fun StorageIrisLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val scale = w / 200f

        val strokeOuter = 16f * scale
        val strokeInner = 14f * scale
        val rOuter = 74f * scale
        val rInner = 44f * scale
        val rCore = 17f * scale

        // Primary arc glass gradient
        val primaryGlass = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFFFFF).copy(alpha = 0.90f),
                Color(0xFF93C5FD).copy(alpha = 0.75f),
                Color(0xFF3B82F6).copy(alpha = 0.65f),
                Color(0xFF1D4ED8).copy(alpha = 0.55f)
            ),
            start = Offset(cx - rOuter, cy - rOuter),
            end = Offset(cx + rOuter, cy + rOuter)
        )

        // Secondary arc glass gradient
        val secondaryGlass = Brush.linearGradient(
            colors = listOf(
                Color(0xFFE0F2FE).copy(alpha = 0.90f),
                Color(0xFF60A5FA).copy(alpha = 0.70f),
                Color(0xFF2563EB).copy(alpha = 0.60f)
            ),
            start = Offset(cx - rOuter, cy),
            end = Offset(cx + rOuter, cy)
        )

        // Outer Arcs
        // Sector 1: Bottom-Left (90° to 200°) -> sweep 110°
        drawArc(
            brush = primaryGlass,
            startAngle = 90f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(cx - rOuter, cy - rOuter),
            size = Size(rOuter * 2, rOuter * 2),
            style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
        )

        // Sector 2: Top-Left (215° to 315°) -> sweep 100°
        drawArc(
            brush = secondaryGlass,
            startAngle = 215f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(cx - rOuter, cy - rOuter),
            size = Size(rOuter * 2, rOuter * 2),
            style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
        )

        // Sector 3: Top-Right Apex before Aperture (330° to 25°) -> sweep 55°
        drawArc(
            brush = primaryGlass,
            startAngle = 330f,
            sweepAngle = 55f,
            useCenter = false,
            topLeft = Offset(cx - rOuter, cy - rOuter),
            size = Size(rOuter * 2, rOuter * 2),
            style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
        )

        // Inner Arcs
        // Arc 1: 110° to 310° -> sweep 200°
        drawArc(
            brush = secondaryGlass,
            startAngle = 110f,
            sweepAngle = 200f,
            useCenter = false,
            topLeft = Offset(cx - rInner, cy - rInner),
            size = Size(rInner * 2, rInner * 2),
            style = Stroke(width = strokeInner, cap = StrokeCap.Round)
        )

        // Arc 2: 330° to 90° -> sweep 120°
        drawArc(
            brush = primaryGlass,
            startAngle = 330f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(cx - rInner, cy - rInner),
            size = Size(rInner * 2, rInner * 2),
            style = Stroke(width = strokeInner, cap = StrokeCap.Round)
        )

        // Specular Top Sheen Highlight
        drawArc(
            color = Color.White.copy(alpha = 0.85f),
            startAngle = 230f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = Offset(cx - rOuter, cy - rOuter),
            size = Size(rOuter * 2, rOuter * 2),
            style = Stroke(width = 2.5f * scale, cap = StrokeCap.Round)
        )

        // Core / Nucleus Radial Lens
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    Color(0xFFBAE6FD),
                    Color(0xFF3B82F6),
                    Color(0xFF1D4ED8)
                ),
                center = Offset(cx - 3f * scale, cy - 3f * scale),
                radius = rCore
            ),
            radius = rCore,
            center = Offset(cx, cy)
        )

        // Specular glint on core
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = 3.5f * scale,
            center = Offset(cx - 4f * scale, cy - 4f * scale)
        )
    }
}
