package org.valkyrienskies.core.api.world

import org.valkyrienskies.core.api.bodies.PhysicsVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.reference.VSRef

interface ValkyrienPhysicsWorld : ValkyrienBaseWorld {

    fun createBody(): PhysicsVSBody

    fun removeBody(id: BodyId)

    override fun getBody(id: BodyId): PhysicsVSBody?

    override fun getBodyReference(id: BodyId): VSRef<PhysicsVSBody>

}