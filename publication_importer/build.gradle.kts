plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("net.nprod.lotus.tools.publicationImporter.MainKt")
}

dependencies {
    implementation(project(":wdkt"))
}
