plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.xevrae.ui"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:media"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.constraintlayout.compose)

    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.navigation.compose)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.kmpalette.core)

    implementation(libs.compottie)
    implementation(libs.compottie.dot)
    implementation(libs.compottie.network)
    implementation(libs.compottie.resources)

    implementation(libs.paging.compose)
    implementation(libs.paging.common)

    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.markdown)
    implementation(libs.haze)
    implementation(libs.haze.materials)
    implementation(libs.liquid.glass)
    implementation(libs.liquid.glass.shapes)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
}
