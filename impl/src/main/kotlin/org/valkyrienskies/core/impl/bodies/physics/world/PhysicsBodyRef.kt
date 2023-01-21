package org.valkyrienskies.core.impl.bodies.physics.world

import org.valkyrienskies.core.api.bodies.PhysicsVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.reference.VSRef
import org.valkyrienskies.core.api.world.ValkyrienPhysicsWorld

class PhysicsBodyRef(private val id: BodyId, private val world: ValkyrienPhysicsWorld): VSRef<PhysicsVSBody> {
    override val type: Class<PhysicsVSBody> = PhysicsVSBody::class.java

    override fun get(): PhysicsVSBody? = world.getBody(id)

    override fun getOrThrow(): PhysicsVSBody = world.getBody(id)
        ?: throw NullPointerException("Reference pointing to nonexistent body with ID: $id")
}