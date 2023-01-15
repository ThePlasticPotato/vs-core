package org.valkyrienskies.core.impl.bodies.physics.world

import org.valkyrienskies.core.api.attachment.AttachmentHolder
import org.valkyrienskies.core.api.bodies.PhysicsVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.physics.constraints.VSConstraint
import org.valkyrienskies.core.api.physics.constraints.VSConstraintId
import org.valkyrienskies.core.api.reference.VSRef
import org.valkyrienskies.core.api.world.ValkyrienPhysicsWorld

class ValkyrienPhysicsWorldImpl(
    private val attachments: AttachmentHolder
) : ValkyrienPhysicsWorld, AttachmentHolder by attachments {
    override fun createBody(): PhysicsVSBody {
        TODO("Not yet implemented")
    }

    override fun removeBody(id: BodyId) {
        TODO("Not yet implemented")
    }

    override fun getBody(id: BodyId): PhysicsVSBody? {
        TODO("Not yet implemented")
    }

    override fun getBodyReference(id: BodyId): VSRef<PhysicsVSBody> {
        TODO("Not yet implemented")
    }

    override fun createNewConstraint(vsConstraint: VSConstraint): VSConstraintId? {
        TODO("Not yet implemented")
    }

    override fun updateConstraint(constraintId: VSConstraintId, updatedVSConstraint: VSConstraint): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeConstraint(constraintId: VSConstraintId): Boolean {
        TODO("Not yet implemented")
    }

    override fun execute(command: Runnable) {
        TODO("Not yet implemented")
    }
}