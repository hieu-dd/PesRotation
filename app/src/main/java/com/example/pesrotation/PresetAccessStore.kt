package com.example.pesrotation

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.presetAccessDataStore by preferencesDataStore(name = "preset_access")
private val ExpiresAtKey = longPreferencesKey("expires_at")

class PresetAccessStore(private val context: Context) {
    val expiresAt: Flow<Long> = context.presetAccessDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[ExpiresAtKey] ?: 0L }

    suspend fun saveExpiry(expiresAt: Long) {
        context.presetAccessDataStore.edit { preferences ->
            preferences[ExpiresAtKey] = expiresAt
        }
    }
}
