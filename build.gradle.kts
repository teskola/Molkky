// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()  // Google's Maven repository
        mavenCentral()  // Maven Central repository
    }
    dependencies {
        classpath ("com.google.gms:google-services:4.3.15")
    }
}


plugins {
    alias(libs.plugins.android.application) apply false
}