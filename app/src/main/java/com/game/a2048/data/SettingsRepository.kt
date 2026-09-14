package com.game.a2048.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.game.a2048.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    constructor(context: Context) : this(context.settingsDataStore)

    private object PreferenceKeys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_AD_FREE = booleanPreferencesKey("is_ad_free")
        val HAMMER_USES = intPreferencesKey("hammer_uses")
        val SWITCH_USES = intPreferencesKey("switch_uses")
        val UNDO_USES = intPreferencesKey("undo_uses")
    }

    val userSettingsFlow: Flow<UserSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserSettings(
                soundEnabled = preferences[PreferenceKeys.SOUND_ENABLED] ?: true,
                hapticsEnabled = preferences[PreferenceKeys.HAPTICS_ENABLED] ?: true,
                themeMode = preferences[PreferenceKeys.THEME_MODE] ?: "DEFAULT",
                isAdFree = preferences[PreferenceKeys.IS_AD_FREE] ?: false,
                hammerUses = preferences[PreferenceKeys.HAMMER_USES] ?: 2,
                switchUses = preferences[PreferenceKeys.SWITCH_USES] ?: 2,
                undoUses = preferences[PreferenceKeys.UNDO_USES] ?: 2
            )
        }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_MODE] = mode
        }
    }

    suspend fun setAdFree(isAdFree: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.IS_AD_FREE] = isAdFree
        }
    }

    suspend fun addHammerUses(amount: Int = 5) {
        dataStore.edit { preferences ->
            val current = preferences[PreferenceKeys.HAMMER_USES] ?: 2
            preferences[PreferenceKeys.HAMMER_USES] = current + amount
        }
    }

    suspend fun addSwitchUses(amount: Int = 5) {
        dataStore.edit { preferences ->
            val current = preferences[PreferenceKeys.SWITCH_USES] ?: 2
            preferences[PreferenceKeys.SWITCH_USES] = current + amount
        }
    }

    suspend fun addUndoUses(amount: Int = 5) {
        dataStore.edit { preferences ->
            val current = preferences[PreferenceKeys.UNDO_USES] ?: 2
            preferences[PreferenceKeys.UNDO_USES] = current + amount
        }
    }

    suspend fun setHammerUses(count: Int) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.HAMMER_USES] = count
        }
    }

    suspend fun setSwitchUses(count: Int) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.SWITCH_USES] = count
        }
    }

    suspend fun setUndoUses(count: Int) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.UNDO_USES] = count
        }
    }
}
