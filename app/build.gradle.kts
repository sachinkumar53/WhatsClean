plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.dagger.hilt)
    id("androidx.navigation.safeargs.kotlin")

}

android {
    compileSdk = 34
    namespace = "com.sachin.app.whatsclean"

    defaultConfig {
        applicationId = "com.sachin.app.whatsclean"
        minSdk = 21
        targetSdk = 34
        versionCode = 29
        versionName = "2.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
//    implementation("androidx.legacy:legacy-support-v4:1.0.0")
//    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation(libs.androidx.preference.ktx)
    implementation(project(":glide_loaders"))

//    implementation("androidx.activity:activity-ktx:1.9.0")
//    implementation("androidx.fragment:fragment-ktx:1.8.1")

    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Navigation component
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
//    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")

    // Dagger hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Datastore
    implementation(libs.datastore.preferences)

    // View binding delegate
    implementation(libs.viewbindingdelegate)

    implementation(libs.androidx.swiperefreshlayout)

    // Glide
    implementation(libs.glide)
    ksp(libs.glide.ksp)

    implementation(libs.smoothbottombar)
    //Drag select recyclerview
    implementation(libs.drag.select.recyclerview)

    implementation(libs.photoview)

    implementation(libs.work.runtime.ktx)
    //implementation "androidx.paging:paging-runtime:$paging_version"
    implementation(libs.biometric)
    implementation(libs.androidx.biometric.ktx)

    implementation(libs.files)
    implementation(libs.autodispose)
    implementation(libs.observablecollections)
    implementation(libs.shimmer)

}