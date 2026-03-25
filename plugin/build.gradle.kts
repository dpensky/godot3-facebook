plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.godot.facebook"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    compileOnly(libs.godot.lib)
    implementation(libs.facebook.android.sdk)
    
    implementation(libs.androidx.core.ktx)
}

tasks.register<Copy>("exportPlugin") {
    dependsOn("assembleRelease")
    from("build/outputs/aar/facebook-plugin-release.aar")
    from("facebook-plugin.gdap")
    into(rootProject.projectDir.resolve("dist"))
}
