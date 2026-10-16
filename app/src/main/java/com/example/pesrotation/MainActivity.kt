package com.example.pesrotation

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.pesrotation.ui.theme.BroadcastBackground
import com.example.pesrotation.ui.theme.BroadcastDanger
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MinTeamCount = 1
private const val MaxTeamCount = 4
private const val RotationLoadingMillis = 1_000L

private enum class AppScreen {
    HOME,
    HISTORY,
    SETTINGS,
}

class MainActivity : ComponentActivity() {
    private var rotationTrick by mutableStateOf(RotationTrick.NONE)
    private var tierOverrides by mutableStateOf(emptyMap<String, Tier>())
    private var disabledTeamIds by mutableStateOf(emptySet<String>())
    private var tierWeights by mutableStateOf(DefaultTierWeights)
    private var rotationHistory by mutableStateOf(emptyList<RotationHistoryEntry>())
    private var presetAccessExpiresAt by mutableStateOf(0L)
    private val tierPreferences by lazy { TeamTierPreferences(applicationContext) }
    private val historyStore by lazy { RotationHistoryStore(applicationContext) }
    private val presetAccessStore by lazy { PresetAccessStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            tierPreferences.overrides.collect { tierOverrides = it }
        }
        lifecycleScope.launch {
            tierPreferences.weights.collect { tierWeights = it }
        }
        lifecycleScope.launch {
            tierPreferences.disabledTeamIds.collect { disabledTeamIds = it }
        }
        lifecycleScope.launch {
            historyStore.history.collect { rotationHistory = it }
        }
        lifecycleScope.launch {
            presetAccessStore.expiresAt.collect { presetAccessExpiresAt = it }
        }
        setContent {
            PesRotationTheme {
                var currentScreen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
                val effectiveTeams = applyTierOverrides(teams, tierOverrides)
                when (currentScreen) {
                    AppScreen.HOME -> PesRotationScreen(
                        teamPool = effectiveTeams,
                        disabledTeamIds = disabledTeamIds,
                        tierWeights = tierWeights,
                        history = rotationHistory,
                        presetAccessExpiresAt = presetAccessExpiresAt,
                        rotationTrick = rotationTrick,
                        onTrickConsumed = { rotationTrick = RotationTrick.NONE },
                        onPresetAccessActivated = ::activatePresetAccess,
                        onRotationCompleted = ::recordRotation,
                        onOpenHistory = { currentScreen = AppScreen.HISTORY },
                        onOpenSettings = { currentScreen = AppScreen.SETTINGS },
                    )

                    AppScreen.HISTORY -> HistoryScreen(
                        history = rotationHistory,
                        teamPool = effectiveTeams,
                        onBack = { currentScreen = AppScreen.HOME },
                        onClear = ::clearHistory,
                    )

                    AppScreen.SETTINGS -> SettingsScreen(
                        teams = effectiveTeams,
                        disabledTeamIds = disabledTeamIds,
                        onBack = { currentScreen = AppScreen.HOME },
                        onTierChange = ::updateTeamTier,
                        onTeamEnabledChange = ::updateTeamEnabled,
                        weights = tierWeights,
                        onWeightsChange = ::updateTierWeights,
                        onReset = ::resetTeamTiers,
                    )
                }
            }
        }
    }

    private fun updateTeamTier(teamId: String, newTier: Tier) {
        val currentTeams = applyTierOverrides(teams, tierOverrides)
        if (!canChangeTeamTier(currentTeams, teamId, newTier, disabledTeamIds)) return

        val defaultTier = teams.find { it.id == teamId }?.tier
        tierOverrides = if (newTier == defaultTier) tierOverrides - teamId else tierOverrides + (teamId to newTier)
        lifecycleScope.launch { tierPreferences.setTier(teamId, newTier) }
    }

    private fun updateTeamEnabled(teamId: String, enabled: Boolean) {
        disabledTeamIds = if (enabled) disabledTeamIds - teamId else disabledTeamIds + teamId
        lifecycleScope.launch { tierPreferences.setTeamEnabled(teamId, enabled) }
    }

    private fun resetTeamTiers() {
        tierOverrides = emptyMap()
        disabledTeamIds = emptySet()
        tierWeights = DefaultTierWeights
        lifecycleScope.launch { tierPreferences.reset() }
    }

    private fun updateTierWeights(weights: TierWeights) {
        if (!weights.isValid) return
        tierWeights = weights
        lifecycleScope.launch { tierPreferences.setWeights(weights) }
    }

    private fun recordRotation(selectedTeams: List<Team>) {
        if (selectedTeams.isEmpty()) return
        val entry = RotationHistoryEntry(
            timestamp = System.currentTimeMillis(),
            teams = selectedTeams.map { HistoryTeam(it.id, it.tier) },
        )
        rotationHistory = addHistoryEntry(rotationHistory, entry)
        lifecycleScope.launch { historyStore.save(rotationHistory) }
    }

    private fun clearHistory() {
        rotationHistory = emptyList()
        lifecycleScope.launch { historyStore.clear() }
    }

    private fun activatePresetAccess() {
        presetAccessExpiresAt = presetAccessExpiry(System.currentTimeMillis())
        lifecycleScope.launch { presetAccessStore.saveExpiry(presetAccessExpiresAt) }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (!isPresetAccessActive(presetAccessExpiresAt)) {
            rotationTrick = RotationTrick.NONE
            return super.onKeyDown(keyCode, event)
        }
        val trick = when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> RotationTrick.VOLUME_UP_POOL
            KeyEvent.KEYCODE_VOLUME_DOWN -> RotationTrick.VOLUME_DOWN_POOL
            else -> return super.onKeyDown(keyCode, event)
        }
        if (event.repeatCount == 0) rotationTrick = trick
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        val isPresetKey = keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        if (isPresetKey && isPresetAccessActive(presetAccessExpiresAt)) return true
        return super.onKeyUp(keyCode, event)
    }
}

