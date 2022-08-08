package org.valkyrienskies.core.config

import com.github.imifou.jsonschema.module.addon.annotation.JsonSchema
import org.valkyrienskies.core.config.framework.scopes.single.SingleScopedConfig

object VSCoreConfig {

    data class Server(
        @JsonSchema(
            description = "Port to attempt to establish UDP connections on"
        )
        val udpPort: Int = 25565,

        @JsonSchema(
            description = "Ship load distance in blocks"
        )
        val shipLoadDistance: Double = 128.0,

        @JsonSchema(
            description = "Ship unload distance in blocks"
        )
        val shipUnloadDistance: Double = 196.0
    ) : SingleScopedConfig
}
