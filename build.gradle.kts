buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false //ksp-1.8.10-1.0.9 is too old for kotlin-1.9.0. Please upgrade ksp or downgrade kotlin-gradle-plugin to 1.8.10.
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false
}