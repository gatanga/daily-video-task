plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
    kotlin("kapt")
    id("de.mannodermaus.android-junit5")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
}

android {
    compileSdk = AndroidSdk.compileSdk

    defaultConfig {
        applicationId = "co.daily.dailyvideoapp.android"
        minSdk = AndroidSdk.minSdk
        targetSdk = AndroidSdk.targetSdk
        versionCode = AndroidSdk.versionCode
        versionName = AndroidSdk.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile(ProguardFile.textFile),
                ProguardFile.ruleFile
            )
        }
    }
    compileOptions {
        sourceCompatibility = jvmSourceCompatibility
        targetCompatibility = jvmTargetCompatibility
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaTarget
        }
    }
    buildFeatures {
        viewBinding = true
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    /***********************
     ** Android Libraries
     ***********************/
    implementation(AndroidLibraries.coreKtx)
    implementation(AndroidLibraries.lifecycleRuntimeKtx)
    implementation(project(mapOf("path" to ":dailysdk")))
    implementation(AndroidLibraries.dataStore)
    implementation(AndroidLibraries.dataStorePreferences)
    implementation(AndroidLibraries.accompanistNavigationAnimation)
    implementation(AndroidLibraries.appCompat)
    // For loading and tinting drawables on older versions of the platform
    implementation(AndroidLibraries.appCompatResources)
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("com.google.android.material:material:1.6.0")


    /***********************
     ** Other Libraries
     ***********************/
    implementation(OtherLibraries.coroutinesAndroid)
    implementation(OtherLibraries.coroutinesCore)
    implementation(OtherLibraries.retrofit)
    implementation(OtherLibraries.ktxRetrofitConverter)
    implementation(OtherLibraries.okhttp)
    implementation(OtherLibraries.kotlinSerialization)
    implementation(OtherLibraries.loggingInterceptor)
    implementation(OtherLibraries.mediasoupAndroid)

    /***********************
     ** Testing Libraries
     ***********************/
    testImplementation(TestingLibraries.jUnit5Api)
    testRuntimeOnly(TestingLibraries.jUnit5Engine)
    testImplementation(TestingLibraries.jUnit5Params)
    androidTestImplementation(TestingLibraries.androidJUnit)
    androidTestImplementation(TestingLibraries.espresso)
    testImplementation(TestingLibraries.mockitoCore)
    testImplementation(TestingLibraries.mockitoCoreInline)
    testImplementation(TestingLibraries.mockitoKotlin)
    testImplementation(TestingLibraries.hiltAndroidTesting)
    androidTestImplementation(TestingLibraries.hiltAndroidTesting)
    kaptTest(TestingLibraries.hiltAndroidTestingCompiler)
    kaptAndroidTest(TestingLibraries.hiltAndroidTestingCompiler)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}