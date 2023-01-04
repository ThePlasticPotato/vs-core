plugins {
    id("vs-core.convention")
}

repositories {
    mavenLocal()
}

dependencies {
    api(project(":api-game"))

    // Kotlin
    api(kotlin("stdlib-jdk8"))
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    val jacksonVersion = "2.13.3"
    val nettyVersion = "4.1.25.Final"
    val kotestVersion = "5.4.1"

    // VS Physics
    api("org.valkyrienskies:physics_api_krunch:1.0.0+cf19fdcc84")

    implementation("ch.ethz.globis.phtree:phtree:2.5.0")

    // JOML for Math
    api("org.joml:joml:1.10.4")
    api("org.joml:joml-primitives:1.10.0")

    // Apache Commons Math for Linear Programming
    implementation("org.apache.commons", "commons-math3", "3.6.1")

    // Guava
    implementation("com.google.guava:guava:29.0-jre")

    // Jackson Binary Dataformat for Object Serialization
    api("com.fasterxml.jackson.module", "jackson-module-kotlin", jacksonVersion)
    api("com.fasterxml.jackson.module", "jackson-module-parameter-names", jacksonVersion)
    api("com.fasterxml.jackson.dataformat", "jackson-dataformat-cbor", jacksonVersion)
    api("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml", jacksonVersion)
    api("com.github.Rubydesic:jackson-kotlin-dsl:1.2.0")

    api("com.networknt", "json-schema-validator", "1.0.71")
    api("com.github.imifou", "jsonschema-module-addon", "1.2.1")
    implementation("com.github.victools", "jsonschema-module-jackson", "4.25.0")
    implementation("com.github.victools", "jsonschema-generator", "4.25.0")
    implementation("com.flipkart.zjsonpatch", "zjsonpatch", "0.4.11")

    // FastUtil for Fast Primitive Collections
    implementation("it.unimi.dsi", "fastutil", "8.2.1")

    // Netty for networking (ByteBuf)
    implementation("io.netty", "netty-buffer", nettyVersion)

    // Dagger for compile-time Dependency Injection
    val daggerVersion = "2.43.2"
    implementation("com.google.dagger", "dagger", daggerVersion)
    annotationProcessor("com.google.dagger", "dagger-compiler", daggerVersion)
    testAnnotationProcessor("com.google.dagger", "dagger-compiler", daggerVersion)
    kapt("com.google.dagger", "dagger-compiler", daggerVersion)
    kaptTest("com.google.dagger", "dagger-compiler", daggerVersion)

    // MapStruct for DTO mapping (particularly ShipData)
    implementation("org.mapstruct:mapstruct:1.5.4.RubyDaggerFork-2")
    kapt("org.mapstruct:mapstruct-processor:1.5.4.RubyDaggerFork-2")

    // Junit 5 for Unit Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.mockk:mockk:1.12.5")

    // Log4j2 for Logging
    implementation("org.apache.logging.log4j:log4j-api:${properties["mc_log4j2_version"]}")
}

tasks.test {
    useJUnitPlatform {
        systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    }
}

// Publish javadoc and sources to maven
java {
    withJavadocJar()
    withSourcesJar()
}



