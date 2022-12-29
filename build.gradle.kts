import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

val localPropertiesFile = file("local.properties")
val localProperties = if (localPropertiesFile.exists()) {
    val properties = Properties()
    properties.load(localPropertiesFile.inputStream())
    properties
} else null

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    id("com.github.ben-manes.versions")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
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

        localProperties?.let {
            val localMaven: String by it
            maven(uri("file:///$localMaven"))
        }
    }
}

subprojects {
    apply {
        plugin("com.github.ben-manes.versions")
        plugin("io.gitlab.arturbosch.detekt")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("org.jmailen.kotlinter")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.serialization")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
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
}

project("uploadLotus") {

    dependencies {
        val cdkVersion: String by project
        val detektVersion: String by project
        val jsoupVersion: String by project
        val junitApiVersion: String by project
        val konnectorVersion: String by project
        val kotlinVersion: String by project
        val kotlinxCliVersion: String by project
        val ktorVersion: String by project
        val log4jVersion: String by project
        val rdf4jVersion: String by project
        val serializationVersion: String by project
        val wdtkVersion: String by project

        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")

        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-cli:$kotlinxCliVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

        implementation("io.ktor:ktor-client-cio:$ktorVersion")
        implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
        implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")

        implementation("org.wikidata.wdtk:wdtk-dumpfiles:$wdtkVersion") {
            exclude("org.slf4j", "slf4j-api")
        }
        implementation("org.wikidata.wdtk:wdtk-wikibaseapi:$wdtkVersion") {
            exclude("org.slf4j", "slf4j-api")
        }
        implementation("org.wikidata.wdtk:wdtk-datamodel:$wdtkVersion") {
            exclude("org.slf4j", "slf4j-api")
        }
        implementation("org.wikidata.wdtk:wdtk-rdf:$wdtkVersion") {
            exclude("org.slf4j", "slf4j-api")
        }

        implementation("org.openscience.cdk:cdk-bundle:$cdkVersion")

        implementation("org.eclipse.rdf4j:rdf4j-client:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-core:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-repository-sail:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-sail-memory:$rdf4jVersion")

        // jsoup HTML parser library @ https://jsoup.org/
        implementation("org.jsoup:jsoup:$jsoupVersion")

        implementation("net.nprod:konnector:$konnectorVersion")

        testImplementation(kotlin("test-junit5"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitApiVersion")
        testImplementation("org.junit.jupiter:junit-jupiter:$junitApiVersion")
    }
}

project("wdkt") {

    dependencies {
        val cdkVersion: String by project
        val detektVersion: String by project
        val jsoupVersion: String by project
        val junitApiVersion: String by project
        val konnectorVersion: String by project
        val kotlinVersion: String by project
        val kotlinxCliVersion: String by project
        val ktorVersion: String by project
        val log4jVersion: String by project
        val rdf4jVersion: String by project
        val serializationVersion: String by project
        val wdtkVersion: String by project

        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")

        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-cli:$kotlinxCliVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

        implementation("io.ktor:ktor-client-cio:$ktorVersion")
        implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
        implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")

        implementation("org.wikidata.wdtk:wdtk-dumpfiles:$wdtkVersion") {
            exclude("org.slf4j", "slf4j-api")
        }
        implementation("org.wikidata.wdtk:wdtk-wikibaseapi:$wdtkVersion") {
            exclude("org.slf4j", "slf4j-api")
        }
        api("org.wikidata.wdtk:wdtk-datamodel:$wdtkVersion") {
            exclude("org.slf4j", "slf4j-api")
        }
        implementation("org.wikidata.wdtk:wdtk-rdf:$wdtkVersion") {
            exclude("org.slf4j", "slf4j-api")
        }

        implementation("org.openscience.cdk:cdk-bundle:$cdkVersion")

        implementation("org.eclipse.rdf4j:rdf4j-client:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-core:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-repository-sail:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-sail-memory:$rdf4jVersion")

        // jsoup HTML parser library @ https://jsoup.org/
        implementation("org.jsoup:jsoup:$jsoupVersion")

        implementation("net.nprod:konnector:$konnectorVersion")

        testImplementation(kotlin("test-junit5"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitApiVersion")
        testImplementation("org.junit.jupiter:junit-jupiter:$junitApiVersion")
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
        val univocityParserVersion: String by project
        val sqliteJdbcVersion: String by project
        val serializationVersion: String by project
        val detektVersion: String by project

        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")

        implementation(project(":wdkt"))

        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
        implementation("com.univocity:univocity-parsers:$univocityParserVersion")
        implementation("org.xerial:sqlite-jdbc:$sqliteJdbcVersion")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
    }
}

project(":downloadLotus") {
    val junitApiVersion: String by project
    val rdf4jVersion: String by project
    val log4jVersion: String by project
    val cliktVersion: String by project
    val univocityParserVersion: String by project
    val coroutinesVersion: String by project
    val konnectorVersion: String by project
    val detektVersion: String by project

    apply {
        plugin("application")
    }

    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

        implementation("com.github.ajalt.clikt:clikt:$cliktVersion")

        implementation("com.univocity:univocity-parsers:$univocityParserVersion")

        implementation("org.eclipse.rdf4j:rdf4j-repository-sail:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-sail-nativerdf:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-core:$rdf4jVersion")
        implementation("org.eclipse.rdf4j:rdf4j-client:$rdf4jVersion")

        implementation("net.nprod:konnector:$konnectorVersion")

        implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
        implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
        implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")

        testImplementation(kotlin("test-junit5"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitApiVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitApiVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitApiVersion")
    }

    val jar by tasks.getting(Jar::class) {
        manifest {
            attributes["Main-Class"] = "net.nprod.lotus.wikidata.download.MainKt"
        }
    }

    application {
        mainClass.set("net.nprod.lotus.wikidata.download.MainKt")
    }
}

project("importPublication") {
    apply {
        plugin("application")
    }

    application {
        mainClass.set("net.nprod.lotus.tools.publicationImporter.MainKt")
    }

    dependencies {
        val detektVersion: String by project

        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")

        implementation(project(":wdkt"))
    }
}

kotlinter {
    ignoreFailures = project.hasProperty("lintContinueOnError")
    experimentalRules = project.hasProperty("lintKotlinExperimental")
}

allprojects {
    detekt {
        val detektVersion: String by project
        toolVersion = detektVersion
        config = rootProject.files("qc/detekt.yml")
        baseline = rootProject.file("qc/detekt-baseline.xml")
    }
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(1, "minutes")
}
