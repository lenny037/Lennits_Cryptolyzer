plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace   = "com.lennit.cryptolyzer"
    compileSdk  = 35

    defaultConfig {
        applicationId   = "com.lennit.cryptolyzer"
        minSdk          = 26          // Android 8 — covers S20 FE (Android 13)
        targetSdk       = 35
        versionCode     = 200
        versionName     = "2.0.0"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")  // S20 FE = arm64
        }

        buildConfigField("String", "BROCHURE_URL", "\"https://lennitcryptolyzer.com\"")
    }

    signingConfigs {
        create("release") {
            // Fill from environment or keystore.properties in CI
        }
    }

    buildTypes {
        release {
            isMinifyEnabled      = true
            isShrinkResources    = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix  = ".debug"
            isDebuggable         = true
        }
    }

    buildFeatures {
        compose      = true
        buildConfig  = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    ndkVersion = "27.0.12077973"
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.navigation.compose)
    implementation(libs.datastore.preferences)
    implementation(libs.security.crypto)
    implementation(libs.biometric)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.coroutines.android)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    debugImplementation("androidx.compose.ui:ui-tooling")
}
