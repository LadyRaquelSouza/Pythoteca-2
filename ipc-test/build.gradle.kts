plugins { id("com.android.application") }
android {
    namespace = "br.com.pythoteca.experimental.ipctest"
    compileSdk = 35
    defaultConfig {
        applicationId = "br.com.pythoteca.experimental.ipctest"
        minSdk = 29
        targetSdk = 35
        testInstrumentationRunner = "br.com.pythoteca.experimental.ipctest.ProbeTest"
    }
}
dependencies { implementation(project(":protocol")) }
