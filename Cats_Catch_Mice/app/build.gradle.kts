plugins {

    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.cats_catch_mice"
    compileSdk = 31

    defaultConfig {
        applicationId = "com.example.cats_catch_mice"
        minSdk = 28
        targetSdk = 31
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

    // 添加 packagingOptions 以排除冲突文件
    packagingOptions {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
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
    // AndroidX 基础库依赖，选择与 API 30 兼容的版本
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("androidx.navigation:navigation-fragment:2.3.5")
    implementation("androidx.navigation:navigation-ui:2.3.5")

    // Material Components 依赖，修复主题问题
    implementation("com.google.android.material:material:1.3.0")

    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation(libs.play.services.location)


    // 单元测试和 Android Instrumentation Test 依赖
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:28.0.1"))
    implementation("com.google.firebase:firebase-database")

    // ZXing Android Embedded 库
    implementation("com.journeyapps:zxing-android-embedded:4.1.0")
    implementation("com.google.zxing:core:3.4.1")

    // 二维码依赖
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.journeyapps:zxing-android-embedded:4.1.0")
    androidTestImplementation(libs.junit.jupiter)
}
