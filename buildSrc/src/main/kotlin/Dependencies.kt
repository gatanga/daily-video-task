import org.gradle.api.JavaVersion

const val kotlinVersion = "1.6.10"
const val gradleVersion = "7.1.1"
val jvmSourceCompatibility = JavaVersion.VERSION_1_8
val jvmTargetCompatibility = JavaVersion.VERSION_1_8
const val javaTarget = "1.8"
const val jUnit5Plugin = "1.8.2.0"
const val hilt = "2.41"

object AndroidSdk {
    const val compileSdk = 31
    const val minSdk = 24
    const val targetSdk = 31
    const val versionCode = 1
    const val versionName = "1.0"
}

object BuildPlugins {

    private object Versions {
        const val navigationSafeArgs = "2.5.0-alpha03"
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:$gradleVersion"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val jUnitPlugin = "de.mannodermaus.gradle.plugins:android-junit5:$jUnit5Plugin"
    const val hiltPlugin = "com.google.dagger:hilt-android-gradle-plugin:$hilt"
    const val kotlinSerializationPlugin = "org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion"
    const val navigationSafeArgsPlugin =
        "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.navigationSafeArgs}"
}

object ProguardFile {
    const val textFile = "proguard-android-optimize.txt"
    const val ruleFile = "proguard-rules.pro"
}

object AndroidLibraries {

    private object Versions {
        const val coreKtx = "1.7.0"
        const val lifecycleRuntimeKtx = "2.4.1"
        const val dataStore = "1.0.0"
        const val accompanistNavigationAnimation = "0.23.1"
        const val appCompat = "1.4.1"
    }

    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val lifecycleRuntimeKtx =
        "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycleRuntimeKtx}"
    const val dataStore = "androidx.datastore:datastore:${Versions.dataStore}"
    const val dataStorePreferences =
        "androidx.datastore:datastore-preferences:${Versions.dataStore}"
    const val accompanistNavigationAnimation =
        "com.google.accompanist:accompanist-navigation-animation:${Versions.accompanistNavigationAnimation}"
    const val appCompat =
        "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val appCompatResources =
        "androidx.appcompat:appcompat-resources:${Versions.appCompat}"
}

object OtherLibraries {

    private object Versions {
        const val coroutines = "1.6.0"
        const val retrofit = "2.9.0"
        const val retrofitKotlinSerializationConverter = "0.8.0"
        const val okhttp = "4.9.3"
        const val kotlinSerialization = "1.2.2"
        const val loggingInterceptor = "4.9.3"
        const val mediasoupAndroid = "3.0.8-beta-3"
        const val materialDialog = "3.3.0"
    }

    const val coroutinesAndroid =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    const val coroutinesCore =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val ktxRetrofitConverter =
        "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:${Versions.retrofitKotlinSerializationConverter}"
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val kotlinSerialization =
        "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinSerialization}"
    const val loggingInterceptor =
        "com.squareup.okhttp3:logging-interceptor:${Versions.loggingInterceptor}"
    const val mediasoupAndroid =
        "org.mediasoup.droid:mediasoup-client:${Versions.mediasoupAndroid}"
    const val materialDialog = "com.afollestad.material-dialogs:core:${Versions.materialDialog}"
}

object TestingLibraries {

    private object Versions {
        const val jUnit5 = "5.8.2"
        const val androidJUnit = "1.1.3"
        const val espresso = "3.4.0"
        const val mockitoCore = "4.4.0"
        const val mockitoKotlin = "4.0.0"
        const val hiltAndroidTesting = "2.38.1"
    }

    const val jUnit5Api = "org.junit.jupiter:junit-jupiter-api:${Versions.jUnit5}"
    const val jUnit5Engine = "org.junit.jupiter:junit-jupiter-engine:${Versions.jUnit5}"
    const val jUnit5Params = "org.junit.jupiter:junit-jupiter-params:${Versions.jUnit5}"
    const val androidJUnit = "androidx.test.ext:junit:${Versions.androidJUnit}"
    const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    const val mockitoCore = "org.mockito:mockito-core:${Versions.mockitoCore}"
    const val mockitoCoreInline = "org.mockito:mockito-inline:${Versions.mockitoCore}"
    const val mockitoKotlin = "org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}"
    const val hiltAndroidTesting =
        "com.google.dagger:hilt-android-testing:${Versions.hiltAndroidTesting}"
    const val hiltAndroidTestingCompiler =
        "com.google.dagger:hilt-android-compiler:${Versions.hiltAndroidTesting}"
}