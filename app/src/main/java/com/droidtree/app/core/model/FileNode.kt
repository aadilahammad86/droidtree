package com.droidtree.app.core.model

import java.util.Locale

/**
 * Immutable tree node representation for DroidTree's WizTree-like storage tree.
 * Optimized for rapid squarified treemap rendering and hierarchical drill-down.
 */
data class FileNode(
    val id: String,
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val isDirectory: Boolean,
    val children: List<FileNode> = emptyList(),
    val category: StorageCategory = StorageCategory.SYSTEM,
    val lastModified: Long = 0L,
    val fileCount: Int = if (isDirectory) 0 else 1,
    val folderCount: Int = if (isDirectory) 1 else 0
) {
    val formattedSize: String
        get() = formatByteSize(sizeBytes)

    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast('.', "")

    companion object {
        fun formatByteSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
            val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
            return if (digitGroups == 0) {
                String.format(Locale.US, "%d B", bytes)
            } else {
                String.format(Locale.US, "%.2f %s", value, units[digitGroups])
            }
        }
    }
}
