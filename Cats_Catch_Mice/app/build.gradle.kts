plugins {
    alias(libs.plugins.android.application)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.cats_catch_mice"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cats_catch_mice"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.google.android.gms:play-services-maps:19.0.0")

    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-database")

    implementation("com.google.zxing:core:3.4.1")
    implementation("com.journeyapps:zxing-android-embedded:4.1.0")

