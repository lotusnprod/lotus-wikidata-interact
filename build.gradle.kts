import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

val localPropertiesFile = file("local.properties")
val localProperties =
    if (localPropertiesFile.exists()) {
        val props = Properties()
        props.load(localPropertiesFile.inputStream())
        props
    } else {
        null
    }

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    id("com.github.ben-manes.versions")
    id("io.gitlab.arturbosch.detekt")
    id("org.jmailen.kotlinter")
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
    kotlin("plugin.spring")
    `java-library`
}

allprojects {
    group = "net.nprod.lotus.wikidata"
    version = "0.4-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://jitpack.io")

        localProperties?.let {
            val localMaven: String by it
            maven(uri("file:///$localMaven"))
        }
    }
}

subprojects {
    val javaVersion = "17"

    apply {
        plugin("com.github.ben-manes.versions")
        plugin("org.jmailen.kotlinter")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.serialization")
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        }
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        useJUnitPlatform {
            if (!System.getProperty("includeIntegrationTests", "false").toBoolean()) {
                excludeTags("integration")
            }
        }
        testLogging {
            events("PASSED", "FAILED", "SKIPPED")
            warn.events("PASSED", "FAILED", "SKIPPED", "STANDARD_ERROR")
            info.events("PASSED", "FAILED", "SKIPPED", "STANDARD_ERROR", "STANDARD_OUT")
            debug.events("PASSED", "FAILED", "SKIPPED", "STANDARD_ERROR", "STANDARD_OUT", "STARTED")
        }
    }

    tasks.withType<JavaExec> {
        jvmArgs = listOf("-Xms16g", "-Xmx24g")
    }

    tasks.withType<org.jmailen.gradle.kotlinter.tasks.LintTask>().configureEach {
        exclude("build/generated/**")
        exclude("**/build/generated/**")
    }
}

project(":wdkt") {
    dependencies {
        val kotlinxCliVersion: String by project
        val cdkVersion: String by project
        val wdtkVersion: String by project
        val rdf4jVersion: String by project
        val log4jVersion: String by project
        val junitApiVersion: String by project
        val ktorVersion: String by project
        val serializationVersion: String by project
        val kotlinVersion: String by project

        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-cli:$kotlinxCliVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

        implementation("io.ktor:ktor-client-core:$ktorVersion")
        implementation("io.ktor:ktor-client-cio:$ktorVersion")
        implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
        implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")

        implementation("org.wikidata.wdtk:wdtk-dumpfiles:$wdtkVersion") { exclude("org.slf4j", "slf4j-api") }
        implementation("org.wikidata.wdtk:wdtk-wikibaseapi:$wdtkVersion") { exclude("org.slf4j", "slf4j-api") }
        api("org.wikidata.wdtk:wdtk-datamodel:$wdtkVersion") { exclude("org.slf4j", "slf4j-api") }
        implementation("org.wikidata.wdtk:wdtk-rdf:$wdtkVersion") { exclude("org.slf4j", "slf4j-api") }

        implementation("org.openscience.cdk:cdk-depict:$cdkVersion")
        implementation("org.openscience.cdk:cdk-formula:$cdkVersion")
        implementation("org.openscience.cdk:cdk-silent:$cdkVersion")
        implementation("org.openscience.cdk:cdk-smiles:$cdkVersion")

        implementation("org.eclipse.rdf4j:rdf4j-client:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-core:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-repository-sail:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-sail-memory:$rdf4jVersion")

        implementation(project(":konnector"))

        testImplementation(platform("org.junit:junit-bom:$junitApiVersion"))
        testImplementation(kotlin("test-junit5"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }
}

project(":uploadLotus") {
    apply {
        plugin("org.jetbrains.kotlin.plugin.serialization")
        plugin("application")
    }

    application {
        mainClass.set("net.nprod.lotus.wikidata.upload.MainKt")
    }

    dependencies {
        val kotlinxCliVersion: String by project
        val cdkVersion: String by project
        val wdtkVersion: String by project
        val rdf4jVersion: String by project
        val log4jVersion: String by project
        val junitApiVersion: String by project
        val ktorVersion: String by project
        val serializationVersion: String by project
        val kotlinVersion: String by project
        val univocityParserVersion: String by project
        val sqliteJdbcVersion: String by project

        implementation(project(":wdkt"))

        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-cli:$kotlinxCliVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

        implementation("io.ktor:ktor-client-cio:$ktorVersion")
        implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
        implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")

        implementation("com.univocity:univocity-parsers:$univocityParserVersion")
        implementation("org.xerial:sqlite-jdbc:$sqliteJdbcVersion")

        implementation("org.wikidata.wdtk:wdtk-dumpfiles:$wdtkVersion") { exclude("org.slf4j", "slf4j-api") }
        implementation("org.wikidata.wdtk:wdtk-wikibaseapi:$wdtkVersion") { exclude("org.slf4j", "slf4j-api") }
        implementation("org.wikidata.wdtk:wdtk-datamodel:$wdtkVersion") { exclude("org.slf4j", "slf4j-api") }
        implementation("org.wikidata.wdtk:wdtk-rdf:$wdtkVersion") { exclude("org.slf4j", "slf4j-api") }

        implementation("org.openscience.cdk:cdk-silent:$cdkVersion")
        implementation("org.openscience.cdk:cdk-smiles:$cdkVersion")

        implementation("org.eclipse.rdf4j:rdf4j-client:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-core:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-repository-sail:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-sail-memory:$rdf4jVersion")

        implementation(project(":konnector"))

        testImplementation(platform("org.junit:junit-bom:$junitApiVersion"))
        testImplementation(kotlin("test-junit5"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }
}

project(":downloadLotus") {
    apply {
        plugin("application")
    }

    application {
        mainClass.set("net.nprod.lotus.wikidata.download.MainKt")
    }

    val junitApiVersion: String by project
    val rdf4jVersion: String by project
    val log4jVersion: String by project
    val cliktVersion: String by project
    val univocityParserVersion: String by project
    val coroutinesVersion: String by project

    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
        implementation("com.github.ajalt.clikt:clikt:$cliktVersion")
        implementation("com.univocity:univocity-parsers:$univocityParserVersion")

        implementation("org.eclipse.rdf4j:rdf4j-repository-sail:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-sail-nativerdf:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-core:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-client:$rdf4jVersion")

        implementation(project(":konnector"))

        implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
        implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
        implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")

        testImplementation(platform("org.junit:junit-bom:$junitApiVersion"))
        testImplementation(kotlin("test-junit5"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks.named<Jar>("jar") {
        manifest {
            attributes["Main-Class"] = "net.nprod.lotus.wikidata.download.MainKt"
        }
    }
}

project(":importPublication") {
    apply { plugin("application") }

    application {
        mainClass.set("net.nprod.lotus.tools.publicationImporter.MainKt")
    }

    dependencies {
        implementation(project(":wdkt"))
    }
}

val kotlinVersion: String by project

configurations.all {
    resolutionStrategy {
        // Force the kotlin-compiler-embeddable to the project's kotlinVersion to avoid old versions
        force("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
    }
    resolutionStrategy.cacheChangingModulesFor(1, "minutes")
}
