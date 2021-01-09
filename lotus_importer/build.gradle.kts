plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("net.nprod.lotus.wdimport.MainKt")
}

dependencies {
    val univocityParserVersion: String by project

    implementation(project(":wdkt"))
    implementation("com.univocity:univocity-parsers:$univocityParserVersion")
    implementation("org.xerial:sqlite-jdbc:3.15.1")
    implementation("org.springframework:spring-jdbc")
    implementation("org.springframework:spring-oxm")
    implementation("org.springframework.batch:spring-batch-core")
}
