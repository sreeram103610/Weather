import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

allprojects {
    android {
        buildFeatures {
            compose = true
        }
        composeOptions {
            kotlinCompilerExtensionVersion = "1.4.7"
        }
    }
}

android {
    compileSdk = 33
    namespace = "com.maadlabs.weather"
    defaultConfig {
        applicationId = "com.maadlabs.weather"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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


    fun Packaging.() {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")

            resources.excludes.add("META-INF/*")
        }
    }
}


dependencies {
    implementation(libs.androidx.ktx)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
    implementation("androidx.compose.ui:ui-graphics")
    implementation(libs.bundles.compose)
    testImplementation(libs.junit)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(libs.compose.test)
    debugImplementation(libs.compose.debug.test)
    debugImplementation(libs.compose.manifest.test)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}

