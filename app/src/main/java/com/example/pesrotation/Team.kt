package com.example.pesrotation

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private const val MaxTier3Teams = 2
private const val MaxTier2Teams = 2
private const val MinRequiredTeamsPerPrimaryTier = 2
private const val MinVolumeDownPoolSize = 4
private const val ReducedTierPoolProbability = 0.6

data class TierWeights(
    val tier1: Int,
    val tier2: Int,
    val tier3: Int,
) {
    val isValid: Boolean
        get() = tier1 >= 0 && tier2 >= 0 && tier3 >= 0 && tier1 + tier2 + tier3 == 100

    operator fun get(tier: Tier): Int = when (tier) {
        Tier.TIER_1 -> tier1
        Tier.TIER_2 -> tier2
        Tier.TIER_3 -> tier3
    }
}

val DefaultTierWeights = TierWeights(tier1 = 25, tier2 = 40, tier3 = 35)

enum class Tier(val level: Int) {
    TIER_1(1),
    TIER_2(2),
    TIER_3(3),
}

enum class RotationTrick {
    NONE,
    VOLUME_UP_POOL,
    VOLUME_DOWN_POOL,
}

val VolumeUpTeamIds = setOf(
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
)

val VolumeDownTier1ExcludedTeamIds = setOf("liverpool", "psg")
val VolumeDownTier2ExcludedTeamIds = setOf(
    "juventus",
    "ac-milan",
    "argentina",
    "dortmund",
    "fenerbahce",
    "aston-villa",
)
val VolumeDownTier3IncludedTeamIds = setOf("netherlands", "tottenham", "italy")
val Tier1ReducedPoolExcludedTeamIds = setOf("england", "france", "arsenal", "real-madrid")
val Tier2ReducedPoolExcludedTeamIds = setOf("germany", "brazil")

data class Team(
    val id: String,
    val name: String,
    val category: String,
    val color: Color,
    val tier: Tier,
    @param:DrawableRes val imageRes: Int = R.drawable.team_badge,
)

val teams = listOf(
    Team("psg", "PSG", "CLB", Color(0xFF004170), Tier.TIER_1, R.drawable.team_psg),
    Team("liverpool", "Liverpool", "CLB", Color(0xFFC8102E), Tier.TIER_1, R.drawable.team_liverpool),
    Team("dortmund", "Dortmund", "CLB", Color(0xFFFDE100), Tier.TIER_3, R.drawable.team_dortmund),
    Team("benfica", "Benfica", "CLB", Color(0xFFE30613), Tier.TIER_3, R.drawable.team_benfica),
    Team("fenerbahce", "Fenerbahçe", "CLB", Color(0xFFFFD700), Tier.TIER_2, R.drawable.team_fenerbahce),
    Team("bayern-munich", "Bayern Munich", "CLB", Color(0xFF0066B2), Tier.TIER_1, R.drawable.team_bayern_munich),
    Team("man-city", "Man City", "CLB", Color(0xFF6CABDD), Tier.TIER_1, R.drawable.team_man_city),
    Team("man-utd", "Man Utd", "CLB", Color(0xFFDA291C), Tier.TIER_2, R.drawable.team_man_utd),
    Team("tottenham", "Tottenham", "CLB", Color(0xFF132257), Tier.TIER_3, R.drawable.team_tottenham),
    Team("aston-villa", "Aston Villa", "CLB", Color(0xFF670E36), Tier.TIER_2, R.drawable.team_aston_villa),
    Team("galatasaray", "Galatasaray", "CLB", Color(0xFFFFA500), Tier.TIER_3, R.drawable.team_galatasaray),
    Team("newcastle", "Newcastle United", "CLB", Color(0xFF111111), Tier.TIER_3, R.drawable.team_newcastle),
    Team("ac-milan", "AC Milan", "CLB", Color(0xFFB21E29), Tier.TIER_2, R.drawable.team_ac_milan),
    Team("as-roma", "AS Roma", "CLB", Color(0xFF8E1F2F), Tier.TIER_3, R.drawable.team_as_roma),
    Team("chelsea", "Chelsea", "CLB", Color(0xFF034694), Tier.TIER_2, R.drawable.team_chelsea),
    Team("juventus", "Juventus", "CLB", Color(0xFF202020), Tier.TIER_2, R.drawable.team_juventus),
    Team("atletico-madrid", "Atletico Madrid", "CLB", Color(0xFFCB3524), Tier.TIER_2, R.drawable.team_atletico_madrid),
    Team("napoli", "Napoli", "CLB", Color(0xFF12A0D7), Tier.TIER_3, R.drawable.team_napoli),
    Team("barcelona", "Barcelona", "CLB", Color(0xFF9B1D36), Tier.TIER_1, R.drawable.team_barcelona),
    Team("porto", "Porto", "CLB", Color(0xFF0046AD), Tier.TIER_3, R.drawable.team_porto),
    Team("real-madrid", "Real Madrid", "CLB", Color(0xFF315A9B), Tier.TIER_1, R.drawable.team_real_madrid),
    Team("arsenal", "Arsenal", "CLB", Color(0xFFEF0107), Tier.TIER_1, R.drawable.team_arsenal),
    Team("inter-milan", "Inter Milan", "CLB", Color(0xFF00529F), Tier.TIER_2, R.drawable.team_inter_milan),
    Team("argentina", "Argentina", "Đội tuyển", Color(0xFF75AADB), Tier.TIER_2, R.drawable.team_argentina),
    Team("brazil", "Brazil", "Đội tuyển", Color(0xFF009C3B), Tier.TIER_2, R.drawable.team_brazil),
    Team("netherlands", "Hà Lan", "Đội tuyển", Color(0xFFF36C21), Tier.TIER_3, R.drawable.team_netherlands),
    Team("germany", "Đức", "Đội tuyển", Color(0xFF242424), Tier.TIER_2, R.drawable.team_germany),
    Team("france", "Pháp", "Đội tuyển", Color(0xFF1A2F74), Tier.TIER_1, R.drawable.team_france),
    Team("england", "Anh", "Đội tuyển", Color(0xFFCE1124), Tier.TIER_1, R.drawable.team_england),
    Team("belgium", "Bỉ", "Đội tuyển", Color(0xFFEF3340), Tier.TIER_3, R.drawable.team_belgium),
    Team("portugal", "Bồ Đào Nha", "Đội tuyển", Color(0xFF046A38), Tier.TIER_2, R.drawable.team_portugal),
    Team("uruguay", "Uruguay", "Đội tuyển", Color(0xFF5CBFEB), Tier.TIER_3, R.drawable.team_uruguay),
    Team("spain", "Tây Ban Nha", "Đội tuyển", Color(0xFFAA151B), Tier.TIER_1, R.drawable.team_spain),
    Team("italy", "Ý", "Đội tuyển", Color(0xFF0066B3), Tier.TIER_3, R.drawable.team_italy),
)

