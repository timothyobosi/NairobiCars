// In your project-level build.gradle.kts file

plugins {
    // Applying the Android and Kotlin plugins to submodules
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    // You can also add the Google services plugin here (but it must be declared separately in the buildscript block)

    //Add dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    dependencies {
        // This is where you add classpath dependencies for Gradle plugins
        classpath("com.google.gms:google-services:4.3.15") // Google services plugin
    }
}

allprojects {
    // Remove the repositories block from here
    // repositories { google(); mavenCentral(); }
}
