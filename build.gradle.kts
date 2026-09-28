import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.changelog")
    id("org.jetbrains.intellij.platform")
}

dependencies {
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.commonmark:commonmark:0.27.1")
    testImplementation(libs.junit)

    intellijPlatform {
        intellijIdea("2025.3.6.1")
        testFramework(TestFrameworkType.Platform)
    }
}