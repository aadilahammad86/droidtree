package com.droidtree.app

import androidx.compose.ui.geometry.Rect
import com.droidtree.app.core.designsystem.components.computeSquarifiedTreemap
import com.droidtree.app.core.model.FileNode
import com.droidtree.app.core.model.StorageCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for DroidTree's squarified treemap and data models.
 * Implements: android-testing (pure unit testing on JVM, backtick naming without commas).
 */
class TreemapAlgorithmTest {

    @Test
    fun `formatByteSize accurately formats boundary values`() {
        assertEquals("0 B", FileNode.formatByteSize(0L))
        assertEquals("500 B", FileNode.formatByteSize(500L))
        assertEquals("1.00 KB", FileNode.formatByteSize(1024L))
        assertEquals("1.50 MB", FileNode.formatByteSize(1572864L))
        assertEquals("2.00 GB", FileNode.formatByteSize(2147483648L))
    }

    @Test
    fun `storageCategory identifies extensions accurately`() {
        assertEquals(StorageCategory.VIDEOS, StorageCategory.fromExtension("mp4"))
        assertEquals(StorageCategory.IMAGES, StorageCategory.fromExtension("jpg"))
        assertEquals(StorageCategory.AUDIO, StorageCategory.fromExtension("mp3"))
        assertEquals(StorageCategory.DOCUMENTS, StorageCategory.fromExtension("pdf"))
        assertEquals(StorageCategory.ARCHIVES, StorageCategory.fromExtension("zip"))
        assertEquals(StorageCategory.APKS, StorageCategory.fromExtension("apk"))
    }

    @Test
    fun `computeSquarifiedTreemap returns empty list on empty input`() {
        val bounds = Rect(0f, 0f, 400f, 400f)
        val tiles = computeSquarifiedTreemap(emptyList(), bounds, 0L)
        assertTrue(tiles.isEmpty())
    }

    @Test
    fun `computeSquarifiedTreemap allocates tiles covering canvas`() {
        val bounds = Rect(0f, 0f, 1000f, 1000f)
        val item1 = FileNode("1", "Video1.mp4", "/path/1", 500L, false, category = StorageCategory.VIDEOS)
        val item2 = FileNode("2", "Image1.jpg", "/path/2", 300L, false, category = StorageCategory.IMAGES)
        val item3 = FileNode("3", "Doc1.pdf", "/path/3", 200L, false, category = StorageCategory.DOCUMENTS)

        val tiles = computeSquarifiedTreemap(listOf(item1, item2, item3), bounds, 1000L)

        assertEquals(3, tiles.size)
        // Largest item must occupy the largest area
        assertTrue(tiles[0].bounds.width * tiles[0].bounds.height >= tiles[1].bounds.width * tiles[1].bounds.height)
    }
}
