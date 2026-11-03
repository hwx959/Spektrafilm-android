// Spektrafilm for Android — engine:spektra-core. GPLv3.
// Android library wrapping the native spektrafilm engine (libspektra.so, built via
// CMake/NDK) plus the Kotlin facade (SpektraEngine / SpektraParams).
plugins {
    alias(libs.plugins.android.library)
}

android {
    // 预编译原生库模式：不编译 CMake，直接打包官方 APK 中同版本的 .so
    packaging {
        jniLibs { keepDebugSymbols += "**/*.so" }
    }

    namespace = "com.spectrafilm.engine"
    compileSdk = 36

    // NDK r28c (28.2.13676358): 16 KB page-size support is the default (r27 introduced it), so
    // native LOAD segments are 16 KB-aligned and
    // ships a 16 KB-aligned libc++_shared.so — required for Android 15's 16 KB page
    // devices. The CMake link flag (CMakeLists.txt) pins the alignment explicitly too.
    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "com.spectrafilm.engine.EngineBoundaryInstrumentation"
    }


    // C++ sources test code lives under src/main/cpp/tests but is host-only
    // (standalone g++ host parity tests); it is not part of the Android library.

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    testImplementation(libs.junit)
}
