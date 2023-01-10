plugins {
    id("vs-core.convention")
}

dependencies {
    // JOML for Math
    api("org.joml:joml:1.10.4")
    api("org.joml:joml-primitives:1.10.0")

    compileOnlyApi("org.jetbrains:annotations:23.0.0")

    // used only very minimally, not strictly required by dependencies
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.14.1")

}
