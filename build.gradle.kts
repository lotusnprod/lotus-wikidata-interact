import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.ben-manes.versions")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jmailen.kotlinter")
    application
}
group = "net.nprod.onpdb.wdimport"
version = "0.2-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx")

    flatDir {
        dirs("./libs")
    }
}
dependencies {
    val kotlinxCliVersion: String by project
    val cdkVersion: String by project
    val wdtkVersion: String by project
    val rdf4jVersion: String by project
    val log4jVersion: String by project
    val junitApiVersion: String by project
    val univocityParserVersion: String by project
    val ktorVersion: String by project
    val serializationVersion: String by project
    val kotlinVersion: String by project

    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:$kotlinxCliVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:$log4jVersion")

    implementation("org.wikidata.wdtk:wdtk-dumpfiles:$wdtkVersion")
    implementation("org.wikidata.wdtk:wdtk-wikibaseapi:$wdtkVersion")
    implementation("org.wikidata.wdtk:wdtk-datamodel:$wdtkVersion")
    implementation("org.wikidata.wdtk:wdtk-rdf:$wdtkVersion")

    implementation("org.openscience.cdk:cdk-bundle:$cdkVersion")

    implementation("org.eclipse.rdf4j:rdf4j-client:$rdf4jVersion")
    implementation("org.eclipse.rdf4j:rdf4j-core:$rdf4jVersion")
    implementation("org.eclipse.rdf4j:rdf4j-repository-sail:$rdf4jVersion")
    implementation("org.eclipse.rdf4j:rdf4j-sail-memory:$rdf4jVersion")

    implementation("com.univocity:univocity-parsers:$univocityParserVersion")

    //implementation(":wiki-java-0.36-SNAPSHOT")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitApiVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitApiVersion")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClassName = "net.nprod.lotus.wdimport.MainKt"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
