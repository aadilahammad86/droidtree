package com.droidtree.app.ui.dashboard

import androidx.compose.runtime.Stable
import com.droidtree.app.core.designsystem.theme.AppColorPalette
import com.droidtree.app.core.designsystem.theme.ThemeMode
import com.droidtree.app.core.model.FileNode
import com.droidtree.app.core.model.ScanState
import com.droidtree.app.core.model.StorageCategory
import com.droidtree.app.core.model.StorageSummary

/**
 * Single immutable UI state for DashboardScreen following android-dev guidelines.
 */
data class DashboardUiState(
    val scanState: ScanState = ScanState.Idle,
    val summary: StorageSummary = StorageSummary(),
    val isScanning: Boolean = false,
    val scanProgressText: String = "",
    val errorMessage: String? = null,
    val exportSuccessMessage: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorPalette: AppColorPalette = AppColorPalette.MONET,
    val isThemeSheetVisible: Boolean = false
)

/**
 * Fire-once UI side effects.
 */
sealed interface DashboardEffect {
    data class ShowToast(val message: String) : DashboardEffect
    data class NavigateToTreemap(val rootNode: FileNode) : DashboardEffect
    data class NavigateToFileBrowser(val rootPath: String) : DashboardEffect
}

/**
 * Stable actions interface decoupling Compose UI from ViewModel.
 */
@Stable
interface DashboardActions {
    fun onStartScanClick()
    fun onOpenTreemapClick()
    fun onOpenFileBrowserClick()
    fun onExportPdfReportClick()
    fun onOpenThemeSheet()
    fun onCloseThemeSheet()
    fun onSelectThemeMode(mode: ThemeMode)
    fun onSelectColorPalette(palette: AppColorPalette)
    fun onDismissToast()
}
