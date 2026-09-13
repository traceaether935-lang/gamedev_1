package com.game.a2048.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryTest {

    private fun createRepository(): Pair<SettingsRepository, FakeDataStore> {
        val fakeDataStore = FakeDataStore()
        val repository = SettingsRepository(fakeDataStore)
        return Pair(repository, fakeDataStore)
    }

    @Test
    fun defaultSettingsAreTrue() = runTest {
        val (repository, _) = createRepository()
        val settings = repository.userSettingsFlow.first()
        assertTrue(settings.soundEnabled)
        assertTrue(settings.hapticsEnabled)
    }

    @Test
    fun updateSoundAndHaptics() = runTest {
        val (repository, _) = createRepository()

        repository.setSoundEnabled(false)
        var settings = repository.userSettingsFlow.first()
        assertFalse(settings.soundEnabled)
        assertTrue(settings.hapticsEnabled)

        repository.setHapticsEnabled(false)
        settings = repository.userSettingsFlow.first()
        assertFalse(settings.soundEnabled)
        assertFalse(settings.hapticsEnabled)
    }

    @Test
    fun updateThemeMode() = runTest {
        val (repository, _) = createRepository()

        repository.setThemeMode("DARK")
        val settings = repository.userSettingsFlow.first()
        Assert.assertEquals("DARK", settings.themeMode)
    }
}
