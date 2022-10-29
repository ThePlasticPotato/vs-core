package org.valkyrienskies.core.api.impl

import org.joml.Vector3dc
import org.valkyrienskies.core.api.ForcesApplier
import org.valkyrienskies.core.util.requireIsFinite
import org.valkyrienskies.physics_api.RigidBodyReference

@JvmInline
value class APIForcesApplier(val rigidBody: RigidBodyReference) : ForcesApplier {

    override fun applyRotDependentForce(force: Vector3dc) {
        requireIsFinite(force)
        
        rigidBody.addRotDependentForceToNextPhysTick(force)
    }

    override fun applyInvariantForce(force: Vector3dc) {
        requireIsFinite(force)

        rigidBody.addInvariantForceToNextPhysTick(force)
    }

    override fun applyInvariantForceToPos(force: Vector3dc, pos: Vector3dc) {
        requireIsFinite(force)
        requireIsFinite(pos)

        rigidBody.addInvariantForceAtPosToNextPhysTick(pos, force)
    }

    override fun applyRotDependentTorque(torque: Vector3dc) {
        requireIsFinite(torque)

        rigidBody.addRotDependentTorqueToNextPhysTick(torque)
    }

    override fun applyInvariantTorque(torque: Vector3dc) {
        requireIsFinite(torque)

        rigidBody.addInvariantTorqueToNextPhysTick(torque)
    }

    // Makes the rigidbody static
    override fun setStatic(b: Boolean) {
        rigidBody.isStatic = b
    }
}
