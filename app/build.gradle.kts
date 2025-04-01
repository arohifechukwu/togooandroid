plugins {
    //alias(libs.plugins.android.application)
    id("com.android.application")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.togoo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.togoo"
        minSdk = 24
        targetSdk = 34
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
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.01.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("com.stripe:stripe-android:20.44.0")
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-auth:21.1.0")
    implementation ("com.google.firebase:firebase-firestore:24.4.1")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("com.google.firebase:firebase-storage:20.1.0")
    implementation ("com.google.firebase:firebase-database:20.0.5")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation(libs.firebase.functions)
    implementation(libs.play.services.location)
    implementation ("androidx.constraintlayout:constraintlayout-compose:1.1.0-alpha12")
    implementation(libs.volley)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

}
