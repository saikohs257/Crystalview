plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.crystalclearview.offercapture"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.crystalclearview.offercapture"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.1.0-capture-fabric-v1"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
