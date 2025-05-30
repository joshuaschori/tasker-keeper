// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.ksp) apply false
}