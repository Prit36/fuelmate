plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.fuelmate"
    compileSdk = 37

    // Release signing config. The keystore is generated once with:
    //   keytool -genkeypair -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias fuelmate
    // Credentials are read from gradle.properties / environment so they are never hard-coded in source.
    signingConfigs {
        create("release") {
            val keystoreFile = rootProject.file("release-key.jks")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = (project.findProperty("RELEASE_STORE_PASSWORD") as? String)
                    ?: System.getenv("RELEASE_STORE_PASSWORD")
                keyAlias = (project.findProperty("RELEASE_KEY_ALIAS") as? String)
                    ?: System.getenv("RELEASE_KEY_ALIAS")
                    ?: "fuelmate"
                keyPassword = (project.findProperty("RELEASE_KEY_PASSWORD") as? String)
                    ?: System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    defaultConfig {
        applicationId = "com.example.fuelmate"
        minSdk = 26
        targetSdk = 37
        versionCode = 4
        versionName = "1.3.0"
    }

    // Keep only English string resources (drops the rest) for a smaller APK.
    androidResources {
        localeFilters += listOf("en")
    }

    buildTypes {
        release {
            // Full R8 optimization: shrink code + resources for the smallest possible APK.
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            // Sign the release APK so it can actually be installed on a device.
            // (An unsigned APK is rejected by Android and will not run.)
            signingConfig = signingConfigs.getByName("release")
            // R8 "full mode" enables more aggressive, performant optimizations.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // AGP 9.0+ ships built-in Kotlin support; configure the compiler via this block.
    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        }
        // Explicit toolchain so the build is reproducible regardless of the
        // locally installed JDK (Java 21 LTS is the modern default).
        jvmToolchain(21)
    }

    buildFeatures {
        compose = true
    }

    // Persist Room schema history so AutoMigrations can be verified at build time.
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose (versions managed by the BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Room (using KSP)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Koin DI
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}
