plugins {
    id("vs-core.convention")
}

dependencies {
    api(project(":api"))
    api("io.netty:netty-buffer:4.1.85.Final")

    val jacksonVersion = "2.13.3"

    // Jackson Binary Dataformat for Object Serialization
    api("com.fasterxml.jackson.module", "jackson-module-kotlin", jacksonVersion)
    api("com.fasterxml.jackson.module", "jackson-module-parameter-names", jacksonVersion)
    api("com.fasterxml.jackson.dataformat", "jackson-dataformat-cbor", jacksonVersion)
    api("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml", jacksonVersion)
    api("com.github.Rubydesic:jackson-kotlin-dsl:1.2.0")
}
