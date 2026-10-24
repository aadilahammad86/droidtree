package com.droidtree.app.data.repository

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.droidtree.app.core.error.DataError
import com.droidtree.app.core.model.FileNode
import com.droidtree.app.core.model.ScanState
import com.droidtree.app.core.model.StorageCategory
import com.droidtree.app.core.model.StorageSummary
import com.droidtree.app.data.datastore.PreferencesManager
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
    private val context: Context,
    private val preferencesManager: PreferencesManager
) : StorageRepository {

    override fun startScan(includeHidden: Boolean): Flow<ScanState> {
        return scanner.scanStorage(includeHidden = includeHidden)
    }

    override suspend fun getQuickSummary(): Result<StorageSummary> = withContext(Dispatchers.IO) {
        try {
            // 1. Try to load previously cached full disk scan
            val cached = preferencesManager.getCachedSummary()
            if (cached != null && cached.scannedFiles > 0) {
                return@withContext Result.success(cached)
            }

            val root = Environment.getExternalStorageDirectory()
            val total = root.totalSpace
            val free = root.freeSpace

            // 2. Query MediaStore for fast real file count & categorized storage
            val mediaSummary = queryMediaStoreSummary(rootTotal = total, rootFree = free)
            if (mediaSummary != null && mediaSummary.scannedFiles > 0) {
                preferencesManager.saveCachedSummary(mediaSummary)
                return@withContext Result.success(mediaSummary)
            }

            // 3. Fallback when MediaStore returned no files or permissions not yet granted
            val used = (total - free).coerceAtLeast(0L)
            val summary = StorageSummary(
                totalBytes = total,
                usedBytes = used,
                freeBytes = free,
                scannedFiles = 0,
                categoryBreakdown = if (used > 0) mapOf(StorageCategory.SYSTEM to used) else emptyMap()
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

    private fun queryMediaStoreSummary(rootTotal: Long, rootFree: Long): StorageSummary? {
        return try {
            val resolver = context.contentResolver
            val filesUri = MediaStore.Files.getContentUri("external")
            val projection = arrayOf(
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.MediaColumns.DISPLAY_NAME
            )

            val selection = "${MediaStore.Files.FileColumns.SIZE} > 0"
            var totalFiles = 0
            var totalBytes = 0L
            val categoryMap = mutableMapOf<StorageCategory, Long>()

            val cursor = resolver.query(filesUri, projection, selection, null, null) ?: return null
            cursor.use {
                val sizeCol = it.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val typeCol = it.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)
                val nameCol = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)

                if (sizeCol < 0) return null

                while (it.moveToNext()) {
                    val size = it.getLong(sizeCol)
                    if (size <= 0) continue
                    totalFiles++
                    totalBytes += size

                    val mediaType = if (typeCol >= 0) it.getInt(typeCol) else MediaStore.Files.FileColumns.MEDIA_TYPE_NONE
                    val category = when (mediaType) {
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> StorageCategory.IMAGES
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> StorageCategory.VIDEOS
                        MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO -> StorageCategory.AUDIO
                        MediaStore.Files.FileColumns.MEDIA_TYPE_DOCUMENT -> StorageCategory.DOCUMENTS
                        else -> {
                            val name = if (nameCol >= 0) it.getString(nameCol) else null
                            val ext = name?.substringAfterLast('.', "") ?: ""
                            StorageCategory.fromExtension(ext)
                        }
                    }
                    categoryMap[category] = (categoryMap[category] ?: 0L) + size
                }
            }

            if (totalFiles == 0) return null

            StorageSummary(
                totalBytes = rootTotal.coerceAtLeast(totalBytes),
                usedBytes = totalBytes,
                freeBytes = rootFree,
                scannedFiles = totalFiles,
                scannedFolders = 0,
                categoryBreakdown = categoryMap
            )
        } catch (_: Exception) {
            null
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
