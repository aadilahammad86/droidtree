package com.droidtree.app.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.droidtree.app.core.designsystem.theme.DroidPrimary
import com.droidtree.app.core.designsystem.theme.DroidSecondary
import java.util.Locale

/**
 * Animated storage capacity gauge.
 * Follows apple-design and mobile-app-ui-design guidelines with smooth ease-out entry.
 */
@Composable
fun StorageGaugeRing(
    usedPercentage: Float,
    usedFormatted: String,
    totalFormatted: String,
    modifier: Modifier = Modifier,
    ringSize: Dp = 180.dp,
    strokeWidth: Dp = 14.dp
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(usedPercentage) {
        animatedProgress.animateTo(
            targetValue = usedPercentage.coerceIn(0f, 1f),
            animationSpec = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            )
        )
    }

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val strokePx = strokeWidth.toPx()

            // Background track (Free space)
            drawCircle(
                color = DroidSecondary.copy(alpha = 0.2f),
                style = Stroke(width = strokePx)
            )

            // Animated Used arc
            drawArc(
                color = DroidPrimary,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress.value,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format(Locale.US, "%.1f%%", animatedProgress.value * 100),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$usedFormatted / $totalFormatted",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
