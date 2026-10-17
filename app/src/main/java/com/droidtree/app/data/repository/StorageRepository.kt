package com.droidtree.app.data.repository

import com.droidtree.app.core.error.DataError
import com.droidtree.app.core.model.FileNode
import com.droidtree.app.core.model.ScanState
import com.droidtree.app.core.model.StorageSummary
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Storage Repository interface defining the domain boundary.
 * Implements: android-data-layer, kmp-boundaries.
 */
interface StorageRepository {
    fun startScan(includeHidden: Boolean): Flow<ScanState>
    suspend fun getQuickSummary(): Result<StorageSummary>
    suspend fun deleteFile(fileNode: FileNode): Result<Boolean>
}
