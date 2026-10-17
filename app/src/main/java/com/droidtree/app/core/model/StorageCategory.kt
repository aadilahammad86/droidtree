package com.droidtree.app.core.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * High-level categorization of files for storage visualization.
 */
enum class StorageCategory(
    val displayName: String,
    val color: Color,
    val icon: ImageVector
) {
    VIDEOS("Videos", Color(0xFFEF5350), Icons.Rounded.Videocam),
    IMAGES("Images", Color(0xFF42A5F5), Icons.Rounded.Image),
    AUDIO("Audio", Color(0xFFFFB74D), Icons.Rounded.Audiotrack),
    DOCUMENTS("Documents", Color(0xFF66BB6A), Icons.Rounded.Description),
    ARCHIVES("Archives", Color(0xFFAB47BC), Icons.Rounded.FolderZip),
    APKS("APKs & Installers", Color(0xFF26A69A), Icons.Rounded.Android),
    CODE("Code & Text", Color(0xFFFFA726), Icons.Rounded.Code),
    SYSTEM("System & Other", Color(0xFF78909C), Icons.Rounded.Memory);

    companion object {
        fun fromExtension(ext: String): StorageCategory {
            return when (ext.lowercase()) {
                "mp4", "mkv", "mov", "avi", "webm", "3gp", "flv" -> VIDEOS
                "jpg", "jpeg", "png", "gif", "webp", "heic", "svg", "bmp" -> IMAGES
                "mp3", "wav", "flac", "m4a", "aac", "ogg", "opus" -> AUDIO
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "epub" -> DOCUMENTS
                "zip", "rar", "7z", "tar", "gz", "bz2", "xz" -> ARCHIVES
                "apk", "xapk", "apks", "aab" -> APKS
                "kt", "java", "json", "xml", "html", "js", "ts", "py", "c", "cpp" -> CODE
                else -> SYSTEM
            }
        }
    }
}
