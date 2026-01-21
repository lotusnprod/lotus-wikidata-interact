pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        val kotlinVersion: String by settings
        val kotlinterVersion: String by settings
        val versionsPluginVersion: String by settings
        val detektVersion: String by settings
        val ktlintVersion: String by settings
        val springBootVersion: String by settings
        val springDependenciesVersion: String by settings
        val springPluginVersion: String by settings

        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("com.github.ben-manes.versions") version versionsPluginVersion
        id("io.gitlab.arturbosch.detekt") version detektVersion
        id("org.jmailen.kotlinter") version kotlinterVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependenciesVersion
        id("org.jetbrains.kotlin.plugin.spring") version springPluginVersion
    }
}

rootProject.name = "wikidatainteract"

include(
    "wdkt",
    "uploadLotus",
    "importPublication",
    "downloadLotus",
    "konnector",
)
