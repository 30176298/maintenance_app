import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    //alias(libs.plugins.composeHotReload)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinSerialization)
}

configurations.all {
    resolutionStrategy {
        //Backdate fix
        //force(libs.androidx.savedstate.kmp.get())
        //force(libs.androidx.savedstate.android.get())
    }
}

configurations.all {
    if (name.contains("desktop", ignoreCase = true) || name.contains("jvm", ignoreCase = true)) {
        resolutionStrategy {
            // Force the JetBrains version specifically for the desktop runtime
            force("org.jetbrains.compose.ui:ui-util:1.8.0")

            // Optionally, substitute the androidx version to prevent it from sneaking in
            dependencySubstitution {
                substitute(module("androidx.compose.ui:ui-util"))
                    .using(module("org.jetbrains.compose.ui:ui-util:1.8.0"))
            }
        }
    }
    else {
        resolutionStrategy {
            force("androidx.compose.ui:ui-util:1.8.0")
        }
    }
}


kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.koin.android)
            implementation(libs.kotlinx.coroutines.android)

            implementation("androidx.compose.ui:ui-util:1.8.0")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            //implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.compose.material.icons.core)

            //implementation(libs.androidx.ui.util)
            //implementation(compose.uiUtil)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            //implementation(libs.androidx.lifecycle.viewmodelCompose)

            //Backdate fix
            //implementation(libs.androidx.savedstate.kmp)
            //implementation(libs.androidx.savedstate.android)

            implementation("org.jetbrains.compose.ui:ui-util:1.8.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.sqldelight.sqlite.driver)

            implementation("org.jetbrains.compose.ui:ui-util:1.8.0") {
                exclude(group = "androidx.compose.ui", module = "ui-util")
            }
        }
    }
}

android {
    namespace = "com.example.theseus"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.theseus"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.example.theseus.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.example.theseus"
            packageVersion = "1.0.0"
        }
    }
}

compose.resources {
    generateResClass = never
}

sqldelight {
    databases {
        create("TheseusDatabase") {
            packageName.set("com.example.theseus.database")
        }
    }
}
