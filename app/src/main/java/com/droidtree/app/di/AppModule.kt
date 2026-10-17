package com.droidtree.app.di

import com.droidtree.app.data.datastore.PreferencesManager
import com.droidtree.app.data.pdf.StorageReportPdfExporter
import com.droidtree.app.data.remote.ExportApiService
import com.droidtree.app.data.repository.StorageRepository
import com.droidtree.app.data.repository.StorageRepositoryImpl
import com.droidtree.app.data.scanner.FastStorageScanner
import com.droidtree.app.ui.browser.FileBrowserViewModel
import com.droidtree.app.ui.dashboard.DashboardViewModel
import com.droidtree.app.ui.treemap.TreemapViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Dependency injection module for DroidTree using Koin Classic DSL.
 * Implements: koin, android-dev.
 */
val appModule = module {
    single { FastStorageScanner() }
    single<StorageRepository> { StorageRepositoryImpl(get(), androidContext()) }
    single { PreferencesManager(androidContext()) }
    single { StorageReportPdfExporter() }
    single { ExportApiService.create() }

    viewModel { DashboardViewModel(get(), get(), get()) }
    viewModel { TreemapViewModel(get()) }
    viewModel { FileBrowserViewModel(get(), get()) }
}
