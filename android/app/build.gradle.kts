import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// 从 local.properties 读取敏感配置（不在 git 仓库中）
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}
val minimaxBaseUrl: String = localProps.getProperty("MINIMAX_BASE_URL", "https://api.MiniMax.cn/v1")
val minimaxApiKey:  String = localProps.getProperty("MINIMAX_API_KEY",  "")

android {
    namespace  = "com.jueqiao.jianghu"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jueqiao.jianghu"
        minSdk        = 24
        targetSdk     = 35
        versionCode   = 1
        versionName   = "1.0"
        vectorDrawables { useSupportLibrary = true }

        // Minimax API 配置（从 local.properties 注入,生产环境不要再硬编码）
        buildConfigField("String", "MINIMAX_BASE_URL", "\"$minimaxBaseUrl\"")
        buildConfigField("String", "MINIMAX_API_KEY",  "\"$minimaxApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources.excludes += setOf(
            "META-INF/AL2.0",
            "META-INF/LGPL2.1"
        )
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.google.material)
    implementation(libs.coil.compose)
    implementation(libs.okhttp)
    implementation(libs.gson)
    debugImplementation(libs.androidx.compose.ui.tooling)
}