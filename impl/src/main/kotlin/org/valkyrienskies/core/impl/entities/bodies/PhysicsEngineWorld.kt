package org.valkyrienskies.core.impl.entities.bodies

import org.valkyrienskies.core.api.bodies.VSBody
import org.valkyrienskies.core.api.physics.constraints.VSConstraintAndId
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.impl.entities.EntityWorld
import org.valkyrienskies.physics_api.PhysicsWorldReference

class PhysicsEngineWorld(
    private val bodies: EntityWorld<VSBody>,
    private val constraints: EntityWorld<VSConstraintAndId>
) {

    private val worlds = HashMap<DimensionId, PhysicsWorldReference>()

    fun tick() {

    }
}