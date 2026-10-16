package com.example.pesrotation

import androidx.compose.ui.graphics.Color
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamRotationTest {
    private class SequenceRandom(private vararg val doubleRolls: Double) : Random() {
        var doubleRollCount = 0
            private set

        override fun nextBits(bitCount: Int): Int = 0

        override fun nextDouble(): Double = doubleRolls[doubleRollCount++]
    }

    private val source = (1..6).map { index ->
        val tier = if (index <= 4) Tier.TIER_1 else Tier.TIER_2
        Team(index.toString(), "Team $index", "Test", Color.Black, tier)
    }

    @Test
    fun rotateTeams_returnsRequestedNumberOfUniqueTeams() {
        val result = rotateTeams(source, count = 4, random = Random(1))

        assertEquals(4, result.size)
        assertEquals(4, result.distinctBy(Team::id).size)
    }

    @Test
    fun rotateTeams_doesNotReturnMoreThanAvailableTeams() {
        val result = rotateTeams(source, count = 10, random = Random(1))

        assertEquals(source.size, result.size)
    }

    @Test
    fun rotateTeams_returnsEmptyListForNegativeCount() {
        assertTrue(rotateTeams(source, count = -1).isEmpty())
    }

    @Test
    fun rotateTeams_returnsEmptyWhenNoTier2TeamIsAvailable() {
        assertTrue(rotateTeams(source.filter { it.tier != Tier.TIER_2 }, count = 3).isEmpty())
    }

    @Test
    fun rotationModes_neverReturnDisabledTeams() {
        val disabledTeamIds = setOf("arsenal", "brazil", "netherlands")

        val regularResult = rotateTeams(
            source = teams,
            count = 4,
            random = Random(1),
            disabledTeamIds = disabledTeamIds,
        )
        assertTrue(regularResult.none { it.id in disabledTeamIds })

        assertTrue(
            rotateVolumeUpTeams(
                source = teams,
                count = 4,
                random = Random(1),
                disabledTeamIds = VolumeUpTeamIds,
            ).isEmpty(),
        )

        val volumeDownTeamIds = teams.filter(::isVolumeDownEligible).map { it.id }.toSet()
        assertTrue(
            rotateVolumeDownTeams(
                source = teams,
                count = 4,
                random = Random(1),
                disabledTeamIds = volumeDownTeamIds,
            ).isEmpty(),
        )
    }

    @Test
    fun roster_containsThirtyFourUniqueTeams() {
        assertEquals(34, teams.size)
        assertEquals(teams.size, teams.distinctBy(Team::id).size)
        assertEquals(teams.size, teams.distinctBy(Team::imageRes).size)
    }

    @Test
    fun roster_containsExpectedTeamCountInEachTier() {
        assertEquals(10, teams.count { it.tier == Tier.TIER_1 })
        assertEquals(12, teams.count { it.tier == Tier.TIER_2 })
        assertEquals(12, teams.count { it.tier == Tier.TIER_3 })
    }

    @Test
    fun roster_usesUpdatedDefaultTierAssignments() {
        assertEquals(Tier.TIER_3, teams.first { it.id == "dortmund" }.tier)
        assertEquals(Tier.TIER_3, teams.first { it.id == "napoli" }.tier)
        assertEquals(Tier.TIER_2, teams.first { it.id == "man-utd" }.tier)
        assertEquals(Tier.TIER_2, teams.first { it.id == "germany" }.tier)
        assertEquals(Tier.TIER_2, teams.first { it.id == "fenerbahce" }.tier)
        assertEquals(Tier.TIER_2, teams.first { it.id == "aston-villa" }.tier)
        assertEquals(Tier.TIER_3, teams.first { it.id == "galatasaray" }.tier)
        assertEquals(Tier.TIER_3, teams.first { it.id == "newcastle" }.tier)
    }

    @Test
    fun applyTierOverrides_changesOnlyConfiguredTeams() {
        val result = applyTierOverrides(
            source = teams,
            overrides = mapOf("psg" to Tier.TIER_3, "italy" to Tier.TIER_1),
        )

        assertEquals(Tier.TIER_3, result.first { it.id == "psg" }.tier)
        assertEquals(Tier.TIER_1, result.first { it.id == "italy" }.tier)
        assertEquals(teams.first { it.id == "liverpool" }, result.first { it.id == "liverpool" })
    }

    @Test
    fun groupTeamsByTier_returnsTierOrderWithTeamsSortedByName() {
        val grouped = groupTeamsByTier(teams)

        assertEquals(Tier.entries, grouped.keys.toList())
        Tier.entries.forEach { tier ->
            val names = grouped.getValue(tier).map(Team::name)
            assertEquals(names.sortedBy { it.lowercase() }, names)
        }
    }

    @Test
    fun canChangeTeamTier_keepsAtLeastTwoTeamsInTier1AndTier2() {
        Tier.entries.take(2).forEach { protectedTier ->
            val protectedIds = teams.filter { it.tier == protectedTier }.take(2).map(Team::id).toSet()
            val constrainedTeams = teams.map { team ->
                if (team.tier == protectedTier && team.id !in protectedIds) {
                    team.copy(tier = Tier.TIER_3)
                } else {
                    team
                }
            }

            assertTrue(!canChangeTeamTier(constrainedTeams, protectedIds.first(), Tier.TIER_3))
            assertTrue(canChangeTeamTier(constrainedTeams, protectedIds.first(), protectedTier))
        }
    }

    @Test
    fun rotateTeams_returnsAtMostTwoTier3Teams() {
        repeat(100) { seed ->
            val result = rotateTeams(teams, count = 4, random = Random(seed))

            assertEquals(4, result.size)
            assertTrue(result.count { it.tier == Tier.TIER_3 } <= 2)
            assertTrue(result.count { it.tier == Tier.TIER_2 } in 1..2)
            assertTrue(result.any { it.tier == Tier.TIER_2 })
        }
    }

    @Test
    fun rotateTeams_canReturnTwoTier3Teams() {
        val tieredSource = listOf(
            Team("tier-2", "Tier 2", "Test", Color.Black, Tier.TIER_2),
            Team("tier-3-a", "Tier 3 A", "Test", Color.Black, Tier.TIER_3),
            Team("tier-3-b", "Tier 3 B", "Test", Color.Black, Tier.TIER_3),
            Team("tier-3-c", "Tier 3 C", "Test", Color.Black, Tier.TIER_3),
        )

        val result = rotateTeams(tieredSource, count = 3, random = Random(1))

        assertEquals(2, result.count { it.tier == Tier.TIER_3 })
        assertEquals(1, result.count { it.tier == Tier.TIER_2 })
    }

    @Test
    fun rotateTeams_alwaysIncludesTier2WhenAvailable() {
        (1..4).forEach { count ->
            repeat(100) { seed ->
                val result = rotateTeams(teams, count = count, random = Random(seed))

                assertTrue(result.any { it.tier == Tier.TIER_2 })
                assertTrue(result.count { it.tier == Tier.TIER_2 } <= 2)
            }
        }
    }

    @Test
    fun randomTier_usesConfiguredWeights() {
        val counts = Tier.entries.associateWith { 0 }.toMutableMap()
        val sampleSize = 100_000
        val random = Random(1234)

        repeat(sampleSize) {
            val tier = randomTier(random)
            counts[tier] = counts.getValue(tier) + 1
        }

        assertTrue(counts.getValue(Tier.TIER_1).toDouble() / sampleSize in 0.24..0.26)
        assertTrue(counts.getValue(Tier.TIER_2).toDouble() / sampleSize in 0.39..0.41)
        assertTrue(counts.getValue(Tier.TIER_3).toDouble() / sampleSize in 0.34..0.36)
    }

    @Test
    fun randomTier_usesCustomWeights() {
        assertEquals(Tier.TIER_1, randomTier(Random(1), TierWeights(100, 0, 0)))
        assertEquals(Tier.TIER_2, randomTier(Random(1), TierWeights(0, 100, 0)))
        assertEquals(Tier.TIER_3, randomTier(Random(1), TierWeights(0, 0, 100)))
    }

    @Test
    fun rotateTeams_usesCustomWeights() {
        val result = rotateTeams(
            source = teams,
            count = 4,
            random = Random(1),
            weights = TierWeights(0, 100, 0),
        )

        assertEquals(4, result.size)
        assertEquals(2, result.count { it.tier == Tier.TIER_2 })
    }

    @Test
    fun rotateTeams_appliesReducedPoolRollForEverySelectedTeam() {
        val requiredTier2 = Team("required-tier2", "Required Tier 2", "Test", Color.Black, Tier.TIER_2)
        val scriptedRandom = SequenceRandom(0.7, 0.2, 0.7)
        val source = listOf(
            teams.first { it.id == "psg" },
            teams.first { it.id == "england" },
            requiredTier2,
        )

        val result = rotateTeams(
            source = source,
            count = 3,
            random = scriptedRandom,
            weights = TierWeights(100, 0, 0),
        )

        assertEquals(3, result.size)
        assertEquals(setOf("psg", "england"), result.filter { it.tier == Tier.TIER_1 }.map { it.id }.toSet())
        assertTrue(result.any { it.id == requiredTier2.id })
        assertEquals(3, scriptedRandom.doubleRollCount)
    }

    @Test
    fun rotateTeams_reducesTier2PoolOnSixtyPercentRoll() {
        val result = rotateTeams(
            source = teams.filter { it.id in Tier2ReducedPoolExcludedTeamIds } +
                teams.first { it.id == "man-utd" },
            count = 1,
            random = SequenceRandom(0.2),
            weights = TierWeights(0, 100, 0),
        )

        assertEquals("man-utd", result.single().id)
    }

    @Test
    fun rotateTeams_reducesTier3PoolOnSixtyPercentRoll() {
        val requiredTier2 = Team("required-tier2", "Required Tier 2", "Test", Color.Black, Tier.TIER_2)
        val scriptedRandom = SequenceRandom(0.7, 0.2, 0.7)
        val tier3Teams = teams.filter { it.id in Tier3ReducedPoolExcludedTeamIds } +
            teams.first { it.id == "italy" }

        val result = rotateTeams(
            source = listOf(requiredTier2) + tier3Teams,
            count = 3,
            random = scriptedRandom,
            weights = TierWeights(0, 0, 100),
        )

        assertEquals(3, result.size)
        assertEquals(
            setOf("italy", "tottenham"),
            result.filter { it.tier == Tier.TIER_3 }.map { it.id }.toSet(),
        )
        assertEquals(3, scriptedRandom.doubleRollCount)
    }

    @Test
    fun rotateTeams_returnsEmptyWhenReducedTier2PoolHasNoAvailableTeam() {
        val result = rotateTeams(
            source = teams.filter { it.id in Tier2ReducedPoolExcludedTeamIds },
            count = 1,
            random = SequenceRandom(0.2),
            weights = TierWeights(0, 100, 0),
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun allRotationModesSelectOneOrTwoTier2Teams() {
        (1..4).forEach { count ->
            repeat(100) { seed ->
                val random = Random(seed)
                val results = listOf(
                    rotateTeams(teams, count, random),
                    rotateVolumeUpTeams(teams, count, random),
                    rotateVolumeDownTeams(teams, count, random),
                )

                results.forEach { result ->
                    assertEquals(count, result.size)
                    assertTrue(result.count { it.tier == Tier.TIER_2 } in 1..2)
                    assertEquals(result.size, result.distinctBy(Team::id).size)
                }
            }
        }
    }

    @Test
    fun allRotationModesReturnEmptyWhenNoEnabledTier2TeamIsAvailable() {
        val disabledTier2Ids = teams.filter { it.tier == Tier.TIER_2 }.map { it.id }.toSet()

        assertTrue(rotateTeams(teams, count = 4, disabledTeamIds = disabledTier2Ids).isEmpty())
        assertTrue(rotateVolumeUpTeams(teams, count = 4, disabledTeamIds = disabledTier2Ids).isEmpty())
        assertTrue(rotateVolumeDownTeams(teams, count = 4, disabledTeamIds = disabledTier2Ids).isEmpty())
    }

    @Test
    fun volumeTricks_useConfiguredWeightsAfterSelectingRequiredTier2() {
        val tier1OnlyWeights = TierWeights(100, 0, 0)

        listOf(
            rotateVolumeUpTeams(teams, count = 4, weights = tier1OnlyWeights),
            rotateVolumeDownTeams(teams, count = 4, weights = tier1OnlyWeights),
        ).forEach { result ->
            assertEquals(4, result.size)
            assertEquals(1, result.count { it.tier == Tier.TIER_2 })
            assertEquals(3, result.count { it.tier == Tier.TIER_1 })
        }
    }

    @Test
    fun tierWeights_requireNonNegativeValuesAndTotalOfOneHundred() {
        assertTrue(TierWeights(30, 40, 30).isValid)
        assertTrue(!TierWeights(30, 40, 29).isValid)
        assertTrue(!TierWeights(-1, 51, 50).isValid)
    }

    @Test
    fun rotateVolumeUpTeams_usesOnlyConfiguredTeams() {
        assertEquals(
            setOf(
                "england",
                "arsenal",
                "france",
                "real-madrid",
                "spain",
                "brazil",
                "portugal",
                "inter-milan",
                "man-utd",
                "germany",
                "tottenham",
            ),
            VolumeUpTeamIds,
        )

        (1..4).forEach { count ->
            val result = rotateVolumeUpTeams(teams, count, Random(count))

            assertEquals(count, result.size)
            assertTrue(result.all { it.id in VolumeUpTeamIds })
            assertTrue(result.any { it.tier == Tier.TIER_2 })
            assertEquals(result.size, result.distinctBy(Team::id).size)
        }
    }

    @Test
    fun rotateVolumeUpTeams_returnsEmptyWhenNoTier2TeamIsAvailable() {
        val volumeUpPoolWithoutTier2 = teams
            .filter { it.id in VolumeUpTeamIds }
            .map { it.copy(tier = Tier.TIER_1) }

        assertTrue(rotateVolumeUpTeams(volumeUpPoolWithoutTier2, count = 2, random = Random(1)).isEmpty())
    }

    @Test
    fun rotateVolumeDownTeams_usesConfiguredTierRules() {
        val expectedEligibleTeamIds = setOf(
            "bayern-munich",
            "man-city",
            "barcelona",
            "real-madrid",
            "arsenal",
            "france",
            "england",
            "spain",
            "chelsea",
            "atletico-madrid",
            "inter-milan",
            "brazil",
            "portugal",
            "man-utd",
            "germany",
            "tottenham",
            "netherlands",
            "italy",
        )
        assertEquals(expectedEligibleTeamIds, teams.filter(::isVolumeDownEligible).map(Team::id).toSet())
        assertTrue(teams.filter { it.id in setOf("fenerbahce", "aston-villa", "galatasaray", "newcastle") }
            .none(::isVolumeDownEligible))

        val result = rotateVolumeDownTeams(teams, count = 4, random = Random(1))

        assertEquals(4, result.size)
        assertTrue(result.all(::isVolumeDownEligible))
        assertTrue(result.all { it.id in expectedEligibleTeamIds })
        assertTrue(result.count { it.tier == Tier.TIER_2 } in 1..2)
        assertEquals(result.size, result.distinctBy(Team::id).size)
    }

    @Test
    fun canChangeTeamTier_keepsAtLeastFourVolumeDownTeams() {
        val constrainedSource = listOf(
            Team("psg", "PSG", "Test", Color.Black, Tier.TIER_1),
            Team("liverpool", "Liverpool", "Test", Color.Black, Tier.TIER_1),
            Team("a", "A", "Test", Color.Black, Tier.TIER_1),
            Team("b", "B", "Test", Color.Black, Tier.TIER_1),
            Team("e", "E", "Test", Color.Black, Tier.TIER_1),
            Team("c", "C", "Test", Color.Black, Tier.TIER_2),
            Team("d", "D", "Test", Color.Black, Tier.TIER_2),
            Team("dortmund", "Dortmund", "Test", Color.Black, Tier.TIER_2),
            Team("ac-milan", "AC Milan", "Test", Color.Black, Tier.TIER_2),
        )

        assertTrue(canChangeTeamTier(constrainedSource, "a", Tier.TIER_3))
        assertTrue(!canChangeTeamTier(constrainedSource, "a", Tier.TIER_3, disabledTeamIds = setOf("b")))
    }
}
