import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "software.amazon.location.sample"
    compileSdk = 34
    val customConfig = Properties()
    customConfig.load(project.rootProject.file("custom.properties").inputStream())
    defaultConfig {
        applicationId = "software.amazon.location.sample"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "MQTT_END_POINT", "\"${customConfig.getProperty("MQTT_END_POINT")}\"")
        buildConfigField("String", "POLICY_NAME", "\"${customConfig.getProperty("POLICY_NAME")}\"")
        buildConfigField("String", "GEOFENCE_COLLECTION_NAME", "\"${customConfig.getProperty("GEOFENCE_COLLECTION_NAME")}\"")
        buildConfigField("String", "TOPIC_TRACKER", "\"${customConfig.getProperty("TOPIC_TRACKER")}\"")
        buildConfigField("String", "DEFAULT_TRACKER_NAME", "\"${customConfig.getProperty("DEFAULT_TRACKER_NAME")}\"")
        buildConfigField("String", "TEST_POOL_ID", "\"${customConfig.getProperty("TEST_POOL_ID")}\"")
        buildConfigField("String", "TEST_MAP_NAME", "\"${customConfig.getProperty("TEST_MAP_NAME")}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("org.maplibre.gl:android-sdk:11.0.0-pre5")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("aws.sdk.kotlin:cognitoidentity:1.2.21")
    implementation("com.amazonaws:aws-iot-device-sdk-java:1.3.9")
    implementation("aws.sdk.kotlin:iot:1.2.28")
    implementation("aws.sdk.kotlin:location:1.2.21")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.5")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.5")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    if (findProject(":authSdk") != null) {
        implementation(project(mapOf("path" to ":authSdk")))
    } else {
        implementation("software.amazon.location:auth:0.0.1")
    }
    if (findProject(":trackingSdk") != null) {
        implementation(project(mapOf("path" to ":trackingSdk")))
    } else {
        implementation("software.amazon.location:tracking:0.0.1")
    }
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
