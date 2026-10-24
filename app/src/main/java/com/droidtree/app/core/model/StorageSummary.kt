package com.droidtree.app.core.model

/**
 * Aggregated storage information across internal & external storage partitions.
 */
data class StorageSummary(
    val totalBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val scannedFiles: Int = 0,
    val scannedFolders: Int = 0,
    val categoryBreakdown: Map<StorageCategory, Long> = emptyMap(),
    val largestFiles: List<FileNode> = emptyList(),
    val rootNode: FileNode? = null
) {
    val usedPercentage: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedTotal: String get() = FileNode.formatByteSize(totalBytes)
    val formattedUsed: String get() = FileNode.formatByteSize(usedBytes)
    val formattedFree: String get() = FileNode.formatByteSize(freeBytes)
    val formattedScannedFiles: String get() = java.text.NumberFormat.getNumberInstance().format(scannedFiles)
}

/**
 * Real-time scanning state emitted during traversal.
 */
sealed interface ScanState {
    object Idle : ScanState
    data class Scanning(
        val currentPath: String,
        val filesFound: Int,
        val totalBytes: Long
    ) : ScanState
    data class Completed(val summary: StorageSummary) : ScanState
    data class Error(val message: String) : ScanState
}

enum class SortOption(val title: String) {
    SIZE_DESC("Largest First"),
    SIZE_ASC("Smallest First"),
    NAME_ASC("Name (A to Z)"),
    DATE_DESC("Recently Modified")
}