@Composable
fun PesRotationScreen(
    modifier: Modifier = Modifier,
    teamPool: List<Team> = teams,
    disabledTeamIds: Set<String> = emptySet(),
    tierWeights: TierWeights = DefaultTierWeights,
    history: List<RotationHistoryEntry> = emptyList(),
    presetAccessExpiresAt: Long = 0L,
    rotationTrick: RotationTrick = RotationTrick.NONE,
    onTrickConsumed: () -> Unit = {},
    onPresetAccessActivated: () -> Unit = {},
    onRotationCompleted: (List<Team>) -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    var teamCount by rememberSaveable { mutableStateOf("3") }
    var pendingTeamIds by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var rotationNotice by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedTeams = history.firstOrNull()?.resolveTeams(teamPool).orEmpty()
    val previousTeams = history.getOrNull(1)?.resolveTeams(teamPool).orEmpty()
    val parsedCount = teamCount.toIntOrNull()
    val hasError = teamCount.isNotEmpty() && parsedCount !in MinTeamCount..MaxTeamCount
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(RotationLoadingMillis)
            val completedTeams = pendingTeamIds.split(',').filter(String::isNotBlank)
                .mapNotNull { id -> teamPool.find { it.id == id } }
            pendingTeamIds = ""
            onRotationCompleted(completedTeams)
            isLoading = false
        }
    }

    fun rotate() {
        if (parsedCount !in MinTeamCount..MaxTeamCount || isLoading) return
        val requestedCount = requireNotNull(parsedCount)
        val activeTrick = rotationTrick.takeIf { isPresetAccessActive(presetAccessExpiresAt) }
            ?: RotationTrick.NONE
        if (rotationTrick != RotationTrick.NONE && activeTrick == RotationTrick.NONE) onTrickConsumed()
        val selected = when (activeTrick) {
            RotationTrick.VOLUME_UP_POOL -> rotateVolumeUpTeams(
                teamPool,
                requestedCount,
                disabledTeamIds = disabledTeamIds,
                weights = tierWeights,
            )

            RotationTrick.VOLUME_DOWN_POOL -> rotateVolumeDownTeams(
                teamPool,
                requestedCount,
                disabledTeamIds = disabledTeamIds,
                weights = tierWeights,
            )

            RotationTrick.NONE -> rotateTeams(
                teamPool,
                requestedCount,
                weights = tierWeights,
                disabledTeamIds = disabledTeamIds,
            )
        }
        if (selected.isEmpty()) {
            rotationNotice = "Cần ít nhất một đội Tier 2 đang bật trong pool quay này."
            return
        }
        rotationNotice = null
        if (activeTrick != RotationTrick.NONE && selected.size == requestedCount) onTrickConsumed()
        pendingTeamIds = selected.joinToString(",") { it.id }
        isLoading = true
        focusManager.clearFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to BroadcastBackground,
                    0.55f to Color(0xFF0B1413),
                    1f to BroadcastBackground,
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        BroadcastBackdrop()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
        ) {
            AppHeader(
                onPresetAccessActivated = onPresetAccessActivated,
                onOpenHistory = onOpenHistory,
                onOpenSettings = onOpenSettings,
                actionsEnabled = !isLoading,
            )
            DrawControl(
                teamCount = teamCount,
                hasError = hasError,
                isLoading = isLoading,
                message = rotationNotice,
                onTeamCountChange = { value ->
                    if (value.length <= 1 && value.all(Char::isDigit)) teamCount = value
                },
                onRotate = ::rotate,
                canRotate = parsedCount in MinTeamCount..MaxTeamCount && !isLoading,
            )
            Spacer(Modifier.height(20.dp))
            if (isLoading) {
                LoadingState(Modifier.weight(1f))
            } else if (selectedTeams.isEmpty()) {
                EmptyState(Modifier.weight(1f))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("LIVE DRAW", style = MaterialTheme.typography.labelLarge, color = BroadcastLime)
                    Box(
                        Modifier
                            .padding(horizontal = 10.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(BroadcastDanger),
                    )
                    Text("${selectedTeams.size} TEAMS", color = BroadcastTextMuted, fontSize = 12.sp)
                }
                Spacer(Modifier.height(10.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(selectedTeams, key = { _, team -> team.id }) { index, team ->
                        TeamCard(team = team, drawNumber = index + 1)
                    }
                }
                if (previousTeams.isNotEmpty()) {
                    PreviousRotation(previousTeams)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun AppHeader(
    onPresetAccessActivated: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    actionsEnabled: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppLogo(onPresetAccessActivated)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text("PES ROTATION", style = MaterialTheme.typography.titleLarge, color = BroadcastText)
            Text("MATCHDAY TEAM DRAW", color = BroadcastTextMuted, fontSize = 11.sp, letterSpacing = 1.5.sp)
        }
        HeaderAction(
            iconRes = R.drawable.ic_history,
            contentDescription = "Lịch sử quay",
            enabled = actionsEnabled,
            onClick = onOpenHistory,
        )
        Spacer(Modifier.width(8.dp))
        HeaderAction(
            iconRes = R.drawable.ic_settings,
            contentDescription = "Cài đặt tier",
            enabled = actionsEnabled,
            onClick = onOpenSettings,
        )
    }
}

@Composable
private fun AppLogo(onPresetAccessActivated: () -> Unit) {
    val hapticFeedback = LocalHapticFeedback.current
    val currentOnActivated by rememberUpdatedState(onPresetAccessActivated)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(BroadcastSurfaceHigh)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val up = waitForUpOrCancellation()
                    if (up != null && up.uptimeMillis - down.uptimeMillis >= PresetLongPressMillis) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentOnActivated()
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(R.drawable.ic_app_mark), null, Modifier.size(40.dp))
    }
}

