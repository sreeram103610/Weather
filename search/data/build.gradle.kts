plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.maadlabs.weather.search.data"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    defaultConfig {
        val weatherBaseUrlKeyValue = "WEATHER_BASE_URL" to "\"http://api.openweathermap.org/data/2.5/weather\""
        val weatherAppKeyKeyValue = "WEATHER_APP_KEY" to "\"3caea5d148b0f43ec3f533f18acac961\""

        buildConfigField("String", weatherBaseUrlKeyValue.first, weatherBaseUrlKeyValue.second)
        buildConfigField("String", weatherAppKeyKeyValue.first, weatherAppKeyKeyValue.second)
    }

    buildTypes {

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    kapt {
        correctErrorTypes = true
        showProcessorStats = true
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation(libs.moshi)
    implementation(libs.hilt.android)
    implementation(libs.bundles.retrofit.okhttp)
    implementation(libs.moshi.conv)
    implementation(libs.datastore)
    implementation(libs.androidx.test.junit)
    implementation(libs.androidx.test)
    implementation(libs.googleplay)

    kapt(libs.hilt.compiler)
    kapt(libs.moshi.codegen)

    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotlin.coroutines)
    testImplementation(libs.mockito)
    testImplementation(libs.truth)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}