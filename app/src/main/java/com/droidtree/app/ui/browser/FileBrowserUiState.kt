package com.droidtree.app.ui.browser

import androidx.compose.runtime.Stable
import com.droidtree.app.core.model.FileNode
import com.droidtree.app.core.model.SortOption

data class FileBrowserUiState(
    val currentPath: String = "",
    val currentFolderName: String = "",
    val items: List<FileNode> = emptyList(),
    val totalFolderBytes: Long = 0L,
    val sortOption: SortOption = SortOption.SIZE_DESC,
    val selectedItem: FileNode? = null,
    val itemPendingDeletion: FileNode? = null,
    val isDetailsModalVisible: Boolean = false,
    val isDeleteConfirmationVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface FileBrowserEffect {
    data class ShowToast(val text: String) : FileBrowserEffect
    object NavigateBack : FileBrowserEffect
}

@Stable
interface FileBrowserActions {
    fun onItemClick(node: FileNode)
    fun onRequestDelete(node: FileNode)
    fun onConfirmDelete()
    fun onDismissModals()
    fun onSortChange(sort: SortOption)
    fun onNavigateUpClick()
    fun onShowToast(text: String)
}
