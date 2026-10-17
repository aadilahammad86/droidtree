package com.droidtree.app.data.scanner

import android.os.Environment
import com.droidtree.app.core.error.DataError
import com.droidtree.app.core.model.FileNode
import com.droidtree.app.core.model.ScanState
import com.droidtree.app.core.model.StorageCategory
import com.droidtree.app.core.model.StorageSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.coroutineContext

/**
 * High-performance asynchronous storage scanner (WizTree indexing engine for Android).
 *
 * Implements: kotlin-coroutines, kotlin-flows, android-data-layer, android-source-search.
 * Features:
 * - Traverses file hierarchies bottom-up with cumulative size aggregation.
 * - Throttles UI progress emissions to preserve 120fps frame budget.
 * - Extracts largest files index in-memory.
 */
class FastStorageScanner {

    fun scanStorage(
        rootDirectory: File = Environment.getExternalStorageDirectory(),
        includeHidden: Boolean = false
    ): Flow<ScanState> = flow {
        if (!rootDirectory.exists() || !rootDirectory.canRead()) {
            emit(ScanState.Error("Cannot access root directory: ${rootDirectory.absolutePath}"))
            return@flow
        }

        var scannedFilesCount = 0
        var scannedFoldersCount = 0
        var totalBytesAccumulator = 0L
        val categoryBreakdown = mutableMapOf<StorageCategory, Long>()
        val largestFilesList = mutableListOf<FileNode>()

        var lastEmitTime = 0L

        suspend fun scanDirectory(dir: File): FileNode {
            val childrenNodes = mutableListOf<FileNode>()
            var folderBytes = 0L
            var dirFilesCount = 0
            var dirFoldersCount = 0

            val fileList = dir.listFiles() ?: emptyArray()

            for (file in fileList) {
                if (!coroutineContext.isActive) break
                if (!includeHidden && file.name.startsWith(".")) continue

                if (file.isDirectory) {
                    scannedFoldersCount++
                    dirFoldersCount++
                    val childFolder = scanDirectory(file)
                    childrenNodes.add(childFolder)
                    folderBytes += childFolder.sizeBytes
                    dirFilesCount += childFolder.fileCount
                    dirFoldersCount += childFolder.folderCount
                } else {
                    scannedFilesCount++
                    dirFilesCount++
                    val length = file.length().coerceAtLeast(0L)
                    folderBytes += length
                    totalBytesAccumulator += length

                    val category = StorageCategory.fromExtension(file.extension)
                    categoryBreakdown[category] = (categoryBreakdown[category] ?: 0L) + length

                    val leafNode = FileNode(
                        id = file.absolutePath,
                        name = file.name,
                        path = file.absolutePath,
                        sizeBytes = length,
                        isDirectory = false,
                        category = category,
                        lastModified = file.lastModified()
                    )
                    childrenNodes.add(leafNode)

                    // Track top largest files
                    if (largestFilesList.size < 50 || length > (largestFilesList.lastOrNull()?.sizeBytes ?: 0L)) {
                        largestFilesList.add(leafNode)
                        largestFilesList.sortByDescending { it.sizeBytes }
                        if (largestFilesList.size > 50) {
                            largestFilesList.removeAt(largestFilesList.lastIndex)
                        }
                    }

                    // Throttle UI update emissions (every 60ms) to maintain frame budget
                    val now = System.currentTimeMillis()
                    if (now - lastEmitTime > 60) {
                        lastEmitTime = now
                        emit(
                            ScanState.Scanning(
                                currentPath = file.parent ?: file.name,
                                filesFound = scannedFilesCount,
                                totalBytes = totalBytesAccumulator
                            )
                        )
                    }
                }
            }

            return FileNode(
                id = dir.absolutePath,
                name = if (dir == rootDirectory) "Storage" else dir.name,
                path = dir.absolutePath,
                sizeBytes = folderBytes,
                isDirectory = true,
                children = childrenNodes.sortedByDescending { it.sizeBytes },
                fileCount = dirFilesCount,
                folderCount = dirFoldersCount,
                lastModified = dir.lastModified()
            )
        }

        try {
            val rootNode = scanDirectory(rootDirectory)

            val totalStorage = rootDirectory.totalSpace.coerceAtLeast(totalBytesAccumulator)
            val freeStorage = rootDirectory.freeSpace

            val summary = StorageSummary(
                totalBytes = totalStorage,
                usedBytes = totalBytesAccumulator,
                freeBytes = freeStorage,
                scannedFiles = scannedFilesCount,
                scannedFolders = scannedFoldersCount,
                categoryBreakdown = categoryBreakdown,
                largestFiles = largestFilesList,
                rootNode = rootNode
            )

            emit(ScanState.Completed(summary))
        } catch (e: Exception) {
            emit(ScanState.Error(e.localizedMessage ?: "Unknown scanning error"))
        }
    }.flowOn(Dispatchers.IO)
}
