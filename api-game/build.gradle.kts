plugins {
    id("vs-core.convention")
}

dependencies {
    api(project(":api"))

    // TODO: this is really horrific. fix before releasing new api
    api("org.valkyrienskies:physics_api:1.0.0+68940375dc")
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
