plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.teskola.molkky"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.teskola.molkky"
        minSdk = 21
        targetSdk = 34
        versionCode = 4
        versionName = "1.4"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation (platform("com.google.firebase:firebase-bom:31.2.3"))
    implementation ("com.google.firebase:firebase-auth:21.1.0")
    implementation ("com.google.firebase:firebase-database")
    implementation ("com.firebaseui:firebase-ui-storage:8.0.2")
    implementation ("com.android.support:multidex:1.0.3")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("com.google.code.gson:gson:2.9.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
}