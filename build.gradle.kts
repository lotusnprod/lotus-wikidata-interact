import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinxCliVersion = "0.3"
val cdkVersion = "2.3"
val wdtkVersion = "0.11.1"
val rdf4jVersion = "3.4.0"
val log4jVersion = "2.13.3"
val junitApiVersion = "5.6.0"
val univocityParserVersion = "2.8.4"

plugins {
    kotlin("jvm") version "1.4.10"
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
    implementation("org.jetbrains.kotlinx:kotlinx-cli:$kotlinxCliVersion")

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

    implementation(":wiki-java-0.36-SNAPSHOT")

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
