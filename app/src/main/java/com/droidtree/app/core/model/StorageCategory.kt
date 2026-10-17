package com.droidtree.app.core.model

import androidx.compose.ui.graphics.Color

/**
 * High-level categorization of files for storage visualization.
 */
enum class StorageCategory(val displayName: String, val color: Color) {
    VIDEOS("Videos", Color(0xFFEF5350)),
    IMAGES("Images", Color(0xFF42A5F5)),
    AUDIO("Audio", Color(0xFFFFB74D)),
    DOCUMENTS("Documents", Color(0xFF66BB6A)),
    ARCHIVES("Archives", Color(0xFFAB47BC)),
    APKS("APKs & Installers", Color(0xFF26A69A)),
    CODE("Code & Text", Color(0xFFFFA726)),
    SYSTEM("System & Other", Color(0xFF78909C));

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
