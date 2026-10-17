package com.droidtree.app.ui.treemap

import androidx.compose.runtime.Stable
import com.droidtree.app.core.model.FileNode

data class TreemapUiState(
    val currentRoot: FileNode? = null,
    val breadcrumbTrail: List<FileNode> = emptyList(),
    val selectedNode: FileNode? = null,
    val nodePendingDeletion: FileNode? = null,
    val isBottomSheetVisible: Boolean = false,
    val isDeleteConfirmationVisible: Boolean = false,
    val message: String? = null
)

sealed interface TreemapEffect {
    data class ShowToast(val text: String) : TreemapEffect
    object NavigateBack : TreemapEffect
}

@Stable
interface TreemapActions {
    fun onTileClick(node: FileNode)
    fun onFolderClick(folderNode: FileNode)
    fun onBreadcrumbClick(node: FileNode)
    fun onNavigateUpClick()
    fun onDismissDetails()
    fun onRequestDeleteNode(node: FileNode)
    fun onConfirmDeleteNode()
    fun onDismissConfirmation()
    fun onDismissToast()
    fun onShowToast(text: String)
}
