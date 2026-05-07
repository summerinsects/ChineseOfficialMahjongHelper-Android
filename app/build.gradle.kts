plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "net.tziakcha.chineseofficialmahjonghelper"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.tziakcha.chineseofficialmahjonghelper"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "2.0.1"

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
            }
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
}