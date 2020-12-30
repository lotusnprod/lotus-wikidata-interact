plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("net.nprod.lotus.tools.articleImporter.MainKt")
}

dependencies {
    implementation(project(":wdkt"))
}