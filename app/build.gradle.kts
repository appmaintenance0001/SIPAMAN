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

    // 🔹 CORE UI
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // 🔹 LIST & CARD
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // 🔹 FIREBASE
    implementation("com.google.firebase:firebase-database:20.3.0")

    // 🔹 CLOUDINARY (UPLOAD FOTO)
    implementation("com.cloudinary:cloudinary-android:2.3.1")

    // 🔹 WORK MANAGER (NOTIF BACKGROUND)
    implementation("androidx.work:work-runtime:2.9.0")

    // 🔹 CHART
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // 🔹 TEST
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
}