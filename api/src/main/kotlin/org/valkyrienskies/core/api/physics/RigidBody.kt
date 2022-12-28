package org.valkyrienskies.core.api.physics

import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta

@VSBeta
interface RigidBody<T : PhysicsShape> : PhysicsBody {
    val shape: T

    var isStatic: Boolean

    @VSBeta
    var buoyantFactor: Double

    @VSBeta
    var doFluidDrag: Boolean

    fun applyRotDependentForce(force: Vector3dc)

    fun applyInvariantForce(force: Vector3dc)

    fun applyRotDependentTorque(torque: Vector3dc)

    fun applyInvariantTorque(torque: Vector3dc)

    /**
     * Use this for blocks whose force are applied at a position wich causes the ship to rotate depending on the position of the force.
     *
     * For example, simple balloons will just apply at their pos upwards force
     *
     * @param force is the force the block makes in world coordinates. So (O.O, 1.O, 0.O) is a force of 1 upwards.
     * @param pos is the position the force is in shipyard coordinates.
     */
    fun applyInvariantForceToPos(force: Vector3dc, pos: Vector3dc)

    /**
     * Use this for blocks whose force direction and force position both depend on the ship's rotation.
     *
     * For example, the old propeller blocks in VS1 would use this function.
     *
     * @param force is the force the block makes in world coordinates will be transformed based on ship rotation.
     * @param pos is the position the force is in shipyard coordinates.
     */
    fun applyRotDependentForceToPos(force: Vector3dc, pos: Vector3dc)
}
