# Changelog

All notable changes to this project are documented here. This project adheres to
[Semantic Versioning](https://semver.org/) (MAJOR.MINOR.PATCH).

The current version is tracked in the VERSION file. To record a new release, run scripts\bump-version.bat (see that script for usage). Each bump updates this changelog, the VERSION file, and the Android versionCode/versionName in app/build.gradle.kts, then commits and tags the release.

## [1.3.0] - 2026-07-19

### Added
- Search, filter, and sort on the fuel history list (VehicleDetailScreen): text search over entry notes, an inclusive date-range filter, and sort by date / odometer / cost (tap a chip to toggle asc/desc). A "X of Y" count shows how many entries match.
- "Same as last fill-up" one-tap quick-fill on the Add/Edit fuel screen: copies liters, amount, and note from the most recent entry (add mode only).

## [1.2.0] - 2026-07-19

### Added
- Backup & restore: portable JSON backup of all vehicles and fuel entries, exported/imported via the system document picker (runs inside a single Room transaction so a partial import leaves data untouched).
- CSV export: per-vehicle fuel records exported to CSV via the document picker (available from the vehicle detail screen and Settings).
- Settings screen with backup, restore, and CSV export actions, plus a Koin-wired `SettingsViewModel`.

## [1.1.1] - 2026-07-18

### Changed
- Screen transitions now use a WhatsApp-style horizontal slide (new screen slides in from the right, old slides out to the left; reversed on back) at the normal 300ms duration, replacing the earlier sped-up 150ms slide and the default fade.

## [1.1.0] - 2026-07-18

### Added
- Vehicle edit functionality (rename/update vehicles via dialog).
- Fuel entry edit functionality (`edit_fuel/{vehicleId}/{entryId}` route prefills all fields).
- Delete confirmation dialogs for both vehicles and fuel entries.
- Mileage trend chart on the vehicle detail screen.
- Loading states on list, detail, and edit screens (no flash of empty UI).
- Faster 150ms screen transition animations.

### Changed
- Modernized Theme with a custom green/amber color scheme, typography, and rounded shapes.
- Redesigned VehicleListScreen (icons, empty state, swipe-to-delete, edit dialog).
- Redesigned VehicleDetailScreen (hero summary card, stat cards, more-menu for edit/delete).
- Redesigned AddFuelEntryScreen with labeled fields and per-field validation errors.

### Fixed
- Vehicle list content was cut off under the TopAppBar (Scaffold padding now applied).
- Edit fuel entry opened a blank "add" form instead of prefilling (entryId now forwarded through Koin).
- Only the odometer field prefilled when editing (all fields now populate correctly).
- Validation errors always highlighted the first field (now highlights the specific invalid field).
- Average mileage hero card background color mismatched the surrounding surface (now a solid blended tone).

## [1.0.1] - 2026-07-18

### Added
- Add version tracking system (VERSION, CHANGELOG, bump script)

## [1.0.0] - 2026-07-18

### Added
- Initial public release of FuelMate (v1).
- Vehicle management: add, list, and view vehicle details.
- Fuel entry tracking per vehicle with date, amount, cost, odometer, and volume.
- Mileage calculation and statistics (StatCard components).
- Local Room database with DAOs and a Kotlin repository layer.
- Dependency injection via a manual DI module (AppModule).
- Jetpack Compose UI with themed screens and navigation.
- Gradle build setup (Android Gradle Plugin, KSP, Compose compiler).

[1.0.0]: https://github.com/Prit36/fuelmate/releases/tag/v1.0.0
