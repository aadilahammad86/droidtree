package com.droidtree.app.ui.treemap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidtree.app.core.model.FileNode
import com.droidtree.app.data.repository.StorageRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel handling Treemap drill-down, breadcrumb hierarchy, and inspection actions.
 * Implements: android-dev, compose, android-data-layer.
 */
class TreemapViewModel(
    private val repository: StorageRepository
) : ViewModel(), TreemapActions {

    private val _uiState = MutableStateFlow(TreemapUiState())
    val uiState: StateFlow<TreemapUiState> = _uiState.asStateFlow()

    private val _effects = Channel<TreemapEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun setInitialRoot(root: FileNode) {
        if (_uiState.value.currentRoot == null) {
            _uiState.update {
                it.copy(
                    currentRoot = root,
                    breadcrumbTrail = listOf(root)
                )
            }
        }
    }

    override fun onTileClick(node: FileNode) {
        if (node.isDirectory && node.children.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    currentRoot = node,
                    breadcrumbTrail = it.breadcrumbTrail + node,
                    selectedNode = null,
                    isBottomSheetVisible = false
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    selectedNode = node,
                    isBottomSheetVisible = true
                )
            }
        }
    }

    override fun onFolderClick(folderNode: FileNode) {
        if (folderNode.isDirectory) {
            _uiState.update {
                it.copy(
                    currentRoot = folderNode,
                    breadcrumbTrail = it.breadcrumbTrail + folderNode,
                    selectedNode = null,
                    isBottomSheetVisible = false
                )
            }
        }
    }

    override fun onBreadcrumbClick(node: FileNode) {
        val currentTrail = _uiState.value.breadcrumbTrail
        val index = currentTrail.indexOf(node)
        if (index >= 0) {
            _uiState.update {
                it.copy(
                    currentRoot = node,
                    breadcrumbTrail = currentTrail.take(index + 1),
                    selectedNode = null,
                    isBottomSheetVisible = false
                )
            }
        }
    }

    override fun onNavigateUpClick() {
        val currentTrail = _uiState.value.breadcrumbTrail
        if (currentTrail.size > 1) {
            val parent = currentTrail[currentTrail.size - 2]
            _uiState.update {
                it.copy(
                    currentRoot = parent,
                    breadcrumbTrail = currentTrail.dropLast(1),
                    selectedNode = null,
                    isBottomSheetVisible = false
                )
            }
        } else {
            _effects.trySend(TreemapEffect.NavigateBack)
        }
    }

    override fun onDismissDetails() {
        _uiState.update { it.copy(isBottomSheetVisible = false, selectedNode = null) }
    }

    override fun onRequestDeleteNode(node: FileNode) {
        _uiState.update {
            it.copy(
                nodePendingDeletion = node,
                isDeleteConfirmationVisible = true
            )
        }
    }

    override fun onConfirmDeleteNode() {
        val node = _uiState.value.nodePendingDeletion ?: return
        viewModelScope.launch {
            repository.deleteFile(node)
                .onSuccess {
                    val root = _uiState.value.currentRoot
                    if (root != null) {
                        val updatedChildren = root.children.filter { it.id != node.id }
                        val updatedRoot = root.copy(
                            children = updatedChildren,
                            sizeBytes = (root.sizeBytes - node.sizeBytes).coerceAtLeast(0L),
                            fileCount = if (node.isDirectory) (root.fileCount - node.fileCount).coerceAtLeast(0) else (root.fileCount - 1).coerceAtLeast(0)
                        )

                        _uiState.update { state ->
                            state.copy(
                                currentRoot = updatedRoot,
                                selectedNode = null,
                                nodePendingDeletion = null,
                                isBottomSheetVisible = false,
                                isDeleteConfirmationVisible = false,
                                message = "Deleted ${node.name}",
                                breadcrumbTrail = state.breadcrumbTrail.map { crumb ->
                                    if (crumb.id == root.id) updatedRoot else crumb
                                }
                            )
                        }
                    }
                    _effects.trySend(TreemapEffect.ShowToast("Deleted ${node.name}"))
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            message = error.localizedMessage,
                            isDeleteConfirmationVisible = false,
                            nodePendingDeletion = null
                        )
                    }
                    _effects.trySend(TreemapEffect.ShowToast("Error: ${error.localizedMessage}"))
                }
        }
    }

    override fun onDismissConfirmation() {
        _uiState.update {
            it.copy(
                isDeleteConfirmationVisible = false,
                nodePendingDeletion = null
            )
        }
    }

    override fun onDismissToast() {
        _uiState.update { it.copy(message = null) }
    }

    override fun onShowToast(text: String) {
        _uiState.update { it.copy(message = text) }
        _effects.trySend(TreemapEffect.ShowToast(text))
    }
}
