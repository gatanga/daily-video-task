buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    dependencies {
        classpath(BuildPlugins.androidGradlePlugin)
        classpath(BuildPlugins.hiltPlugin)
        classpath(BuildPlugins.kotlinGradlePlugin)
        classpath(BuildPlugins.jUnitPlugin)
        classpath(BuildPlugins.navigationSafeArgsPlugin)
        classpath(BuildPlugins.kotlinSerializationPlugin)
    }
} // Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application") version gradleVersion apply false
    id("com.android.library") version "7.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.6.21" apply false
}

tasks {
    val clear by registering(Delete::class) {
        delete(buildDir)
    }
}