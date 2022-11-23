plugins {
    id("vs-core.convention")
}

dependencies {
    api(project(":api"))
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
