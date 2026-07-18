# FuelMate release ProGuard / R8 rules (full mode)
# Goal: smallest APK + best runtime performance while keeping the app functional.

# ---------------------------------------------------------------------------
# Kotlin / Coroutines
# ---------------------------------------------------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keep class kotlin.coroutines.Continuation
-dontwarn kotlinx.coroutines.**

# ---------------------------------------------------------------------------
# Room (entities, DAOs and the generated impls must be retained)
# ---------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.TypeConverter
-keep class com.example.fuelmate.data.local.entity.** { *; }
-keep class com.example.fuelmate.data.local.dao.** { *; }
# Keep Room's generated query result adapters / converters.
-keep class * implements androidx.room.TypeConverter
-dontwarn androidx.room.**

# ---------------------------------------------------------------------------
# Koin (dependency injection graph is built reflectively)
# ---------------------------------------------------------------------------
-keep class com.example.fuelmate.di.** { *; }
-keep class com.example.fuelmate.ui.viewmodel.** { *; }
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# ---------------------------------------------------------------------------
# Jetpack Compose (keep Compose runtime intrinsics; R8 full mode is stricter)
# ---------------------------------------------------------------------------
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# ---------------------------------------------------------------------------
# Navigation / Lifecycle (keep ViewModel classes referenced by Koin)
# ---------------------------------------------------------------------------
-keep class * extends androidx.lifecycle.ViewModel { *; }
-dontwarn androidx.lifecycle.**

# ---------------------------------------------------------------------------
# General
# ---------------------------------------------------------------------------
# Keep data classes used for JSON/state if any serialization is added later.
-keep class com.example.fuelmate.data.model.** { *; }

# Remove logging in release for size + a small perf win.
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
