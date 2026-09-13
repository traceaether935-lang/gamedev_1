package com.game.a2048.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsRepositoryTest {

    private fun createRepository(): StatisticsRepository {
        val fakeDataStore = FakeDataStore()
        return StatisticsRepository(fakeDataStore)
    }

    @Test
    fun defaultStatisticsAreZero() = runTest {
        val repository = createRepository()
        val stats = repository.getStatisticsFlow(4).first()
        assertEquals(0, stats.gamesPlayed)
        assertEquals(0, stats.gamesWon)
        assertEquals(0, stats.highestScore)
        assertEquals(0, stats.highestTile)
        assertEquals(0L, stats.totalMoves)
    }

    @Test
    fun recordGameStartedAndWon() = runTest {
        val repository = createRepository()
        val boardSize = 4

        repository.recordGameStarted(boardSize)
        repository.recordGameStarted(boardSize)
        repository.recordGameWon(boardSize)

        val stats = repository.getStatisticsFlow(boardSize).first()
        assertEquals(2, stats.gamesPlayed)
        assertEquals(1, stats.gamesWon)
    }

    @Test
    fun recordGameEndUpdatesHighestScoreAndTileAndMoves() = runTest {
        val repository = createRepository()
        val boardSize = 4

        repository.recordGameEnd(boardSize = boardSize, finalScore = 1000, highestTile = 512, moveCount = 150)
        var stats = repository.getStatisticsFlow(boardSize).first()
        assertEquals(1000, stats.highestScore)
        assertEquals(512, stats.highestTile)
        assertEquals(150L, stats.totalMoves)

        // Lower score and tile should not override highest
        repository.recordGameEnd(boardSize = boardSize, finalScore = 500, highestTile = 256, moveCount = 50)
        stats = repository.getStatisticsFlow(boardSize).first()
        assertEquals(1000, stats.highestScore)
        assertEquals(512, stats.highestTile)
        assertEquals(200L, stats.totalMoves)
    }
}
