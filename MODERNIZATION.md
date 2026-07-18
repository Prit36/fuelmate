# FuelMate — Modern Tech Stack & Code Principles

This document records the modernization applied to the FuelMate Android app so it uses a
current, LTS-backed toolchain and modern Kotlin/Compose best practices.

## Toolchain (all current as of the change)

| Concern            | Before            | After                              | Notes |
|--------------------|-------------------|------------------------------------|-------|
| JVM / Java target  | Java 17           | **Java 21 (LTS)**                  | `compileOptions` + `jvmTarget = JVM_21` + `jvmToolchain(21)` |
| Gradle             | 9.6.1             | 9.6.1                              | Already modern |
| AGP                | 9.3.0             | 9.3.0                              | Already modern |
| Kotlin             | 2.4.10            | 2.4.10                             | Already modern |
| Compose Compiler   | (missing)         | `org.jetbrains.kotlin.plugin.compose` | **Required** for Kotlin 2.x + Compose |
| compileSdk/targetSdk | 35             | **37**                             | Required by latest androidx deps |
| Compose BOM        | 2026.06.01        | 2026.06.01                         | Already modern |
| Room               | 2.8.4             | 2.8.4                              | Already modern |
| Koin               | 4.2.2             | 4.2.2                              | Already modern |
| Coroutines        | 1.10.2            | 1.10.2                             | Already modern |

> Note: the only genuinely "old" item was the **Java 17 JVM target**. Everything else was
> already on recent versions; the remaining work was making the build actually compile on a
> modern toolchain and applying code-principle refactors.

## Code-principle changes

1. **Single reactive stream in `VehicleDetailViewModel`**
   Replaced the `MutableStateFlow<Vehicle?>` + `init { launch { ... } }` pattern with a
   `flow { emit(repository.getVehicle(...)) }` combined via `combine`. No manual `init` launch,
   fully declarative, lifecycle-safe.

2. **Separation of concerns**
   Moved the standalone `formatNum` helper out of `AddFuelEntryViewModel.kt` into
   `ui/util/Formatters.kt` alongside the other formatting functions.

3. **Event-based navigation (`AddFuelEntryScreen`)**
   Replaced `LaunchedEffect(state.saved)` with a `Channel<AddFuelEntryEvent>` exposed as a
   `Flow` from the ViewModel and consumed via `collectAsStateWithLifecycle`. This avoids
   double-trigger / recomposition bugs that a boolean `saved` flag can cause.

4. **Room migration strategy (Room 2.8+)**
   - `AppDatabase` now uses `exportSchema = true` so schema history is persisted under
     `schemas/` and `AutoMigration`s are verified at build time.
   - Declared an `autoMigrations = [...]` block (add entries as the version increases).
   - `AppModule` uses `fallbackToDestructiveMigrationOnDowngrade(dropAllTables = false)`
     instead of the blanket `fallbackToDestructiveMigration()`.

5. **Modern Koin DSL**
   Switched `AppModule` to `org.koin.core.module.dsl.viewModel` / `viewModelOf` (the new
   package), removing the deprecated `org.koin.androidx.viewmodel.dsl.viewModel` imports.

6. **Material 3 dynamic theme**
   Added `ui/theme/Theme.kt` (`FuelMateTheme`) with dynamic color (Android 12+) and a
   `res/values/themes.xml` `Theme.FuelMate` for the window background before Compose takes over.

## Missing files that were created

The project as checked out did not compile (several referenced files were absent). To deliver
a working, modern app the following were added:

- `app/src/main/java/com/example/fuelmate/ui/theme/Theme.kt` — `FuelMateTheme` (dynamic color).
- `app/src/main/java/com/example/fuelmate/ui/screen/VehicleListScreen.kt` — vehicle list UI
  (state hoisting, `collectAsStateWithLifecycle`, Koin ViewModel), referenced by `AppNavHost`.
- `app/src/main/res/values/themes.xml` — `Theme.FuelMate` style referenced by `AndroidManifest.xml`.

## Build / run

The Gradle wrapper scripts were regenerated (`gradlew` / `gradlew.bat`) and a `gradle.properties`
was added with a 2 GB daemon heap (AGP 9 + Kotlin 2.4 need more than the 512 MiB default).

Build (requires a JDK 21, e.g. the one bundled with Android Studio):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
```

`BUILD SUCCESSFUL` confirmed on Java 21 / compileSdk 37 with no deprecation warnings.
