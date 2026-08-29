# PES Rotation

An Android app that randomly draws 1–4 football teams for a PES matchday. Instead of picking uniformly
from the roster, it draws by **tier** with configurable odds, so a session mixes elite and weaker sides
in a controlled way rather than handing everyone Real Madrid.

Built with Kotlin and Jetpack Compose, styled as a dark "broadcast" draw show. The UI is in Vietnamese.

## Features

- **Weighted tier draw** — every team sits in Tier 1, 2, or 3. Each drawn slot rolls a tier using
  weights that default to 30% / 40% / 30%, then picks a random team from that tier.
- **Draw guardrails** — at most 2 Tier 3 teams per draw, and at least one Tier 2 team whenever Tier 2
  is non-empty, so a round never comes out lopsided.
- **Editable tiers and odds** — move any team between tiers and set your own weight split (the three
  weights must total 100). Changes persist across launches and can be reset to defaults. Tier 1 and
  Tier 2 must each keep at least 2 teams, and edits that would break that are rejected.
- **History** — the last 10 draws are stored with timestamps. Each entry records the tier a team had
  *at the time of the draw*, so re-tiering a team later doesn't rewrite past results.
- **30-team roster** — 19 European clubs and 11 national teams, each with its own crest or flag.

## Roster

19 clubs: PSG, Liverpool, Dortmund, Benfica, Bayern Munich, Man City, Man Utd, Tottenham, AC Milan,
AS Roma, Chelsea, Juventus, Atletico Madrid, Napoli, Barcelona, Porto, Real Madrid, Arsenal, Inter Milan.

11 national teams: Argentina, Brazil, Netherlands, Germany, France, England, Belgium, Portugal, Uruguay,
Spain, Italy.

Default tiers split them 10 / 10 / 10. See `ASSET_SOURCES.md` for crest and flag provenance — club crests
are trademarks of their owners, bundled here for identification in a non-commercial app.

## Requirements

- Android Studio (recent stable) or the Gradle wrapper on the command line
- JDK for the Gradle daemon is provisioned automatically (Java 25 toolchain via the foojay resolver)
- Device or emulator running Android 7.0 (API 24) or newer; compiled against API 37

## Build and run

```bash
./gradlew assembleDebug      # build a debug APK -> app/build/outputs/apk/debug/
./gradlew installDebug       # build and install on a connected device
./gradlew test               # JVM unit tests
./gradlew lint               # Android Lint
```

The first invocation downloads Gradle 9.5 and the Java toolchain, so expect it to take a while.

## Tests

Unit tests live in `app/src/test/` and cover the parts worth covering — the rotation algorithm and
the history codec. They run on the JVM with no device needed:

```bash
./gradlew test
./gradlew testDebugUnitTest --tests "com.example.pesrotation.TeamRotationTest"
```

Selection functions take an injectable `kotlin.random.Random`, so tests seed it (`Random(1)`) and assert
on deterministic draws. Instrumented tests in `app/src/androidTest/` need a device:
`./gradlew connectedDebugAndroidTest`.

## Project layout

```
app/src/main/java/com/example/pesrotation/
  MainActivity.kt            single Activity, app state, home screen
  Team.kt                    roster + all draw logic (pure functions)
  RotationHistory.kt         history model and its text codec
  HistoryScreen.kt           past draws
  SettingsScreen.kt          tier editor and weight editor
  TeamLogo.kt                crest/flag rendering
  *Store.kt                  DataStore persistence
  ui/theme/                  colors, typography, Material 3 theme
```

State lives in `MainActivity` as Compose state and is mirrored to three DataStore files (`team_tiers`,
`rotation_history`, `preset_access`). Screens are stateless composables driven by callbacks; there is no
navigation library, ViewModel, or DI framework.

## License

No license is declared. Bundled crests and flags are third-party assets; see `ASSET_SOURCES.md` before
distributing publicly.
