package com.example.pesrotation

data class HistoryTeam(
    val id: String,
    val tier: Tier,
)

data class RotationHistoryEntry(
    val timestamp: Long,
    val teams: List<HistoryTeam>,
)

const val MaxHistoryEntries = 10

fun addHistoryEntry(
    history: List<RotationHistoryEntry>,
    entry: RotationHistoryEntry,
): List<RotationHistoryEntry> = (listOf(entry) + history).take(MaxHistoryEntries)

fun RotationHistoryEntry.resolveTeams(source: List<Team>): List<Team> = teams.mapNotNull { snapshot ->
    source.find { it.id == snapshot.id }?.copy(tier = snapshot.tier)
}

fun encodeHistory(history: List<RotationHistoryEntry>): String = history.joinToString("\n") { entry ->
    val encodedTeams = entry.teams.joinToString(",") { "${it.id}:${it.tier.name}" }
    "${entry.timestamp}|$encodedTeams"
}

fun decodeHistory(value: String): List<RotationHistoryEntry> = value
    .lineSequence()
    .mapNotNull { line ->
        val parts = line.split('|', limit = 2)
        val timestamp = parts.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
        val snapshots = parts.getOrNull(1)
            ?.split(',')
            ?.mapNotNull { encodedTeam ->
                val teamParts = encodedTeam.split(':', limit = 2)
                val id = teamParts.getOrNull(0)?.takeIf(String::isNotBlank) ?: return@mapNotNull null
                val tier = teamParts.getOrNull(1)?.let { name -> Tier.entries.find { it.name == name } }
                    ?: return@mapNotNull null
                HistoryTeam(id, tier)
            }
            .orEmpty()
        snapshots.takeIf { it.isNotEmpty() }?.let { RotationHistoryEntry(timestamp, it) }
    }
    .take(MaxHistoryEntries)
    .toList()
