import java.io.File
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.process.CommandLineArgumentProvider

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

private class RoomSchemaArgProvider(
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val schemaDir: File,
) : CommandLineArgumentProvider {
    override fun asArguments(): Iterable<String> =
        listOf("room.schemaLocation=${schemaDir.path}")
}

val roomSchemaDir = File(projectDir, "schemas").apply { mkdirs() }
ksp {
    arg(RoomSchemaArgProvider(roomSchemaDir))
}

val buildNumber = System.getenv("GITHUB_RUN_NUMBER")
    ?.toIntOrNull()
    ?.takeIf { it > 0 }
    ?: 1

// TMDB credentials are public-client values once embedded in an APK. Keep the
// source and debug builds keyless; release CI may inject the publisher value.
val tmdbReadAccessToken = System.getenv("PLAYER_TMDB_READ_ACCESS_TOKEN")?.trim().orEmpty()
if (tmdbReadAccessToken.length > 2_048 ||
    (tmdbReadAccessToken.isNotEmpty() && !Regex("[A-Za-z0-9._~+/\\-]+=*").matches(tmdbReadAccessToken))) {
    throw GradleException("PLAYER_TMDB_READ_ACCESS_TOKEN is malformed; value was not logged.")
}

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
        buildConfigField("String", "TMDB_READ_ACCESS_TOKEN", "\"$tmdbReadAccessToken\"")
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
        buildConfig = true
    }

    sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")
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
    implementation("io.coil-kt.coil3:coil-compose:3.6.3")

    implementation("dev.chrisbanes.haze:haze:2.1.0")
    implementation("dev.chrisbanes.haze:haze-glass:2.1.0")
    implementation("dev.chrisbanes.haze:haze-glass-material3:2.1.0")

    implementation("androidx.media3:media3-exoplayer:1.11.1")
    implementation("androidx.media3:media3-ui:1.11.1")

    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")

    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("junit:junit:4.13.2")
}

