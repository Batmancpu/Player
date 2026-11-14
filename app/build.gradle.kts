plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val buildNumber = System.getenv("GITHUB_RUN_NUMBER")
    ?.toIntOrNull()
    ?.takeIf { it > 0 }
    ?: 1

// Signing credentials exist only in the CI runtime environment.
val releaseKeystorePath = System.getenv("PLAYER_RELEASE_KEYSTORE_PATH")
val releaseKeystorePassword = System.getenv("PLAYER_RELEASE_KEYSTORE_PASSWORD")
val releaseKeyAlias = System.getenv("PLAYER_RELEASE_KEY_ALIAS")
val releaseKeyPassword = System.getenv("PLAYER_RELEASE_KEY_PASSWORD")
val releaseSigningConfigured = listOf(
    releaseKeystorePath,
    releaseKeystorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

android {
    namespace = "com.batmancpu.player"
    compileSdk = 37

    defaultConfig {
        // Keep applicationId stable after the first signed install.
        applicationId = "com.batmancpu.player"
        minSdk = 29
        targetSdk = 37
        versionCode = buildNumber
        versionName = "0.1.$buildNumber"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = file(requireNotNull(releaseKeystorePath))
                storePassword = requireNotNull(releaseKeystorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.09.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation("dev.chrisbanes.haze:haze:2.1.0")
    implementation("dev.chrisbanes.haze:haze-glass:2.1.0")
    implementation("dev.chrisbanes.haze:haze-glass-material3:2.1.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
