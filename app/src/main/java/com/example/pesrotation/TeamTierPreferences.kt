package com.example.pesrotation

import android.content.Context
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.teamTierDataStore by preferencesDataStore(name = "team_tiers")
private val Tier1WeightKey = intPreferencesKey("tier_1_weight")
private val Tier2WeightKey = intPreferencesKey("tier_2_weight")
private val Tier3WeightKey = intPreferencesKey("tier_3_weight")

class TeamTierPreferences(private val context: Context) {
    val overrides: Flow<Map<String, Tier>> = context.teamTierDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            teams.mapNotNull { team ->
                val tier = preferences[stringPreferencesKey(team.id)]
                    ?.let { savedTier -> Tier.entries.find { it.name == savedTier } }
                tier?.let { team.id to it }
            }.toMap()
        }

    val weights: Flow<TierWeights> = context.teamTierDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            TierWeights(
                tier1 = preferences[Tier1WeightKey] ?: DefaultTierWeights.tier1,
                tier2 = preferences[Tier2WeightKey] ?: DefaultTierWeights.tier2,
                tier3 = preferences[Tier3WeightKey] ?: DefaultTierWeights.tier3,
            ).takeIf { it.isValid } ?: DefaultTierWeights
        }

    suspend fun setTier(teamId: String, tier: Tier) {
        context.teamTierDataStore.edit { preferences ->
            val defaultTier = teams.find { it.id == teamId }?.tier
            val key = stringPreferencesKey(teamId)
            if (tier == defaultTier) preferences.remove(key) else preferences[key] = tier.name
        }
    }

    suspend fun setWeights(weights: TierWeights) {
        if (!weights.isValid) return
        context.teamTierDataStore.edit { preferences ->
            preferences[Tier1WeightKey] = weights.tier1
            preferences[Tier2WeightKey] = weights.tier2
            preferences[Tier3WeightKey] = weights.tier3
        }
    }

    suspend fun reset() {
        context.teamTierDataStore.edit { it.clear() }
    }
}
