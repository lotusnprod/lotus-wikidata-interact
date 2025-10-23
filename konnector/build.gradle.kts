plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
    id("com.google.protobuf") version "0.9.5"
}

group = "net.nprod"
version = "0.1.35"

repositories {
    mavenCentral()
}

val ktorVersion: String by project
val serializationVersion: String by project
val coroutinesVersion: String by project
val junitApiVersion: String by project

dependencies {
    // Ktor client
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    // kotlinx.serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    // Woodstox for XML
    implementation("com.fasterxml.woodstox:woodstox-core:7.1.1")
    // JUnit for testing (JUnit 5 latest via BOM; JUnit 6 does not exist yet)
    testImplementation(platform("org.junit:junit-bom:$junitApiVersion"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // gRPC and Protobuf
    implementation("io.grpc:grpc-kotlin-stub:1.5.0")
    implementation("io.grpc:grpc-protobuf:1.75.0")
    implementation("io.grpc:grpc-stub:1.75.0")
    implementation("com.google.protobuf:protobuf-java:4.32.1")
    implementation("com.google.protobuf:protobuf-java-util:4.32.1")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    // For grpc-netty if needed
    implementation("io.grpc:grpc-netty-shaded:1.75.0")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.32.1"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.75.0"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.5.0:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
                create("grpckt")
            }
        }
    }
}

sourceSets {
    main {
        java.srcDirs(
            "src/main/kotlin",
            "build/generated/source/proto/main/java",
            "build/generated/source/proto/main/grpc",
            "build/generated/source/proto/main/grpckt",
        )
    }
    test {
        java.srcDirs("src/test/kotlin")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<Test> {
    maxParallelForks = 1
}

tasks.test {
    useJUnitPlatform()
}

// Limit kotlinter tasks to authored sources only (exclude generated proto/grpc)
afterEvaluate {
    tasks
        .matching { it.name == "lintKotlinMain" || it.name == "formatKotlinMain" }
        .withType(org.jmailen.gradle.kotlinter.tasks.LintTask::class.java) { setSource(fileTree("src/main/kotlin")) }
    tasks
        .matching { it.name == "formatKotlinMain" }
        .withType(org.jmailen.gradle.kotlinter.tasks.FormatTask::class.java) { setSource(fileTree("src/main/kotlin")) }
}
