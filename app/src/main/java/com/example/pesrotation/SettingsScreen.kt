package com.example.pesrotation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import com.example.pesrotation.ui.theme.Tier1Color
import com.example.pesrotation.ui.theme.Tier2Color
import com.example.pesrotation.ui.theme.Tier3Color

@Composable
fun SettingsScreen(
    teams: List<Team>,
    disabledTeamIds: Set<String>,
    onBack: () -> Unit,
    onTierChange: (String, Tier) -> Unit,
    onTeamEnabledChange: (String, Boolean) -> Unit,
    weights: TierWeights,
    onWeightsChange: (TierWeights) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showResetConfirmation by rememberSaveable { mutableStateOf(false) }
    val groupedTeams = groupTeamsByTier(teams)
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
            SettingsHeader(
                onBack = onBack,
                onReset = { showResetConfirmation = true },
            )
            TierSummary(teams)
            TierWeightsEditor(
                weights = weights,
                onApply = onWeightsChange,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
            )
            Text(
                text = "Tắt đội để loại khỏi mọi kiểu random. Tier 1 và Tier 2 cần tối thiểu 2 đội.",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                color = BroadcastTextMuted,
                fontSize = 12.sp,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Tier.entries.forEach { tier ->
                    val tierTeams = groupedTeams.getValue(tier)
                    item(key = "header-${tier.name}") {
                        TierSectionHeader(tier = tier, count = tierTeams.size)
                    }
                    items(tierTeams, key = { it.id }) { team ->
                        TeamTierItem(
                            team = team,
                            isEnabled = team.id !in disabledTeamIds,
                            canSelectTier = { selectedTier ->
                                canChangeTeamTier(teams, team.id, selectedTier, disabledTeamIds)
                            },
                            onTierChange = { selectedTier -> onTierChange(team.id, selectedTier) },
                            onEnabledChange = { enabled -> onTeamEnabledChange(team.id, enabled) },
                        )
                    }
                    item(key = "space-${tier.name}") { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            containerColor = BroadcastSurfaceHigh,
            titleContentColor = BroadcastText,
            textContentColor = BroadcastTextMuted,
            title = { Text("Khôi phục cài đặt mặc định?") },
            text = { Text("Thay đổi tier, tỉ lệ random và trạng thái bật/tắt đội sẽ được xóa.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReset()
                        showResetConfirmation = false
                    },
                ) {
                    Text("KHÔI PHỤC", color = BroadcastLime, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("HỦY", color = BroadcastTextMuted)
                }
            },
        )
    }
}

@Composable
private fun TierWeightsEditor(
    weights: TierWeights,
    onApply: (TierWeights) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tier1 by rememberSaveable(weights.tier1) { mutableStateOf(weights.tier1.toString()) }
    var tier2 by rememberSaveable(weights.tier2) { mutableStateOf(weights.tier2.toString()) }
    var tier3 by rememberSaveable(weights.tier3) { mutableStateOf(weights.tier3.toString()) }
    val candidate = TierWeights(
        tier1 = tier1.toIntOrNull() ?: -1,
        tier2 = tier2.toIntOrNull() ?: -1,
        tier3 = tier3.toIntOrNull() ?: -1,
    )
    val total = listOf(tier1, tier2, tier3).sumOf { it.toIntOrNull() ?: 0 }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BroadcastSurface),
        border = BorderStroke(1.dp, BroadcastLine),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("TỈ LỆ RANDOM", color = BroadcastText, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
                    Text("Chỉ áp dụng cho lượt quay thường", color = BroadcastTextMuted, fontSize = 11.sp)
                }
                Text(
                    "$total%",
                    color = if (candidate.isValid) BroadcastLime else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Black,
                )
            }
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WeightField("T1", tier1, Tier1Color, { tier1 = it }, Modifier.weight(1f))
                WeightField("T2", tier2, Tier2Color, { tier2 = it }, Modifier.weight(1f))
                WeightField("T3", tier3, Tier3Color, { tier3 = it }, Modifier.weight(1f))
            }
            Button(
                onClick = { onApply(candidate) },
                enabled = candidate.isValid && candidate != weights,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(46.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BroadcastLime,
                    contentColor = BroadcastBackground,
                    disabledContainerColor = BroadcastSurfaceHigh,
                    disabledContentColor = BroadcastTextMuted,
                ),
            ) {
                Text("ÁP DỤNG TỈ LỆ", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun WeightField(
    label: String,
    value: String,
    color: Color,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= 3 && newValue.all(Char::isDigit)) onValueChange(newValue)
        },
        modifier = modifier,
        label = { Text(label) },
        suffix = { Text("%") },
        singleLine = true,
        textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(13.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = color,
            focusedLabelColor = color,
            cursorColor = color,
            unfocusedBorderColor = BroadcastLine,
        ),
    )
}

