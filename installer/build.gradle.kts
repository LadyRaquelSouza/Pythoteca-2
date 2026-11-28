plugins { id("com.android.application") }
android {
    namespace = "br.com.pythoteca.experimental.installer"
    compileSdk = 35
    defaultConfig {
        applicationId = "br.com.pythoteca.experimental.installer"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "0.0.1-experiment"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    sourceSets.getByName("main").assets.srcDir(layout.buildDirectory.dir("generated/suite-assets"))
}
