package org.valkyrienskies.core.impl.config

import com.github.imifou.jsonschema.module.addon.annotation.JsonSchema

class PhysicsConfig {

    @JsonSchema(
        description = "The detail of the collision LOD of ships, higher values are more detailed but heavier to compute"
    )
    var lodDetail = 4096
}
