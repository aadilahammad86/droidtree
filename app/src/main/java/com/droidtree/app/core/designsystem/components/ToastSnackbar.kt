package com.droidtree.app.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.droidtree.app.core.designsystem.theme.DroidPrimary
import com.droidtree.app.core.designsystem.theme.DroidSecondary
import com.droidtree.app.core.designsystem.theme.TranslucentBorder

enum class ToastType {
    INFO, SUCCESS, ERROR
}

/**
 * Toast / notification banner modeled after Sonner's non-blocking, fluid stacked toast pattern.
 * Implements: ask-sonner, apple-design.
 */
@Composable
fun ToastBanner(
    message: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
    type: ToastType = ToastType.INFO
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        val (icon, tint) = when (type) {
            ToastType.SUCCESS -> Icons.Default.CheckCircle to DroidSecondary
            ToastType.INFO, ToastType.ERROR -> Icons.Default.Info to DroidPrimary
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xE61F2430))
                .border(1.dp, TranslucentBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}
