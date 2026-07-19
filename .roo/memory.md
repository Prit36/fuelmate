# Project Memory — FuelMate (common pitfalls & conventions)

## Build commands (Windows / Android Studio JBR)
- Always set JAVA_HOME to Android Studio's JBR (do NOT install a separate JDK):
  `set "JAVA_HOME=C:\Program Files\Android\Studio\jbr"` (quoted, no trailing space).
- Gradle builds can take a long time on first compile and appear "stuck" in the tool.
  **Fix:** run detached so it doesn't block the terminal, then poll the log:
  `set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr" && start /B cmd /C ".\gradlew.bat :app:assembleDebug --stacktrace > build-log.txt 2>&1"`
  Then check with: `findstr /R "FAILED SUCCESSFUL BUILD" build-log.txt` and `findstr /R "^e: " build-log.txt`.
- Release build (`assembleRelease`) needs the local `compose-group-mapping:2.2.10` workaround
  in `~/.m2/repository` (AGP 9.3.0 hardcodes a request for a never-published artifact).
  `settings.gradle.kts` has `mavenLocal()` to resolve it. Keep that workaround.

## Kotlin `combine` overload limit
- `kotlinx.coroutines.flow.combine` only has overloads for **up to 5** flows.
  Combining more than 5 sources yields `Array<Any?>` inference errors.
  **Fix:** fold extra flows into one combined flow first, then combine (≤5 total).

## Versioning before commit (DO NOT FORGET)
- Before committing feature work, always:
  1. Bump `VERSION` (semver: feature = minor bump).
  2. Add a section to `CHANGELOG.md` (dated today, under the current version).
  3. Update `versionCode`/`versionName` in `app/build.gradle.kts` if releasing.
- Use `scripts\bump-version.bat minor "summary"` when possible — but note it OVERWRITES
  CHANGELOG.md keeping only [1.0.0], so for multi-section history edit CHANGELOG.md manually
  and just bump VERSION + app/build.gradle.kts, then commit.

## Conventions
- Room is on `androidx.room` 2.8.4 (Room 3 requires KMP — abandoned).
- KSP plugin version must match Kotlin: Kotlin 2.3.21 → KSP 2.3.10.
- IDE "Unresolved reference: androidx/compose" errors after edits are usually stale Gradle
  index false-positives; verify with a real Gradle build, not the editor.
