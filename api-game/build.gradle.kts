plugins {
    id("vs-core.convention")
}

dependencies {
    api(project(":api"))
    implementation(project(":impl"))
}

publishing {
    publications {
        create<MavenPublication>("api") {
            groupId = "org.valkyrienskies.core"
            artifactId = "vs-core-api-game"

            from(components["java"])
        }
    }
}
