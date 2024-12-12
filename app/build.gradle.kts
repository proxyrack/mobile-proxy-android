plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hiltAndroid)
    kotlin("kapt")

    id("io.sentry.android.gradle") version "4.14.0"
}

android {
    namespace = "com.proxyrack.control"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.proxyrack.control"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = System.getenv("KEY_ALIAS") ?: "defaultAlias"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "defaultKeyPassword"
            storeFile = file(System.getenv("KEYSTORE_FILE_PATH") ?: "/path/to/default/keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "defaultStorePassword"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "SERVER_IP", "\"mobile-socket.culturegps.com\"")
            buildConfigField("String", "SENTRY_DSN", "\"https://8bbb406627a4f1fd65da8bf730383142@o58319.ingest.us.sentry.io/4508366264008704\"") // for access in code
            manifestPlaceholders["SENTRY_DSN"] = "https://8bbb406627a4f1fd65da8bf730383142@o58319.ingest.us.sentry.io/4508366264008704" // for access in manifest
        }
        debug {
            //applicationIdSuffix = ".debug"
            isDebuggable = true
            buildConfigField("String", "SERVER_IP", "\"95.216.229.111\"")
            buildConfigField("String", "SENTRY_DSN", "\"\"") // Empty DSN for debug builds so that we don't send events to sentry
            manifestPlaceholders["SENTRY_DSN"] = "" // Empty DSN for debug builds
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(files("libs/android_lib_emulator.aar"))
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.posthog.android)
    implementation(libs.retrofit2.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    kapt(libs.hilt.compiler)
    androidTestImplementation(libs.hilt.android.testing) // For instrumentation tests
    kaptAndroidTest(libs.hilt.compiler)
    testImplementation(libs.hilt.android.testing) // For local unit tests
    kaptTest(libs.hilt.compiler)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}

sentry {
    org.set("proxyrack")
    projectName.set("android-peer-client")

    // this will upload your source code to Sentry to show it as part of the stack traces
    // disable if you don't want to expose your sources
    includeSourceContext.set(true)
    authToken = System.getenv("SENTRY_AUTH_TOKEN")
}
