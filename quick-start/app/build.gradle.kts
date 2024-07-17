import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.amazon.androidquickstartapp"
    compileSdk = 34
    val customConfig = Properties()
    customConfig.load(project.rootProject.file("custom.properties").inputStream())
    defaultConfig {
        applicationId = "com.amazon.androidquickstartapp"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "IDENTITY_POOL_ID", "\"${customConfig.getProperty("IDENTITY_POOL_ID")}\"")
        buildConfigField("String", "TRACKER_NAME", "\"${customConfig.getProperty("TRACKER_NAME")}\"")
        buildConfigField("String", "REGION", "\"${customConfig.getProperty("REGION")}\"")
        buildConfigField("String", "PLACE_INDEX", "\"${customConfig.getProperty("PLACE_INDEX")}\"")
        buildConfigField("String", "MAP_NAME", "\"${customConfig.getProperty("MAP_NAME")}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.play.services.location)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.org.maplibre.gl)
    implementation(libs.com.squareup.okhttp3)
    implementation(libs.location)
    implementation(libs.auth)
    implementation(libs.tracking)
    testImplementation(libs.mockk)
    testImplementation(libs.mockito.core)
    androidTestImplementation(libs.androidx.uiautomator)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}