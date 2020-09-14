import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val wdtkVersion = "0.11.1"
val rdf4jVersion = "3.4.0"
val log4jVersion = "2.13.3"
val junitApiVersion = "5.6.0"
val univocityParserVersion = "2.8.4"
val jacksonVersion = "2.11.2"

plugins {
    kotlin("jvm") version "1.4.0-rc"
    application
}
group = "net.nprod.onpdb.wdimport"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}
dependencies {
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:$log4jVersion")

    implementation("org.wikidata.wdtk:wdtk-dumpfiles:$wdtkVersion")
    implementation("org.wikidata.wdtk:wdtk-wikibaseapi:$wdtkVersion")
    implementation("org.wikidata.wdtk:wdtk-datamodel:$wdtkVersion")
    implementation("org.wikidata.wdtk:wdtk-rdf:$wdtkVersion")

    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")

    implementation("org.eclipse.rdf4j", "rdf4j-client", rdf4jVersion)
    implementation("org.eclipse.rdf4j", "rdf4j-core", rdf4jVersion)
    implementation("org.eclipse.rdf4j", "rdf4j-repository-sail", rdf4jVersion)
    implementation("org.eclipse.rdf4j", "rdf4j-sail-memory", rdf4jVersion)

    implementation("org.jetbrains.exposed", "exposed-core", "0.26.2") {
        exclude("org.jetbrains.kotlin","kotlin-stdlib-jdk7")
        exclude("org.jetbrains.kotlin","kotlin-stdlib-jdk8")
        exclude("org.jetbrains.kotlin","kotlin-reflect")
        exclude("org.jetbrains.kotlin","kotlin-stdlib")
        exclude("org.jetbrains.kotlin","kotlin-stdlib-common")
    }
    implementation("org.jetbrains.exposed", "exposed-dao", "0.26.2")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.26.2")
    //implementation("org.xerial", "sqlite-jdbc", "3.32.3.2")
    implementation("com.h2database", "h2", "1.4.197")

    implementation(
        "com.univocity", "univocity-parsers", univocityParserVersion
    )
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitApiVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitApiVersion")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClassName = "MainKt"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