fun applyTierOverrides(source: List<Team>, overrides: Map<String, Tier>): List<Team> =
    source.map { team -> overrides[team.id]?.let { team.copy(tier = it) } ?: team }

fun groupTeamsByTier(source: List<Team>): Map<Tier, List<Team>> = Tier.entries.associateWith { tier ->
    source.filter { it.tier == tier }.sortedBy { it.name.lowercase() }
}

fun canChangeTeamTier(
    source: List<Team>,
    teamId: String,
    newTier: Tier,
    disabledTeamIds: Set<String> = emptySet(),
): Boolean {
    val team = source.find { it.id == teamId } ?: return false
    if (team.tier == newTier) return true
    val updatedTeams = source.map { if (it.id == teamId) it.copy(tier = newTier) else it }
    val hasEnoughPrimaryTeams = listOf(Tier.TIER_1, Tier.TIER_2).all { tier ->
        updatedTeams.count { it.tier == tier } >= MinRequiredTeamsPerPrimaryTier
    }
    val enabledVolumeDownCount = updatedTeams.count { team ->
        team.id !in disabledTeamIds && isVolumeDownEligible(team)
    }
    return hasEnoughPrimaryTeams && enabledVolumeDownCount >= MinVolumeDownPoolSize
}

fun isVolumeDownEligible(team: Team): Boolean = when (team.tier) {
    Tier.TIER_1 -> team.id !in VolumeDownTier1ExcludedTeamIds
    Tier.TIER_2 -> team.id !in VolumeDownTier2ExcludedTeamIds
    Tier.TIER_3 -> team.id in VolumeDownTier3IncludedTeamIds
}

fun rotateTeams(
    source: List<Team>,
    count: Int,
    random: Random = Random.Default,
    weights: TierWeights = DefaultTierWeights,
    disabledTeamIds: Set<String> = emptySet(),
): List<Team> {
    val teamsByTier = Tier.entries.associateWith { tier ->
        val tierTeams = source.filter { it.tier == tier && it.id !in disabledTeamIds }
        val excludedTeamIds = when (tier) {
            Tier.TIER_1 -> Tier1ReducedPoolExcludedTeamIds
            Tier.TIER_2 -> Tier2ReducedPoolExcludedTeamIds
            Tier.TIER_3 -> emptySet()
        }
        if (
            tierTeams.isNotEmpty() && excludedTeamIds.isNotEmpty() &&
            random.nextDouble() < ReducedTierPoolProbability
        ) {
            tierTeams.filterNot { it.id in excludedTeamIds }
        } else {
            tierTeams
        }
    }
    return rotateFromTierPools(
        pools = teamsByTier,
        count = count,
        random = random,
        weights = weights,
        maxTier3Teams = MaxTier3Teams,
    )
}

