
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.sipaman.maintenance"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sipaman.maintenance"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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

    // 🔥 WAJIB untuk Android baru (fix 16KB warning)
    packaging {
        jniLibs.useLegacyPackaging = true
        }


    // 🔥 Optional tapi bagus
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")


    implementation("com.cloudinary:cloudinary-android:2.3.1")

    implementation("androidx.work:work-runtime:2.9.0")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("androidx.activity:activity:1.9.0") // 🔥 ganti libs.activity

    implementation("de.hdodenhof:circleimageview:3.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Firebase BoM: mengatur versi library Firebase agar kompatibel
    implementation(platform("com.google.firebase:firebase-bom:35.0.2"))

// Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

// Firebase Realtime Database
    implementation("com.google.firebase:firebase-database")

// Firebase Storage
    implementation("com.google.firebase:firebase-storage")

// Firebase Cloud Messaging
    implementation("com.google.firebase:firebase-messaging")

    // Firebase AI Logic - Gemini
    implementation("com.google.firebase:firebase-ai")

// App Check debug provider untuk pengujian lokal
    implementation("com.google.firebase:firebase-appcheck-debug")

// Kompatibilitas Firebase AI Logic untuk Java
    implementation("com.google.guava:guava:31.0.1-android")
    implementation("org.reactivestreams:reactive-streams:1.0.4")

}

apply(plugin = "com.google.gms.google-services")
