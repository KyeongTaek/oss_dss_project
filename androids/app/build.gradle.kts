import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.finalblescanner"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        properties.load(localPropertiesFile.inputStream())
    }
    val myApiKey = properties.getProperty("API_KEY") ?: ""
    val analysisServerUrl = properties.getProperty("ANALYSIS_SERVER_URL") ?: ""
    val classServerUrl = properties.getProperty("CLASS_SERVER_URL") ?: ""

    buildFeatures {
        buildConfig = true
    }


    defaultConfig {
        applicationId = "com.example.finalblescanner"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Manifest에서 사용할 경우 <meta-data>에 전달할 플레이스홀더 설정
        manifestPlaceholders["API_KEY"] = myApiKey
        buildConfigField("String", "API_KEY", "${myApiKey}")
        buildConfigField("String", "ANALYSIS_SERVER_URL", "${analysisServerUrl}")
        buildConfigField("String", "CLASS_SERVER_URL", "${classServerUrl}")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.kakao.maps.open:android:2.13.2")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
}