plugins {
    id("vs-core.convention")
}

dependencies {
    api(project(":api"))
    api("io.netty:netty-buffer:4.1.85.Final")
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
