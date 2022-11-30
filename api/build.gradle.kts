plugins {
    id("vs-core.convention")
}

dependencies {
    // JOML for Math
    api("org.joml:joml:1.10.4")
    api("org.joml:joml-primitives:1.10.0")

    compileOnlyApi("org.jetbrains:annotations:23.0.0")
}

publishing {
    publications {
        create<MavenPublication>("api") {
            from(components["java"])
        }
    }
}
