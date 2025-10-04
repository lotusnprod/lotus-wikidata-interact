pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        kotlin("jvm") version "2.2.20"
        kotlin("plugin.serialization") version "2.2.20"
        id("com.github.ben-manes.versions") version "0.52.0"
        id("org.jmailen.kotlinter") version "5.2.0"
        id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
        id("org.springframework.boot") version "3.5.4"
        id("io.spring.dependency-management") version "1.1.7"
        id("org.jetbrains.kotlin.plugin.spring") version "2.2.20"
    }
}

rootProject.name = "wikidatainteract"

include("wdkt")
include("uploadLotus")
include("importPublication")
include("downloadLotus")
include("konnector")
