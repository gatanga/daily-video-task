plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
    kotlin("kapt")
    id("de.mannodermaus.android-junit5")
}

android {
    compileSdk = AndroidSdk.compileSdk

    defaultConfig {
        minSdk = AndroidSdk.minSdk
        targetSdk = AndroidSdk.targetSdk
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    /**
     * In an actual project, api urls should not be saved here. The same goes with app secrets.
     * They should be in a `local.properties` or `gradle.properties` file or any other file that never gets committed for local dev-ing.
     * For CI pipelines, these values should be pulled from the CI environment.
     * For now, putting an API url is still a bit more secure than putting it in a class or in a string resource
     */
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile(ProguardFile.textFile),
                ProguardFile.ruleFile
            )
            resValue("string", "API_URL", "http://51.210.178.142:3000/signaling/")
//            resValue("string", "API_URL", "https://daily.radu.app:3000/signaling/")
        }
        debug {
            resValue("string", "API_URL", "http://51.210.178.142:3000/signaling/")
//            resValue("string", "API_URL", "https://daily.radu.app:3000/signaling/")
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
    implementation(AndroidLibraries.dataStore)
    implementation(AndroidLibraries.dataStorePreferences)
    implementation(AndroidLibraries.accompanistNavigationAnimation)
    implementation(AndroidLibraries.appCompat)
    // For loading and tinting drawables on older versions of the platform
    implementation(AndroidLibraries.appCompatResources)
    implementation("androidx.work:work-runtime-ktx:2.7.1")

    /***********************
     ** Other Libraries
     ***********************/
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

//    implementation("co.daily:client:0.1.1")
//    implementation("com.twilio:video-android:7.1.0")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}