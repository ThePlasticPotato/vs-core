plugins {
    id("vs-core.convention")
}

dependencies {
    api(project(":api"))
    // JOML for Math
    api("org.joml:joml:1.10.4")
    api("org.joml:joml-primitives:1.10.0")
    api("com.google.code.findbugs:jsr305:3.0.2")

    implementation("it.unimi.dsi", "fastutil", "8.2.1")

    val kotestVersion = "5.4.1"
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}


tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
