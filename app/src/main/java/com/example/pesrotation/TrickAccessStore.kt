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

private val Context.trickAccessDataStore by preferencesDataStore(name = "trick_access")
private val ExpiresAtKey = longPreferencesKey("expires_at")

class TrickAccessStore(private val context: Context) {
    val expiresAt: Flow<Long> = context.trickAccessDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[ExpiresAtKey] ?: 0L }

    suspend fun saveExpiry(expiresAt: Long) {
        context.trickAccessDataStore.edit { preferences ->
            preferences[ExpiresAtKey] = expiresAt
        }
    }
}
