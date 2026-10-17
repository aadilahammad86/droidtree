package com.droidtree.app.ui.treemap

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import com.droidtree.app.core.util.FileOpener
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.droidtree.app.core.designsystem.components.FluidCard
import com.droidtree.app.core.designsystem.components.ToastBanner
import com.droidtree.app.core.designsystem.components.ToastType
import com.droidtree.app.core.designsystem.components.TreemapCanvas
import com.droidtree.app.core.designsystem.motion.fluidPress
import com.droidtree.app.core.designsystem.theme.DroidError
import com.droidtree.app.core.model.FileNode
import org.koin.androidx.compose.koinViewModel

@Composable
fun TreemapScreen(
    rootNode: FileNode,
    onNavigateBack: () -> Unit,
    viewModel: TreemapViewModel = koinViewModel()
) {
    LaunchedEffect(rootNode) {
        viewModel.setInitialRoot(rootNode)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is TreemapEffect.NavigateBack -> onNavigateBack()
                    is TreemapEffect.ShowToast -> {
                        android.widget.Toast.makeText(context, effect.text, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    TreemapContent(
        uiState = uiState,
        actions = viewModel,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreemapContent(
    uiState: TreemapUiState,
    actions: TreemapActions,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { actions.onNavigateUpClick() },
                        modifier = Modifier.fluidPress()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = uiState.currentRoot?.name ?: "Treemap Explorer",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = uiState.currentRoot?.formattedSize ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Breadcrumb Navigation Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for ((index, node) in uiState.breadcrumbTrail.withIndex()) {
                        Text(
                            text = node.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (node == uiState.currentRoot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fluidPress { actions.onBreadcrumbClick(node) }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                        if (index < uiState.breadcrumbTrail.size - 1) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
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
            val root = uiState.currentRoot
            if (root != null && root.children.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    // UPPER SECTION: Interactive Treemap Blocks (~42% height)
                    FluidCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.42f)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        TreemapCanvas(
                            rootNode = root,
                            modifier = Modifier.fillMaxSize(),
                            onNodeClick = { actions.onTileClick(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // LOWER SECTION: Neatly organized separate scrollable box showing folders & files
                    FluidCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            // Section header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Contents (${root.children.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = root.formattedSize,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Scrollable list of files and folders
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(
                                    items = root.children,
                                    key = { it.path }
                                ) { childNode ->
                                    val ratio = if (root.sizeBytes > 0) {
                                        childNode.sizeBytes.toFloat() / root.sizeBytes.toFloat()
                                    } else 0f

                                    TreemapContentRow(
                                        node = childNode,
                                        ratio = ratio,
                                        onRowClick = {
                                            if (childNode.isDirectory) {
                                                actions.onFolderClick(childNode)
                                            } else {
                                                actions.onTileClick(childNode)
                                            }
                                        },
                                        onDeleteClick = {
                                            actions.onRequestDeleteNode(childNode)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Folder is empty",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Non-blocking Toast confirmation banner
            if (uiState.message != null) {
                ToastBanner(
                    message = uiState.message ?: "",
                    visible = true,
                    type = ToastType.INFO,
                    modifier = Modifier.align(Alignment.TopCenter),
                    onDismiss = { actions.onDismissToast() }
                )
            }

            // File Details Modal Bottom Sheet
            if (uiState.isBottomSheetVisible && uiState.selectedNode != null) {
                val node = uiState.selectedNode
                ModalBottomSheet(
                    onDismissRequest = { actions.onDismissDetails() },
                    sheetState = bottomSheetState,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = node.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { actions.onDismissDetails() }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        FluidCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                DetailRow("Size", node.formattedSize)
                                DetailRow("Category", node.category.displayName)
                                DetailRow("Path", node.path)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (node.isDirectory) {
                            Button(
                                onClick = {
                                    actions.onDismissDetails()
                                    actions.onFolderClick(node)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .fluidPress()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Folder", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        } else {
                            Button(
                                onClick = {
                                    actions.onDismissDetails()
                                    val result = FileOpener.openFile(context, node.path)
                                    if (result.isFailure) {
                                        actions.onShowToast(result.exceptionOrNull()?.message ?: "Failed to open file")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .fluidPress()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open File", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { actions.onDismissDetails() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .fluidPress(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                            }

                            Button(
                                onClick = { actions.onRequestDeleteNode(node) },
                                colors = ButtonDefaults.buttonColors(containerColor = DroidError),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .fluidPress()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delete", color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // DELETION CONFIRMATION DIALOG MODAL
            if (uiState.isDeleteConfirmationVisible && uiState.nodePendingDeletion != null) {
                val node = uiState.nodePendingDeletion
                AlertDialog(
                    onDismissRequest = { actions.onDismissConfirmation() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Warning",
                            tint = DroidError,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            text = if (node.isDirectory) "Delete Folder?" else "Delete File?",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Are you sure you want to permanently delete \"${node.name}\"?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FluidCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Size: ${node.formattedSize}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = node.path,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This action cannot be undone.",
                                style = MaterialTheme.typography.labelSmall,
                                color = DroidError
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { actions.onConfirmDeleteNode() },
                            colors = ButtonDefaults.buttonColors(containerColor = DroidError),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fluidPress()
                        ) {
                            Text("Delete Permanently", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { actions.onDismissConfirmation() },
                            modifier = Modifier.fluidPress()
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Neatly organized row inside the separate scrollable contents box.
 * Implements: apple-design, emil-design-eng, android-ux.
 */
@Composable
fun TreemapContentRow(
    node: FileNode,
    ratio: Float,
    onRowClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    FluidCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        onClick = onRowClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(node.category.color.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (node.isDirectory) Icons.Default.Folder else node.category.icon,
                            contentDescription = null,
                            tint = node.category.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = node.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "${node.formattedSize} • ${if (node.isDirectory) "${node.children.size} items" else node.category.displayName} • ${(ratio * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(36.dp)
                            .fluidPress()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (node.isDirectory) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Mini visual percentage bar
            LinearProgressIndicator(
                progress = { ratio.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp)),
                color = node.category.color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
