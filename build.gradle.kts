plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    java
    checkstyle
    id("org.sonarqube") version "3.1.1"
}

group = "org.valkyrienskies.core"
version = "1.0"

val vs_maven_url: String by project
repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
    maven {
        name = "VS Maven"
        url = uri(vs_maven_url)
    }
}

dependencies {
    val jacksonVersion = "2.12.1"
    val nettyVersion = "4.1.25.Final"

    // VS Physics
    api("com.github.ValkyrienSkies:Valkyrien-Skies-Physics-API:c84f1419e606de702e43d5417dd1c925bf0eefd8")
    api("com.github.ValkyrienSkies:Valkyrien-Skies-Krunch-Physics-API:9782703dfb15e5eec490a7b2122489f01b8b35e0")

    // JOML for Math
    api("org.joml:joml:1.10.0")
    api("org.joml:joml-primitives:1.10.0")

    // Apache Commons Math for Linear Programming
    implementation("org.apache.commons", "commons-math3", "3.6.1")

    // Guava
    implementation("com.google.guava:guava:29.0-jre")

    // Jackson Binary Dataformat for Object Serialization
    api("com.fasterxml.jackson.module", "jackson-module-kotlin", jacksonVersion)
    api("com.fasterxml.jackson.dataformat", "jackson-dataformat-cbor", jacksonVersion)
    api("com.fasterxml.jackson.datatype", "jackson-datatype-guava", jacksonVersion)

    implementation("com.flipkart.zjsonpatch", "zjsonpatch", "0.4.11")

    // FastUtil for Fast Primitive Collections
    implementation("it.unimi.dsi", "fastutil", "8.2.1")

    // Netty for networking
    implementation("io.netty", "netty-buffer", nettyVersion)

    // Junit 5 for Unit Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
}

tasks.withType<Checkstyle> {
    reports {
        // Do not output html reports
        html.isEnabled = false
        // Output xml reports
        xml.isEnabled = true
    }
}

checkstyle {
    toolVersion = "8.41"
    configFile = file("$rootDir/.checkstyle/checkstyle.xml")
    isIgnoreFailures = false
}

ktlint {
    disabledRules.set(setOf("parameter-list-wrapping"))
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileJava {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    compileTestJava {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "ValkyrienSkies_vs-core")
        property("sonar.organization", "valkyrienskies")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