@Composable
private fun SettingsHeader(onBack: () -> Unit, onReset: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 10.dp, top = 16.dp, bottom = 14.dp),
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
            Text("TEAM TIERS", style = MaterialTheme.typography.titleLarge, color = BroadcastText)
            Text("SẮP XẾP SỨC MẠNH ĐỘI", color = BroadcastTextMuted, fontSize = 10.sp, letterSpacing = 1.4.sp)
        }
        TextButton(onClick = onReset) {
            Text("RESET", color = BroadcastLime, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun TierSummary(teams: List<Team>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Tier.entries.forEach { tier ->
            val color = tierColor(tier)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BroadcastSurface)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                Text(
                    "T${tier.level}",
                    modifier = Modifier.padding(start = 6.dp),
                    color = BroadcastTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    teams.count { it.tier == tier }.toString().padStart(2, '0'),
                    color = BroadcastText,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

@Composable
private fun TierSectionHeader(tier: Tier, count: Int) {
    val color = tierColor(tier)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Text(
            "TIER ${tier.level}",
            modifier = Modifier.padding(start = 10.dp),
            color = BroadcastText,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.weight(1f))
        Text("$count ĐỘI", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TeamTierItem(
    team: Team,
    isEnabled: Boolean,
    canSelectTier: (Tier) -> Boolean,
    onTierChange: (Tier) -> Unit,
    onEnabledChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BroadcastSurface),
        border = BorderStroke(1.dp, BroadcastLine),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeamLogo(
                    team = team,
                    size = 48.dp,
                    modifier = Modifier.alpha(if (isEnabled) 1f else 0.4f),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                ) {
                    Text(
                        team.name,
                        color = if (isEnabled) BroadcastText else BroadcastTextMuted,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                    )
                    Text(
                        "${team.category.uppercase()} · ${if (isEnabled) "ĐANG BẬT" else "ĐÃ TẮT"}",
                        color = BroadcastTextMuted,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp,
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BroadcastBackground,
                        checkedTrackColor = BroadcastLime,
                        uncheckedThumbColor = BroadcastTextMuted,
                        uncheckedTrackColor = BroadcastSurfaceHigh,
                    ),
                )
            }
            Row(
                modifier = Modifier
                    .padding(start = 58.dp, top = 6.dp)
                    .width(156.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Tier.entries.forEach { tier ->
                    val color = tierColor(tier)
                    val selected = team.tier == tier
                    val canSelect = canSelectTier(tier)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .alpha(if (canSelect) 1f else 0.35f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) color else BroadcastSurfaceHigh)
                            .border(1.dp, if (selected) color else BroadcastLine, RoundedCornerShape(12.dp))
                            .clickable(enabled = canSelect) { onTierChange(tier) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "T${tier.level}",
                            color = if (selected) BroadcastBackground else BroadcastTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
        }
    }
}

private fun tierColor(tier: Tier): Color = when (tier) {
    Tier.TIER_1 -> Tier1Color
    Tier.TIER_2 -> Tier2Color
    Tier.TIER_3 -> Tier3Color
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsScreenPreview() {
    PesRotationTheme {
        SettingsScreen(
            teams = teams,
            disabledTeamIds = emptySet(),
            onBack = {},
            onTierChange = { _, _ -> },
            onTeamEnabledChange = { _, _ -> },
            weights = DefaultTierWeights,
            onWeightsChange = {},
            onReset = {},
        )
    }
}
