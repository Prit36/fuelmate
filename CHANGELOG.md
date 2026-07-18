# Changelog

All notable changes to this project are documented here. This project adheres to
[Semantic Versioning](https://semver.org/) (MAJOR.MINOR.PATCH).

The current version is tracked in the VERSION file. To record a new release, run scripts\bump-version.bat (see that script for usage). Each bump updates this changelog, the VERSION file, and the Android versionCode/versionName in app/build.gradle.kts, then commits and tags the release.

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
