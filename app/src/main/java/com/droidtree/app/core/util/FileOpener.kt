package com.droidtree.app.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

/**
 * Utility to open files safely using FileProvider and Android's Intent.ACTION_VIEW.
 */
object FileOpener {

    fun openFile(context: Context, filePath: String): Result<Unit> {
        val file = File(filePath)
        if (!file.exists()) {
            return Result.failure(IllegalStateException("File does not exist: $filePath"))
        }

        if (file.isDirectory) {
            return Result.failure(IllegalArgumentException("Cannot open directory as file"))
        }

        val extension = file.extension.lowercase()
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"

        val uri: Uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Uri.fromFile(file)
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            val chooser = Intent.createChooser(intent, "Open file with...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Result.success(Unit)
        } catch (e: ActivityNotFoundException) {
            Result.failure(Exception("No installed app found to open this file type"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
