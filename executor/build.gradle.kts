plugins {
    id("com.android.application")
    id("com.chaquo.python")
}
android {
    namespace = "br.com.pythoteca.experimental.executor"
    compileSdk = 35
    defaultConfig {
        applicationId = "br.com.pythoteca.experimental.executor"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "0.0.1-experiment"
        ndk { abiFilters += listOf("arm64-v8a", "x86_64") }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
chaquopy { defaultConfig { version = "3.13" } }
