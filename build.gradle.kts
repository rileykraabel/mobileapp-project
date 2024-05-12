buildscript {
    dependencies {
        classpath(libs.google.services)
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    //enable automatic JSON serialization/deserialization
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"

    //enable KSP processor used by Room
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}
