plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.gms.google-services")
}

android {
    namespace = "week11.st339556.finalProject"
    compileSdk = 36

    defaultConfig {
        applicationId = "week11.st339556.finalProject"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation.core.lint)
    implementation(libs.androidx.navigation.compose)
    //implementation(libs.androidx.compose.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime)
    //implementation(libs.firebase.firestore.ktx)
    //implementation(libs.firebase.auth.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-auth-ktx:22.1.1")
    //implementation("com.google.firebase:firebase-auth-ktx:24.0.1")
    //implementation("com.google.firebase:firebase-firestore-ktx")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.4")


}