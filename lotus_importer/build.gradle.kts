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
}
