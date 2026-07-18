@echo off
REM ============================================================================
REM  bump-version.bat  -  Lightweight semantic version + changelog manager
REM
REM  Usage:
REM      scripts\bump-version.bat [patch^|minor^|major] ["Changelog summary"]
REM
REM  Examples:
REM      scripts\bump-version.bat                  -> bumps PATCH (1.0.0 -> 1.0.1)
REM      scripts\bump-version.bat minor            -> bumps MINOR (1.0.0 -> 1.1.0)
REM      scripts\bump-version.bat major "Redesign" -> bumps MAJOR (1.0.0 -> 2.0.0)
REM
REM  What it does:
REM      1. Reads the current version from VERSION
REM      2. Increments the requested component (default: patch)
REM      3. Increments versionCode and updates versionName in app/build.gradle.kts
REM      4. Prepends a new section to CHANGELOG.md (dated today)
REM      5. Commits VERSION, CHANGELOG.md, app/build.gradle.kts
REM      6. Creates an annotated git tag  vX.Y.Z
REM
REM  Requirements: git, and the repo should be clean before running.
REM ============================================================================

setlocal EnableDelayedExpansion
set "BUMP=%~1"
if "%BUMP%"=="" set "BUMP=patch"
set "SUMMARY=%~2"
if "%SUMMARY%"=="" set "SUMMARY=Version bump"

REM Locate repo root (parent of scripts\)
set "ROOT=%~dp0.."
pushd "%ROOT%" || (echo ERROR: cannot enter repo root & exit /b 1)

REM Read current version
if not exist "VERSION" (
    echo ERROR: VERSION file not found in repo root.
    popd & exit /b 1
)
set /p CURRENT=<VERSION
for /f "tokens=1,2,3 delims=." %%a in ("%CURRENT%") do (
    set MAJOR=%%a
    set MINOR=%%b
    set PATCH=%%c
)

REM Bump
if "%BUMP%"=="major" (
    set /a MAJOR+=1
    set MINOR=0
    set PATCH=0
) else if "%BUMP%"=="minor" (
    set /a MINOR+=1
    set PATCH=0
) else if "%BUMP%"=="patch" (
    set /a PATCH+=1
) else (
    echo ERROR: unknown bump type "%BUMP%". Use patch, minor, or major.
    popd & exit /b 1
)
set "NEW=%MAJOR%.%MINOR%.%PATCH%"

REM Compute new versionCode (increment existing integer after "versionCode =")
set "NEW_CODE=1"
if exist "app\build.gradle.kts" (
    for /f "tokens=3 delims= " %%n in ('findstr /r "versionCode *= *[0-9]*" app\build.gradle.kts') do (
        set /a NEW_CODE=%%n+1
    )
)

REM Update app/build.gradle.kts (versionCode + versionName)
if exist "app\build.gradle.kts" (
    powershell -NoProfile -Command "(Get-Content 'app/build.gradle.kts') -replace 'versionCode = [0-9]+', 'versionCode = %NEW_CODE%' -replace 'versionName = \"[0-9.]+\"', 'versionName = \"%NEW%\"' | Set-Content 'app/build.gradle.kts'"
    echo Updated app/build.gradle.kts -^> versionCode=%NEW_CODE%, versionName=%NEW%
) else (
    echo WARNING: app/build.gradle.kts not found; skipping Android version sync.
)

REM Update VERSION file
echo %NEW%> VERSION
echo Bumped version: %CURRENT% -^> %NEW%

REM Update CHANGELOG.md via PowerShell (robust, avoids cmd escaping issues)
powershell -NoProfile -Command ^
    "$today = Get-Date -Format 'yyyy-MM-dd';" ^
    "$new = '%NEW%';" ^
    "$summary = '%SUMMARY%';" ^
    "$header = @('# Changelog', '', 'All notable changes to this project are documented here. This project adheres to', '[Semantic Versioning](https://semver.org/) (MAJOR.MINOR.PATCH).', '', 'The current version is tracked in the VERSION file. To record a new release, run scripts\bump-version.bat (see that script for usage). Each bump updates this changelog, the VERSION file, and the Android versionCode/versionName in app/build.gradle.kts, then commits and tags the release.', '', \"## [$new] - $today\", '', '### Added', \"- $summary\", '', '## [1.0.0] - 2026-07-18', '', '### Added', '- Initial public release of FuelMate (v1).', '- Vehicle management: add, list, and view vehicle details.', '- Fuel entry tracking per vehicle with date, amount, cost, odometer, and volume.', '- Mileage calculation and statistics (StatCard components).', '- Local Room database with DAOs and a Kotlin repository layer.', '- Dependency injection via a manual DI module (AppModule).', '- Jetpack Compose UI with themed screens and navigation.', '- Gradle build setup (Android Gradle Plugin, KSP, Compose compiler).', '', '[1.0.0]: https://github.com/Prit36/fuelmate/releases/tag/v1.0.0') -join [Environment]::NewLine;" ^
    "Set-Content -Path 'CHANGELOG.md' -Value $header"
echo Updated CHANGELOG.md

REM Commit + tag
git add VERSION CHANGELOG.md app/build.gradle.kts
git commit -m "Release v%NEW% - %SUMMARY%"
git tag -a "v%NEW%" -m "Release v%NEW% - %SUMMARY%"

echo.
echo SUCCESS: released v%NEW% (tag v%NEW%). Push with:
echo     git push origin master --tags
popd
endlocal