fun randomTier(
    random: Random = Random.Default,
    weights: TierWeights = DefaultTierWeights,
): Tier {
    val validWeights = weights.takeIf { it.isValid } ?: DefaultTierWeights
    val roll = random.nextInt(100)
    return when {
        roll < validWeights.tier1 -> Tier.TIER_1
        roll < validWeights.tier1 + validWeights.tier2 -> Tier.TIER_2
        else -> Tier.TIER_3
    }
}

fun rotateVolumeUpTeams(
    source: List<Team>,
    count: Int,
    random: Random = Random.Default,
    disabledTeamIds: Set<String> = emptySet(),
    weights: TierWeights = DefaultTierWeights,
): List<Team> = rotateFromTierPools(
    pools = Tier.entries.associateWith { tier ->
        source.filter { it.tier == tier && it.id !in disabledTeamIds && it.id in VolumeUpTeamIds }
    },
    count = count,
    random = random,
    weights = weights,
)

fun rotateVolumeDownTeams(
    source: List<Team>,
    count: Int,
    random: Random = Random.Default,
    disabledTeamIds: Set<String> = emptySet(),
    weights: TierWeights = DefaultTierWeights,
): List<Team> = rotateFromTierPools(
    pools = Tier.entries.associateWith { tier ->
        source.filter {
            it.tier == tier && it.id !in disabledTeamIds && isVolumeDownEligible(it)
        }
    },
    count = count,
    random = random,
    weights = weights,
)

private fun rotateFromTierPools(
    pools: Map<Tier, List<Team>>,
    count: Int,
    random: Random,
    weights: TierWeights,
    maxTier3Teams: Int? = null,
): List<Team> {
    if (count <= 0) return emptyList()

    val tier2Pool = pools.getValue(Tier.TIER_2)
    if (tier2Pool.isEmpty()) return emptyList()

    val availableCount = pools.entries.sumOf { (tier, tierTeams) ->
        when (tier) {
            Tier.TIER_1 -> tierTeams.size
            Tier.TIER_2 -> tierTeams.size.coerceAtMost(MaxTier2Teams)
            Tier.TIER_3 -> maxTier3Teams?.let(tierTeams.size::coerceAtMost) ?: tierTeams.size
        }
    }
    val requestedCount = count.coerceAtMost(availableCount)
    if (requestedCount == 0) return emptyList()

    val selected = mutableListOf(tier2Pool.random(random))
    while (selected.size < requestedCount) {
        val selectedIds = selected.mapTo(mutableSetOf(), Team::id)
        val selectedTier2Count = selected.count { it.tier == Tier.TIER_2 }
        val selectedTier3Count = selected.count { it.tier == Tier.TIER_3 }
        val remainingByTier = Tier.entries.associateWith { tier ->
            val tierLimitReached = when (tier) {
                Tier.TIER_1 -> false
                Tier.TIER_2 -> selectedTier2Count >= MaxTier2Teams
                Tier.TIER_3 -> maxTier3Teams?.let { selectedTier3Count >= it } ?: false
            }
            if (tierLimitReached) {
                emptyList()
            } else {
                pools.getValue(tier).filter { it.id !in selectedIds }
            }
        }.filterValues { it.isNotEmpty() }

        if (remainingByTier.isEmpty()) break

        val tier = weightedTier(remainingByTier.keys, weights, random)
        selected += remainingByTier.getValue(tier).random(random)
    }

    return selected.shuffled(random)
}

private fun weightedTier(availableTiers: Set<Tier>, weights: TierWeights, random: Random): Tier {
    val validWeights = weights.takeIf { it.isValid } ?: DefaultTierWeights
    val totalWeight = availableTiers.sumOf(validWeights::get)
    if (totalWeight == 0) return availableTiers.random(random)

    var roll = random.nextInt(totalWeight)
    for (tier in Tier.entries) {
        if (tier !in availableTiers) continue
        val tierWeight = validWeights[tier]
        if (roll < tierWeight) return tier
        roll -= tierWeight
    }

    return availableTiers.first()
}
