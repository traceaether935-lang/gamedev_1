package com.game.a2048.data

import com.game.a2048.data.model.SavedGameState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameRepositoryTest {

    private fun createRepository(): GameRepository {
        val fakeDataStore = FakeDataStore()
        return GameRepository(fakeDataStore)
    }

    @Test
    fun defaultBestScoreIsZeroAndSavedStateIsNull() = runTest {
        val repository = createRepository()
        val bestScore = repository.getBestScoreFlow(4).first()
        val savedState = repository.getSavedGameStateFlow(4).first()
        assertEquals(0, bestScore)
        assertNull(savedState)
    }

    @Test
    fun updateBestScoreOnlyIncreasesScore() = runTest {
        val repository = createRepository()
        repository.updateBestScore(4, 2048)
        assertEquals(2048, repository.getBestScoreFlow(4).first())

        repository.updateBestScore(4, 1024)
        assertEquals(2048, repository.getBestScoreFlow(4).first())

        repository.updateBestScore(4, 4096)
        assertEquals(4096, repository.getBestScoreFlow(4).first())
    }

    @Test
    fun saveAndClearGameState() = runTest {
        val repository = createRepository()
        val boardSize = 4
        val stateToSave = SavedGameState(
            boardSize = boardSize,
            gridValues = listOf(
                2, 0, 0, 0,
                0, 4, 0, 0,
                0, 0, 8, 0,
                0, 0, 0, 16
            ),
            currentScore = 1200,
            moveCount = 45,
            status = "PLAYING",
            isContinued = false,
            undoGridValues = listOf(
                2, 0, 0, 0,
                0, 4, 0, 0,
                0, 0, 4, 4,
                0, 0, 0, 16
            ),
            undoScore = 1192,
            undoMoveCount = 44
        )

        repository.saveGameState(boardSize, stateToSave)

        val restoredState = repository.getSavedGameStateFlow(boardSize).first()
        assertEquals(stateToSave, restoredState)

        // Saving game state should also update best score if currentScore is higher
        assertEquals(1200, repository.getBestScoreFlow(boardSize).first())

        repository.clearSavedGameState(boardSize)
        assertNull(repository.getSavedGameStateFlow(boardSize).first())
    }
}