@Composable
private fun HeaderAction(
    iconRes: Int,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BroadcastSurfaceHigh),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = onClick, enabled = enabled) {
                Icon(painterResource(iconRes), contentDescription, tint = BroadcastText)
            }
        }
}

@Composable
private fun DrawControl(
    teamCount: String,
    hasError: Boolean,
    isLoading: Boolean,
    message: String?,
    onTeamCountChange: (String) -> Unit,
    onRotate: () -> Unit,
    canRotate: Boolean,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BroadcastSurface),
        border = BorderStroke(1.dp, BroadcastLine),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("THIẾT LẬP LƯỢT QUAY", color = BroadcastTextMuted, fontSize = 11.sp, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = teamCount,
                onValueChange = onTeamCountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số đội") },
                trailingIcon = { Text("1 - 4", color = BroadcastTextMuted, fontWeight = FontWeight.Bold) },
                supportingText = { if (hasError) Text("Nhập số đội từ 1 đến 4") },
                isError = hasError,
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onRotate() }),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BroadcastLime,
                    unfocusedBorderColor = BroadcastLine,
                    focusedLabelColor = BroadcastLime,
                    cursorColor = BroadcastLime,
                    errorBorderColor = BroadcastDanger,
                ),
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onRotate,
                enabled = canRotate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BroadcastLime,
                    contentColor = BroadcastBackground,
                    disabledContainerColor = BroadcastSurfaceHigh,
                    disabledContentColor = BroadcastTextMuted,
                ),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = BroadcastLime,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("ĐANG QUAY", style = MaterialTheme.typography.labelLarge)
                } else {
                    Text("ROTATE TEAMS", style = MaterialTheme.typography.labelLarge)
                }
            }
            if (message != null) {
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 8.dp),
                    color = BroadcastDanger,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(52.dp),
            color = BroadcastLime,
            trackColor = BroadcastSurfaceHigh,
            strokeWidth = 4.dp,
        )
        Spacer(Modifier.height(18.dp))
        Text("ĐANG CHỌN ĐỘI", style = MaterialTheme.typography.titleMedium, color = BroadcastText)
        Text("Chuẩn bị kết quả lượt quay...", color = BroadcastTextMuted, fontSize = 13.sp)
    }
}

