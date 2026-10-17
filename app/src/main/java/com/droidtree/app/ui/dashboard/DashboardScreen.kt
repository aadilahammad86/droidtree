package com.droidtree.app.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.droidtree.app.core.designsystem.components.FluidCard
import com.droidtree.app.core.designsystem.components.StorageGaugeRing
import com.droidtree.app.core.designsystem.components.ToastBanner
import com.droidtree.app.core.designsystem.components.ToastType
import com.droidtree.app.core.designsystem.motion.fluidPress
import com.droidtree.app.core.designsystem.theme.DroidDarkBackground
import com.droidtree.app.core.designsystem.theme.DroidPrimary
import com.droidtree.app.core.model.FileNode
import com.droidtree.app.core.model.StorageCategory
import org.koin.androidx.compose.koinViewModel

/**
 * Dashboard screen container.
 * Implements: android-dev, compose, apple-design.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    onNavigateToTreemap: (FileNode) -> Unit,
    onNavigateToFileBrowser: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is DashboardEffect.NavigateToTreemap -> onNavigateToTreemap(effect.rootNode)
                    is DashboardEffect.NavigateToFileBrowser -> onNavigateToFileBrowser(effect.rootPath)
                    is DashboardEffect.ShowToast -> Unit
                }
            }
        }
    }

    DashboardContent(
        uiState = uiState,
        actions = viewModel
    )
}

/**
 * Pure previewable content composable receiving only state and actions.
 */
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    actions: DashboardActions
) {
    Scaffold(
        containerColor = DroidDarkBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DroidTree",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "WizTree Disk Analyzer",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { actions.onExportPdfReportClick() },
                    modifier = Modifier.fluidPress()
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export PDF Report",
                        tint = DroidPrimary
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Storage Circular Capacity Gauge
                StorageGaugeRing(
                    usedPercentage = uiState.summary.usedPercentage,
                    usedFormatted = uiState.summary.formattedUsed,
                    totalFormatted = uiState.summary.formattedTotal,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Real-time Scanning Progress Bar
                AnimatedVisibility(
                    visible = uiState.isScanning,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = DroidPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.scanProgressText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Primary Start Scan Button
                Button(
                    onClick = { actions.onStartScanClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .fluidPress(pressedScale = 0.98f),
                    enabled = !uiState.isScanning,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DroidPrimary)
                ) {
                    if (uiState.isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Indexing Storage...", style = MaterialTheme.typography.titleMedium)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Full Disk Scan", style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive Navigation Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionTile(
                        icon = Icons.Default.AccountTree,
                        title = "Treemap",
                        subtitle = "Visual Map",
                        modifier = Modifier.weight(1f),
                        onClick = { actions.onOpenTreemapClick() }
                    )

                    ActionTile(
                        icon = Icons.Default.Folder,
                        title = "Explorer",
                        subtitle = "Size Table",
                        modifier = Modifier.weight(1f),
                        onClick = { actions.onOpenFileBrowserClick() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Category Breakdown Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Storage Allocation",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${uiState.summary.scannedFiles} items",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                for (category in StorageCategory.values()) {
                    val bytes = uiState.summary.categoryBreakdown[category] ?: 0L
                    CategoryRow(category = category, bytes = bytes)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Non-blocking Toast Banner at top
            ToastBanner(
                message = uiState.exportSuccessMessage ?: uiState.errorMessage ?: "",
                visible = uiState.exportSuccessMessage != null || uiState.errorMessage != null,
                type = if (uiState.errorMessage != null) ToastType.ERROR else ToastType.SUCCESS,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun ActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FluidCard(
        modifier = modifier.height(96.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DroidPrimary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CategoryRow(
    category: StorageCategory,
    bytes: Long
) {
    FluidCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(category.color)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = FileNode.formatByteSize(bytes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
