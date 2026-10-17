package com.droidtree.app.ui.dashboard

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidtree.app.core.model.ScanState
import com.droidtree.app.core.model.StorageSummary
import com.droidtree.app.data.datastore.PreferencesManager
import com.droidtree.app.data.pdf.StorageReportPdfExporter
import com.droidtree.app.data.repository.StorageRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel for DashboardScreen.
 * Implements: android-dev, kotlin-flows, kotlin-coroutines.
 */
class DashboardViewModel(
    private val repository: StorageRepository,
    private val preferencesManager: PreferencesManager,
    private val pdfExporter: StorageReportPdfExporter
) : ViewModel(), DashboardActions {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _effects = Channel<DashboardEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadQuickSummary()
    }

    private fun loadQuickSummary() {
        viewModelScope.launch {
            repository.getQuickSummary()
                .onSuccess { summary ->
                    _uiState.update { it.copy(summary = summary) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.localizedMessage) }
                }
        }
    }

    override fun onStartScanClick() {
        viewModelScope.launch {
            val includeHidden = preferencesManager.includeHiddenFiles.first()
            _uiState.update { it.copy(isScanning = true, errorMessage = null) }

            repository.startScan(includeHidden).collect { state ->
                when (state) {
                    is ScanState.Scanning -> {
                        _uiState.update {
                            it.copy(
                                scanState = state,
                                scanProgressText = "Scanned ${state.filesFound} files..."
                            )
                        }
                    }
                    is ScanState.Completed -> {
                        _uiState.update {
                            it.copy(
                                isScanning = false,
                                scanState = state,
                                summary = state.summary,
                                scanProgressText = "Scan Complete"
                            )
                        }
                        _effects.trySend(DashboardEffect.ShowToast("Storage index built: ${state.summary.scannedFiles} files"))
                    }
                    is ScanState.Error -> {
                        _uiState.update {
                            it.copy(isScanning = false, scanState = state, errorMessage = state.message)
                        }
                        _effects.trySend(DashboardEffect.ShowToast("Scan error: ${state.message}"))
                    }
                    ScanState.Idle -> Unit
                }
            }
        }
    }

    override fun onOpenTreemapClick() {
        val rootNode = _uiState.value.summary.rootNode
        if (rootNode != null) {
            _effects.trySend(DashboardEffect.NavigateToTreemap(rootNode))
        } else {
            _effects.trySend(DashboardEffect.ShowToast("Please scan storage first to build treemap"))
        }
    }

    override fun onOpenFileBrowserClick() {
        _effects.trySend(DashboardEffect.NavigateToFileBrowser(Environment.getExternalStorageDirectory().absolutePath))
    }

    override fun onExportPdfReportClick() {
        viewModelScope.launch {
            val summary = _uiState.value.summary
            val exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(exportDir, "DroidTree_Report_${System.currentTimeMillis()}.pdf")

            pdfExporter.generateReport(summary, file)
                .onSuccess { generatedFile ->
                    _uiState.update { it.copy(exportSuccessMessage = "Report saved to: ${generatedFile.name}") }
                    _effects.trySend(DashboardEffect.ShowToast("PDF exported to Downloads/${generatedFile.name}"))
                }
                .onFailure { error ->
                    _effects.trySend(DashboardEffect.ShowToast("Failed exporting PDF: ${error.localizedMessage}"))
                }
        }
    }

    override fun onDismissToast() {
        _uiState.update { it.copy(exportSuccessMessage = null, errorMessage = null) }
    }
}