@Composable
private fun PreviousRotation(teams: List<Team>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BroadcastSurface),
        border = BorderStroke(1.dp, BroadcastLine),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("LƯỢT TRƯỚC", color = BroadcastTextMuted, fontSize = 10.sp, letterSpacing = 1.2.sp)
                Text(
                    teams.joinToString(" · ") { it.name },
                    color = BroadcastText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                )
            }
            Row(
                modifier = Modifier.padding(start = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                teams.forEach { team -> TeamLogo(team = team, size = 28.dp) }
            }
        }
    }
}

@Composable
private fun TeamCard(team: Team, drawNumber: Int) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = BroadcastSurface),
        border = BorderStroke(1.dp, BroadcastLine),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = drawNumber.toString().padStart(2, '0'),
                    color = BroadcastTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                TierBadge(team.tier)
            }
            Spacer(Modifier.height(8.dp))
            TeamLogo(team, 88.dp, contentDescription = "Logo ${team.name}")
            Spacer(Modifier.height(12.dp))
            Text(
                team.name,
                color = BroadcastText,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                minLines = 2,
                maxLines = 2,
            )
            Text(team.category.uppercase(), color = BroadcastTextMuted, fontSize = 10.sp, letterSpacing = 1.2.sp)
        }
    }
}

@Composable
fun TierBadge(tier: Tier, modifier: Modifier = Modifier) {
    val color = when (tier) {
        Tier.TIER_1 -> Tier1Color
        Tier.TIER_2 -> Tier2Color
        Tier.TIER_3 -> Tier3Color
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text("T${tier.level}", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(118.dp)
                .clip(CircleShape)
                .background(BroadcastSurface)
                .padding(20.dp),
        ) {
            Image(painterResource(R.drawable.ic_app_mark), null, Modifier.fillMaxSize(), alpha = 0.85f)
        }
        Spacer(Modifier.height(18.dp))
        Text("CHƯA CÓ KẾT QUẢ", style = MaterialTheme.typography.titleMedium, color = BroadcastText)
        Text("Thiết lập số đội và bắt đầu lượt quay", color = BroadcastTextMuted, fontSize = 13.sp)
    }
}

@Composable
private fun BroadcastBackdrop() {
    Text(
        text = "90'",
        modifier = Modifier.padding(start = 250.dp, top = 120.dp),
        color = Color.White.copy(alpha = 0.025f),
        fontSize = 112.sp,
        fontWeight = FontWeight.Black,
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(BroadcastLime),
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PesRotationPreview() {
    PesRotationTheme { PesRotationScreen() }
}
