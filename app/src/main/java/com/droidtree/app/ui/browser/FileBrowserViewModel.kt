package com.droidtree.app.ui.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidtree.app.core.model.FileNode
import com.droidtree.app.core.model.SortOption
import com.droidtree.app.core.model.StorageCategory
import com.droidtree.app.data.datastore.PreferencesManager
import com.droidtree.app.data.repository.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ViewModel for hierarchical File Browser table.
 * Implements: android-dev, kotlin-flows, kotlin-coroutines, android-data-layer.
 */
class FileBrowserViewModel(
    private val repository: StorageRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel(), FileBrowserActions {

    private val _uiState = MutableStateFlow(FileBrowserUiState())
    val uiState: StateFlow<FileBrowserUiState> = _uiState.asStateFlow()

    private val _effects = Channel<FileBrowserEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun loadDirectory(path: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, currentPath = path) }
            val includeHidden = preferencesManager.includeHiddenFiles.first()
            val sort = preferencesManager.sortOption.first()

            withContext(Dispatchers.IO) {
                val dir = File(path)
                if (!dir.exists() || !dir.isDirectory) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Cannot open folder: $path") }
                    return@withContext
                }

                val files = dir.listFiles() ?: emptyArray()
                val list = mutableListOf<FileNode>()
                var totalBytes = 0L

                for (f in files) {
                    if (!includeHidden && f.name.startsWith(".")) continue
                    val len = if (f.isDirectory) f.walkTopDown().maxDepth(1).filter { it.isFile }.sumOf { it.length() } else f.length()
                    totalBytes += len

                    list.add(
                        FileNode(
                            id = f.absolutePath,
                            name = f.name,
                            path = f.absolutePath,
                            sizeBytes = len,
                            isDirectory = f.isDirectory,
                            category = if (f.isDirectory) StorageCategory.SYSTEM else StorageCategory.fromExtension(f.extension),
                            lastModified = f.lastModified()
                        )
                    )
                }

                val sortedList = sortList(list, sort)

                _uiState.update {
                    it.copy(
                        currentFolderName = dir.name.ifEmpty { "Root" },
                        items = sortedList,
                        totalFolderBytes = totalBytes,
                        sortOption = sort,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    private fun sortList(list: List<FileNode>, sort: SortOption): List<FileNode> {
        return when (sort) {
            SortOption.SIZE_DESC -> list.sortedWith(compareByDescending<FileNode> { it.isDirectory }.thenByDescending { it.sizeBytes })
            SortOption.SIZE_ASC -> list.sortedWith(compareByDescending<FileNode> { it.isDirectory }.thenBy { it.sizeBytes })
            SortOption.NAME_ASC -> list.sortedWith(compareByDescending<FileNode> { it.isDirectory }.thenBy { it.name.lowercase() })
            SortOption.DATE_DESC -> list.sortedWith(compareByDescending<FileNode> { it.isDirectory }.thenByDescending { it.lastModified })
        }
    }

    override fun onItemClick(node: FileNode) {
        if (node.isDirectory) {
            loadDirectory(node.path)
        } else {
            _uiState.update {
                it.copy(
                    selectedItem = node,
                    isDetailsModalVisible = true
                )
            }
        }
    }

    override fun onRequestDelete(node: FileNode) {
        _uiState.update {
            it.copy(
                itemPendingDeletion = node,
                isDeleteConfirmationVisible = true
            )
        }
    }

    override fun onConfirmDelete() {
        val node = _uiState.value.itemPendingDeletion ?: return
        viewModelScope.launch {
            repository.deleteFile(node)
                .onSuccess {
                    val updatedItems = _uiState.value.items.filter { it.id != node.id }
                    val updatedTotal = (_uiState.value.totalFolderBytes - node.sizeBytes).coerceAtLeast(0L)

                    _uiState.update {
                        it.copy(
                            items = updatedItems,
                            totalFolderBytes = updatedTotal,
                            isDeleteConfirmationVisible = false,
                            isDetailsModalVisible = false,
                            itemPendingDeletion = null,
                            selectedItem = null
                        )
                    }
                    _effects.trySend(FileBrowserEffect.ShowToast("Deleted ${node.name}"))
                }
                .onFailure { error ->
                    _effects.trySend(FileBrowserEffect.ShowToast("Delete failed: ${error.localizedMessage}"))
                    _uiState.update {
                        it.copy(
                            isDeleteConfirmationVisible = false,
                            itemPendingDeletion = null
                        )
                    }
                }
        }
    }

    override fun onDismissModals() {
        _uiState.update {
            it.copy(
                isDetailsModalVisible = false,
                isDeleteConfirmationVisible = false,
                selectedItem = null,
                itemPendingDeletion = null
            )
        }
    }

    override fun onSortChange(sort: SortOption) {
        viewModelScope.launch {
            preferencesManager.setSortOption(sort)
            _uiState.update {
                it.copy(
                    sortOption = sort,
                    items = sortList(it.items, sort)
                )
            }
        }
    }

    override fun onNavigateUpClick() {
        val current = File(_uiState.value.currentPath)
        val parent = current.parentFile
        if (parent != null && parent.canRead()) {
            loadDirectory(parent.absolutePath)
        } else {
            _effects.trySend(FileBrowserEffect.NavigateBack)
        }
    }

    override fun onShowToast(text: String) {
        _effects.trySend(FileBrowserEffect.ShowToast(text))
    }
}
