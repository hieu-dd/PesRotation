package com.example.pesrotation

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.rotationHistoryDataStore by preferencesDataStore(name = "rotation_history")
private val HistoryKey = stringPreferencesKey("entries")

class RotationHistoryStore(private val context: Context) {
    val history: Flow<List<RotationHistoryEntry>> = context.rotationHistoryDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> decodeHistory(preferences[HistoryKey].orEmpty()) }

    suspend fun save(history: List<RotationHistoryEntry>) {
        context.rotationHistoryDataStore.edit { preferences ->
            preferences[HistoryKey] = encodeHistory(history.take(MaxHistoryEntries))
        }
    }

    suspend fun clear() {
        context.rotationHistoryDataStore.edit { it.clear() }
    }
}
