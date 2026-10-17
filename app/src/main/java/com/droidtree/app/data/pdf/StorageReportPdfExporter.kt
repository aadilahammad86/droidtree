package com.droidtree.app.data.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.droidtree.app.core.error.DataError
import com.droidtree.app.core.model.StorageSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates an exportable PDF disk analysis audit report using Android's platform PDF APIs.
 *
 * Implements: pdf-annotations, android-data-layer.
 */
class StorageReportPdfExporter {

    suspend fun generateReport(summary: StorageSummary, outputFile: File): Result<File> = withContext(Dispatchers.IO) {
        val document = PdfDocument()

        try {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 portrait
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }

            // Header Background Banner
            paint.color = Color.parseColor("#181B22")
            canvas.drawRect(0f, 0f, 595f, 100f, paint)

            // Title
            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 24f
            canvas.drawText("DroidTree — Storage Audit Report", 40f, 50f, paint)

            // Date
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 12f
            paint.color = Color.parseColor("#94A3B8")
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            canvas.drawText("Generated on: $dateStr", 40f, 75f, paint)

            // Key Metrics Card
            paint.color = Color.parseColor("#F1F5F9")
            canvas.drawRoundRect(40f, 130f, 555f, 220f, 12f, 12f, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 14f
            canvas.drawText("Storage Overview", 60f, 160f, paint)

            paint.typeface = Typeface.DEFAULT
            paint.textSize = 12f
            paint.color = Color.parseColor("#334155")
            canvas.drawText("Total Storage: ${summary.formattedTotal}", 60f, 185f, paint)
            canvas.drawText("Used Storage: ${summary.formattedUsed} (${String.format(Locale.US, "%.1f", summary.usedPercentage * 100)}%)", 60f, 205f, paint)
            canvas.drawText("Free Storage: ${summary.formattedFree}", 300f, 185f, paint)
            canvas.drawText("Files Scanned: ${summary.scannedFiles} (${summary.scannedFolders} folders)", 300f, 205f, paint)

            // Category Breakdown Table
            var yOffset = 260f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 14f
            paint.color = Color.parseColor("#0F172A")
            canvas.drawText("Category Breakdown", 40f, yOffset, paint)

            yOffset += 25f
            paint.textSize = 11f
            paint.typeface = Typeface.DEFAULT
            for ((category, size) in summary.categoryBreakdown) {
                paint.color = Color.parseColor("#475569")
                canvas.drawText(category.displayName, 40f, yOffset, paint)
                canvas.drawText(com.droidtree.app.core.model.FileNode.formatByteSize(size), 260f, yOffset, paint)
                yOffset += 20f
            }

            // Top Largest Files
            yOffset += 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 14f
            paint.color = Color.parseColor("#0F172A")
            canvas.drawText("Top Largest Files", 40f, yOffset, paint)

            yOffset += 25f
            paint.textSize = 10f
            paint.typeface = Typeface.DEFAULT
            for (file in summary.largestFiles.take(15)) {
                paint.color = Color.parseColor("#1E293B")
                val truncatedName = if (file.name.length > 45) file.name.take(42) + "..." else file.name
                canvas.drawText(truncatedName, 40f, yOffset, paint)
                canvas.drawText(file.formattedSize, 450f, yOffset, paint)
                yOffset += 18f
            }

            document.finishPage(page)

            FileOutputStream(outputFile).use { out ->
                document.writeTo(out)
            }

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(DataError.ExportError("Failed to write PDF storage report", e))
        } finally {
            document.close()
        }
    }
}
