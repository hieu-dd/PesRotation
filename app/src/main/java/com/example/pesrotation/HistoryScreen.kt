package com.example.pesrotation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pesrotation.ui.theme.BroadcastBackground
import com.example.pesrotation.ui.theme.BroadcastLime
import com.example.pesrotation.ui.theme.BroadcastLine
import com.example.pesrotation.ui.theme.BroadcastSurface
import com.example.pesrotation.ui.theme.BroadcastSurfaceHigh
import com.example.pesrotation.ui.theme.BroadcastText
import com.example.pesrotation.ui.theme.BroadcastTextMuted
import com.example.pesrotation.ui.theme.PesRotationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    history: List<RotationHistoryEntry>,
    teamPool: List<Team>,
    onBack: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearConfirmation by rememberSaveable { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("HH:mm, dd/MM", Locale.getDefault()) }
    BackHandler(onBack = onBack)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BroadcastBackground, Color(0xFF0B1413), BroadcastBackground),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(BroadcastLime),
        )
        Column(Modifier.fillMaxSize()) {
            HistoryHeader(
                historyCount = history.size,
                onBack = onBack,
                onClear = { showClearConfirmation = true },
            )
            if (history.isEmpty()) {
                EmptyHistory(Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(history, key = { _, entry -> entry.timestamp }) { index, entry ->
                        HistoryCard(
                            position = index + 1,
                            timestamp = dateFormatter.format(Date(entry.timestamp)),
                            teams = entry.resolveTeams(teamPool),
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            containerColor = BroadcastSurfaceHigh,
            titleContentColor = BroadcastText,
            textContentColor = BroadcastTextMuted,
            title = { Text("Xóa toàn bộ lịch sử?") },
            text = { Text("Không thể khôi phục các lượt quay sau khi xóa.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClear()
                        showClearConfirmation = false
                    },
                ) {
                    Text("XÓA", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("HỦY", color = BroadcastTextMuted)
                }
            },
        )
    }
}

@Composable
private fun HistoryHeader(
    historyCount: Int,
    onBack: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 10.dp, top = 16.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BroadcastSurfaceHigh),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = onBack) {
                Icon(painterResource(R.drawable.ic_arrow_back), "Quay lại", tint = BroadcastText)
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text("DRAW HISTORY", style = MaterialTheme.typography.titleLarge, color = BroadcastText)
            Text(
                "$historyCount / $MaxHistoryEntries LƯỢT GẦN NHẤT",
                color = BroadcastTextMuted,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
            )
        }
        TextButton(onClick = onClear, enabled = historyCount > 0) {
            Text("XÓA", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun HistoryCard(
    position: Int,
    timestamp: String,
    teams: List<Team>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = BroadcastSurface),
        border = BorderStroke(1.dp, BroadcastLine),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "#${position.toString().padStart(2, '0')}",
                    color = if (position == 1) BroadcastLime else BroadcastTextMuted,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.weight(1f))
                Text(timestamp, color = BroadcastTextMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            teams.forEach { team ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TeamLogo(team = team, size = 38.dp)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp),
                    ) {
                        Text(team.name, color = BroadcastText, fontWeight = FontWeight.Bold)
                        Text(team.category.uppercase(), color = BroadcastTextMuted, fontSize = 9.sp)
                    }
                    TierBadge(team.tier)
                }
            }
        }
    }
}

@Composable
private fun EmptyHistory(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(BroadcastSurface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_history),
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = BroadcastTextMuted,
            )
        }
        Spacer(Modifier.height(18.dp))
        Text("CHƯA CÓ LỊCH SỬ", style = MaterialTheme.typography.titleMedium, color = BroadcastText)
        Text("Các lượt quay mới sẽ xuất hiện tại đây", color = BroadcastTextMuted, fontSize = 13.sp)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HistoryPreview() {
    PesRotationTheme {
        HistoryScreen(
            history = listOf(
                RotationHistoryEntry(
                    timestamp = 1_700_000_000_000,
                    teams = teams.take(3).map { HistoryTeam(it.id, it.tier) },
                ),
            ),
            teamPool = teams,
            onBack = {},
            onClear = {},
        )
    }
}
