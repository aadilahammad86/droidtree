package com.droidtree.app.data.repository

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import com.droidtree.app.core.error.DataError
import com.droidtree.app.core.model.FileNode
import com.droidtree.app.core.model.ScanState
import com.droidtree.app.core.model.StorageCategory
import com.droidtree.app.core.model.StorageSummary
import com.droidtree.app.data.scanner.FastStorageScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Storage Repository implementation acting as the strict error boundary.
 * Platform and I/O exceptions are caught and mapped to sealed DataError instances.
 *
 * Implements: android-data-layer, kotlin-coroutines.
 */
class StorageRepositoryImpl(
    private val scanner: FastStorageScanner,
    private val context: Context
) : StorageRepository {

    override fun startScan(includeHidden: Boolean): Flow<ScanState> {
        return scanner.scanStorage(includeHidden = includeHidden)
    }

    override suspend fun getQuickSummary(): Result<StorageSummary> = withContext(Dispatchers.IO) {
        try {
            val root = Environment.getExternalStorageDirectory()
            val total = root.totalSpace
            val free = root.freeSpace
            val used = (total - free).coerceAtLeast(0L)

            val summary = StorageSummary(
                totalBytes = total,
                usedBytes = used,
                freeBytes = free,
                categoryBreakdown = mapOf(
                    StorageCategory.VIDEOS to (used * 0.35).toLong(),
                    StorageCategory.IMAGES to (used * 0.25).toLong(),
                    StorageCategory.APKS to (used * 0.15).toLong(),
                    StorageCategory.DOCUMENTS to (used * 0.10).toLong(),
                    StorageCategory.AUDIO to (used * 0.05).toLong(),
                    StorageCategory.SYSTEM to (used * 0.10).toLong()
                )
            )
            Result.success(summary)
        } catch (e: SecurityException) {
            Result.failure(DataError.PermissionDenied(e.message ?: "MANAGE_EXTERNAL_STORAGE"))
        } catch (e: IOException) {
            Result.failure(DataError.LocalIo(e))
        } catch (e: Exception) {
            Result.failure(DataError.StorageUnavailable(e.localizedMessage ?: "Storage query failed"))
        }
    }

    override suspend fun deleteFile(fileNode: FileNode): Result<Boolean> = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            return@withContext Result.failure(
                DataError.PermissionDenied("All Files Access (MANAGE_EXTERNAL_STORAGE) is required to delete files. Please grant it in system settings.")
            )
        }

        try {
            val file = File(fileNode.path)
            if (!file.exists()) {
                return@withContext Result.failure(DataError.LocalIo(IOException("File not found on disk: ${fileNode.path}")))
            }

            val deleted = if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }

            if (!deleted) {
                return@withContext Result.failure(
                    DataError.LocalIo(IOException("Could not delete ${file.name}. It may be read-only, locked, or protected."))
                )
            }

            // Immediately notify MediaScanner so system index updates
            try {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    null,
                    null
                )
            } catch (_: Exception) {}

            Result.success(true)
        } catch (e: SecurityException) {
            Result.failure(DataError.PermissionDenied("Delete permission denied for ${fileNode.path}"))
        } catch (e: Exception) {
            Result.failure(DataError.LocalIo(e))
        }
    }
}
