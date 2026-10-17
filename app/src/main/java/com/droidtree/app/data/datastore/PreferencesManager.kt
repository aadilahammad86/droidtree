package com.droidtree.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.droidtree.app.core.designsystem.theme.AppColorPalette
import com.droidtree.app.core.designsystem.theme.ThemeMode
import com.droidtree.app.core.model.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "droidtree_settings")

/**
 * Persists user configuration using Jetpack DataStore Preferences.
 *
 * Implements: datastore, kotlin-flows.
 * Handles IOException corruption safely.
 */
class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val INCLUDE_HIDDEN = booleanPreferencesKey("include_hidden_files")
        val SORT_OPTION = stringPreferencesKey("sort_option")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val COLOR_PALETTE = stringPreferencesKey("color_palette")
        val AUTO_SCAN = booleanPreferencesKey("auto_scan_on_launch")
    }

    val includeHiddenFiles: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.INCLUDE_HIDDEN] ?: false
        }

    val sortOption: Flow<SortOption> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val name = preferences[PreferencesKeys.SORT_OPTION] ?: SortOption.SIZE_DESC.name
            try {
                SortOption.valueOf(name)
            } catch (e: Exception) {
                SortOption.SIZE_DESC
            }
        }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val name = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(name)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }

    val colorPalette: Flow<AppColorPalette> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val name = preferences[PreferencesKeys.COLOR_PALETTE] ?: AppColorPalette.MONET.name
            try {
                AppColorPalette.valueOf(name)
            } catch (e: Exception) {
                AppColorPalette.MONET
            }
        }

    suspend fun setIncludeHiddenFiles(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCLUDE_HIDDEN] = include
        }
    }

    suspend fun setSortOption(option: SortOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_OPTION] = option.name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setColorPalette(palette: AppColorPalette) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLOR_PALETTE] = palette.name
        }
    }
}
