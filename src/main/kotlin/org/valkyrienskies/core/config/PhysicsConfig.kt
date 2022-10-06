package org.valkyrienskies.core.config

import com.github.imifou.jsonschema.module.addon.annotation.JsonSchema
import org.valkyrienskies.physics_api_krunch.KrunchPhysicsWorldSettings

class PhysicsConfig {

    @JsonSchema(
        description = "The detail of the collision LOD of ships, higher values are more detailed but heavier to compute"
    )
    var lodDetail = 64

    fun makeKrunchSettings(): KrunchPhysicsWorldSettings {
        val settings = KrunchPhysicsWorldSettings()
        // Only use 10 sub-steps
        settings.subSteps = 10

        // Decrease max de-penetration speed so that rigid bodies don't go
        // flying apart when they overlap
        settings.maxDePenetrationSpeed = 10.0

        settings.maxVoxelShapeCollisionPoints = lodDetail
        return settings
    }
}
