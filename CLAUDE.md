# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Single-module Android app (`:app`, package `com.example.pesrotation`) that randomly draws 1–4 football
teams for PES matchdays. 100% Kotlin + Jetpack Compose, no DI framework, no ViewModels, no navigation
library. UI strings are hardcoded Vietnamese in composables (`strings.xml` holds only `app_name`).

## Commands

```bash
./gradlew assembleDebug                 # build
./gradlew test                          # JVM unit tests (app/src/test)
./gradlew testDebugUnitTest             # same, debug variant only
./gradlew testDebugUnitTest --tests "com.example.pesrotation.TeamRotationTest"          # one class
./gradlew testDebugUnitTest --tests "*.TeamRotationTest.rotateTeams_returnsRequestedNumberOfUniqueTeams"  # one test
./gradlew connectedDebugAndroidTest     # instrumented tests, needs a device/emulator
./gradlew lint                          # Android Lint; report at app/build/reports/lint-results-debug.html
./gradlew installDebug                  # install onto connected device
```

Gradle 9.5 with configuration cache on; daemon JVM toolchain is Java 25 (`gradle/gradle-daemon-jvm.properties`),
auto-provisioned via foojay resolver. Dependency versions live only in `gradle/libs.versions.toml` — add
libraries there, never inline coordinates in `app/build.gradle.kts`.

## Architecture

**Activity-as-state-holder.** `MainActivity` is the only Activity and holds all app state in
`mutableStateOf` properties (tier overrides, tier weights, rotation history, preset-access expiry, pending
preset). In `onCreate` it launches one `lifecycleScope` collector per DataStore `Flow` to mirror persisted
state into those properties, then `setContent` renders one of three screens selected by a
`rememberSaveable` `AppScreen` enum — there is no Navigation component. Screens are stateless composables
receiving data + callbacks; mutations go through private `MainActivity` methods that update the in-memory
state *and* launch a coroutine to persist.

**Rotation logic is pure and testable.** `Team.kt` holds the static 30-team roster plus all selection
functions (`rotateTeams`, `rotateFeaturedTeams`, `rotateBalancedTeams`, `randomTier`,
`applyTierOverrides`, `canChangeTeamTier`). Every function takes `source: List<Team>` and an injectable
`random: Random` so tests are deterministic with `Random(seed)`. Keep new selection logic here as free
functions rather than inside composables.

`rotateTeams` works by rejection sampling: it rolls a tier per requested slot using `TierWeights` (three
ints that must sum to 100), retries up to `MaxSelectionAttempts` until the draw satisfies the invariants
(at most `MaxTier3Teams` = 2 tier-3 teams; at least one tier-2 team when tier 2 is non-empty; enough teams
in each tier), then falls back to `fallbackRotation`. `canChangeTeamTier` guards Settings edits so a user
cannot reassign tiers into a state that starves those invariants.

**Three separate DataStore files**, one store class each, all following the same shape (a `Flow` that
catches `IOException` → `emptyPreferences()`, plus suspend writers):

| Store | DataStore name | Holds |
| --- | --- | --- |
| `TeamTierPreferences` | `team_tiers` | per-team tier overrides (key = team id; entry removed when set back to default) + tier weights |
| `RotationHistoryStore` | `rotation_history` | last `MaxHistoryEntries` (10) draws, one string blob |
| `PresetAccessStore` | `preset_access` | preset-access expiry epoch millis |

History is serialized by hand in `RotationHistory.kt` as `timestamp|id:TIER,id:TIER` lines — `encodeHistory`
/`decodeHistory` must stay round-trip compatible or saved history is silently dropped. Entries store a
**tier snapshot** per team so `resolveTeams` replays the tier as it was at draw time, not the current one.

**The current draw result is not separate state** — the home screen renders `history.first()` as the
current result and `history[1]` as "previous rotation". A rotation writes to history, and the UI follows.
Rotating sets `pendingTeamIds` + `isLoading`; a `LaunchedEffect` waits `RotationLoadingMillis` (1s fake
suspense) before calling `onRotationCompleted`.

## Conventions

- Dark-only Material 3 theme; use the named `Broadcast*` / `Tier*` colors from `ui/theme/Color.kt` rather
  than raw `Color(0x…)` in screens. `PesRotationTheme` is the only theme entry point.
- Team crests are `R.drawable.team_<id_with_underscores>`; render them via `TeamLogo`, which crops flags
  full-bleed and pads club crests based on `team.category` (`"CLB"` = club, anything else = national team).
- Adding a team means: a `Team(...)` row in `teams`, a drawable, and a row in `ASSET_SOURCES.md`
  (crest/flag provenance is tracked there). `TeamRotationTest.roster_containsThirtyUniqueTeams` asserts the
  roster size and that ids/drawables are unique — update it when the roster changes.
- Tuning constants are top-level `const val`s at the top of their file — private when only that file needs
  them (`MaxTier3Teams`, `MinBalancedPoolSize`, `MinTeamCount`/`MaxTeamCount`, `RotationLoadingMillis`),
  public when tests or other files read them (`MaxHistoryEntries`, `PresetLongPressMillis`).
