package com.game.a2048.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeDataStore(
    initialPreferences: Preferences = emptyPreferences()
) : DataStore<Preferences> {
    private val _data = MutableStateFlow(initialPreferences)
    override val data: Flow<Preferences> = _data.asStateFlow()

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val current = _data.value
        val mutable = current.toMutablePreferences()
        val updated = transform(mutable)
        _data.value = updated
        return updated
    }
}
