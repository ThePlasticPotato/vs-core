package org.valkyrienskies.core.config

import com.github.imifou.jsonschema.module.addon.annotation.JsonSchema
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

object VSCoreConfig {

    @Module
    class ServerConfigModule {
        @Provides
        @Singleton
        fun server(): Server = SERVER
    }

    @JvmField
    val SERVER = Server()

    class Server {
        @JsonSchema(
            description = "Port to attempt to establish UDP connections on"
        )
        var udpPort = 25565

        @JsonSchema(
            description = "Ship load distance in blocks"
        )
        var shipLoadDistance = 128.0

        @JsonSchema(
            description = "Ship unload distance in blocks"
        )
        var shipUnloadDistance = 196.0

        @JsonSchema(
            description = "All related settings to the physics engine"
        )
        var physics = PhysicsConfig()
    }
}

fun main() {
    val config = VSConfigClass.registerConfig("vs_core", VSCoreConfig::class.java)
    println(config.client!!.schemaJson.toPrettyString())
}
