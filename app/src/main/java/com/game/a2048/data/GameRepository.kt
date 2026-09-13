package com.game.a2048.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.game.a2048.data.model.SavedGameState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore(name = "game_data")

class GameRepository(
    private val dataStore: DataStore<Preferences>
) {
    constructor(context: Context) : this(context.gameDataStore)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun bestScoreKey(boardSize: Int) = intPreferencesKey("best_score_$boardSize")
    private fun savedGameStateKey(boardSize: Int) = stringPreferencesKey("saved_game_state_$boardSize")

    fun getBestScoreFlow(boardSize: Int): Flow<Int> {
        val key = bestScoreKey(boardSize)
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[key] ?: 0
            }
    }

    suspend fun updateBestScore(boardSize: Int, score: Int) {
        val key = bestScoreKey(boardSize)
        dataStore.edit { preferences ->
            val currentBest = preferences[key] ?: 0
            if (score > currentBest) {
                preferences[key] = score
            }
        }
    }

    fun getSavedGameStateFlow(boardSize: Int): Flow<SavedGameState?> {
        val key = savedGameStateKey(boardSize)
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val jsonString = preferences[key] ?: return@map null
                try {
                    json.decodeFromString<SavedGameState>(jsonString)
                } catch (_: Exception) {
                    null
                }
            }
    }

    suspend fun saveGameState(boardSize: Int, gameState: SavedGameState) {
        val stateKey = savedGameStateKey(boardSize)
        val bestKey = bestScoreKey(boardSize)

        val jsonString = json.encodeToString(gameState)

        dataStore.edit { preferences ->
            preferences[stateKey] = jsonString

            val currentBest = preferences[bestKey] ?: 0
            if (gameState.currentScore > currentBest) {
                preferences[bestKey] = gameState.currentScore
            }
        }
    }

    suspend fun clearSavedGameState(boardSize: Int) {
        val key = savedGameStateKey(boardSize)
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}
