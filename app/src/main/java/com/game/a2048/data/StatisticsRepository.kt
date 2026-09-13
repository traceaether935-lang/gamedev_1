package com.game.a2048.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.game.a2048.data.model.GameStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.statisticsDataStore: DataStore<Preferences> by preferencesDataStore(name = "game_statistics")

class StatisticsRepository(
    private val dataStore: DataStore<Preferences>
) {
    constructor(context: Context) : this(context.statisticsDataStore)

    private fun gamesPlayedKey(boardSize: Int) = intPreferencesKey("games_played_$boardSize")
    private fun gamesWonKey(boardSize: Int) = intPreferencesKey("games_won_$boardSize")
    private fun highestScoreKey(boardSize: Int) = intPreferencesKey("highest_score_$boardSize")
    private fun highestTileKey(boardSize: Int) = intPreferencesKey("highest_tile_$boardSize")
    private fun totalMovesKey(boardSize: Int) = longPreferencesKey("total_moves_$boardSize")

    fun getStatisticsFlow(boardSize: Int): Flow<GameStatistics> {
        val keyPlayed = gamesPlayedKey(boardSize)
        val keyWon = gamesWonKey(boardSize)
        val keyScore = highestScoreKey(boardSize)
        val keyTile = highestTileKey(boardSize)
        val keyMoves = totalMovesKey(boardSize)

        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                GameStatistics(
                    gamesPlayed = preferences[keyPlayed] ?: 0,
                    gamesWon = preferences[keyWon] ?: 0,
                    highestScore = preferences[keyScore] ?: 0,
                    highestTile = preferences[keyTile] ?: 0,
                    totalMoves = preferences[keyMoves] ?: 0L
                )
            }
    }

    suspend fun recordGameStarted(boardSize: Int) {
        val keyPlayed = gamesPlayedKey(boardSize)
        dataStore.edit { preferences ->
            val current = preferences[keyPlayed] ?: 0
            preferences[keyPlayed] = current + 1
        }
    }

    suspend fun recordGameWon(boardSize: Int) {
        val keyWon = gamesWonKey(boardSize)
        dataStore.edit { preferences ->
            val current = preferences[keyWon] ?: 0
            preferences[keyWon] = current + 1
        }
    }

    suspend fun recordGameEnd(boardSize: Int, finalScore: Int, highestTile: Int, moveCount: Int) {
        val keyScore = highestScoreKey(boardSize)
        val keyTile = highestTileKey(boardSize)
        val keyMoves = totalMovesKey(boardSize)

        dataStore.edit { preferences ->
            val currentHighestScore = preferences[keyScore] ?: 0
            if (finalScore > currentHighestScore) {
                preferences[keyScore] = finalScore
            }

            val currentHighestTile = preferences[keyTile] ?: 0
            if (highestTile > currentHighestTile) {
                preferences[keyTile] = highestTile
            }

            val currentTotalMoves = preferences[keyMoves] ?: 0L
            preferences[keyMoves] = currentTotalMoves + moveCount
        }
    }
}
