package com.example.pesrotation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RotationHistoryTest {
    @Test
    fun historyCodec_roundTripsEntriesAndTierSnapshots() {
        val history = listOf(
            RotationHistoryEntry(
                timestamp = 1234L,
                teams = listOf(
                    HistoryTeam("arsenal", Tier.TIER_3),
                    HistoryTeam("brazil", Tier.TIER_1),
                ),
            ),
        )

        assertEquals(history, decodeHistory(encodeHistory(history)))
    }

    @Test
    fun addHistoryEntry_keepsTenNewestEntries() {
        val history = (1L..12L).fold(emptyList<RotationHistoryEntry>()) { entries, timestamp ->
            addHistoryEntry(
                entries,
                RotationHistoryEntry(timestamp, listOf(HistoryTeam("arsenal", Tier.TIER_1))),
            )
        }

        assertEquals(MaxHistoryEntries, history.size)
        assertEquals(12L, history.first().timestamp)
        assertEquals(3L, history.last().timestamp)
    }

    @Test
    fun resolveTeams_usesTierSnapshotInsteadOfCurrentTier() {
        val entry = RotationHistoryEntry(
            timestamp = 1L,
            teams = listOf(HistoryTeam("arsenal", Tier.TIER_3)),
        )

        val resolved = entry.resolveTeams(teams)

        assertEquals(Tier.TIER_3, resolved.single().tier)
    }

    @Test
    fun decodeHistory_ignoresMalformedEntries() {
        val history = decodeHistory("invalid\n123|arsenal:TIER_1\n456|unknown-tier")

        assertEquals(1, history.size)
        assertTrue(history.single().teams.any { it.id == "arsenal" })
    }
}
