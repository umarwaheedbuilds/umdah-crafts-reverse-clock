import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Release signing key lives outside the repo. Point to it with keystore.properties in the
// project root (git-ignored) or with the UMDAH_KEYSTORE_PROPS environment variable.
val keystoreProps = Properties().apply {
    val path = System.getenv("UMDAH_KEYSTORE_PROPS")
        ?: rootProject.file("keystore.properties").takeIf { it.exists() }?.path
        ?: rootProject.file("../UmdahCrafts-signing/keystore.properties").path
    val f = file(path)
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.umdah.anticlock"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.umdah.anticlock"
        minSdk = 28
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"
    }

    signingConfigs {
        if (keystoreProps.isNotEmpty()) {
            create("release") {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            // Falls back to the debug key when the release key isn't available (e.g. other machines).
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

// No AndroidX / Compose: the app is plain framework code, so the APK stays tiny.
