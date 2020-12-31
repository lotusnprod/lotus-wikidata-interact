pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
        }
    }

    plugins {
        val kotlinVersion: String by settings
        val kotlinterVersion: String by settings
        val versionsPluginVersion: String by settings
        val detektVersion: String by settings
        val ktlintVersion: String by settings

        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("com.github.ben-manes.versions") version versionsPluginVersion
        id("io.gitlab.arturbosch.detekt") version detektVersion
        id("org.jmailen.kotlinter") version kotlinterVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
    }
}

rootProject.name = "wdkt"

include("wdkt")
include("lotus_importer")
include("publication_importer")
