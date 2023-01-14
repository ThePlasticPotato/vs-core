package org.valkyrienskies.core.api.bodies

import org.joml.Matrix3dc
import org.joml.Matrix4dc
import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta

interface PhysicsVSBody : BaseVSBody {

    /**
     * The mass of this body, in kg
     */
    var mass: Double

    /**
     * The center of mass of this body, in world-space
     */
    var centerOfMass: Vector3dc

    /**
     * The moment of inertia tensor for this body, in world-space
     */
    var momentOfInertia: Matrix3dc

    var isStatic: Boolean

    @VSBeta
    var buoyantFactor: Double

    @VSBeta
    var doFluidDrag: Boolean

    fun setTransform(modelToWorld: Matrix4dc)

    fun applyRotDependentForce(force: Vector3dc)

    fun applyInvariantForce(force: Vector3dc)

    fun applyInvariantForceToPos(force: Vector3dc, pos: Vector3dc)

    fun applyRotDependentTorque(torque: Vector3dc)

    fun applyInvariantTorque(torque: Vector3dc)

    /**
     * Use this for blocks whose force direction and force position both depend on the ship's rotation.
     *
     * For example, the old propeller blocks in VS1 would use this function.
     *
     * @param force is the force the block makes in world coordinates will be transformed based on ship rotation.
     * @param pos is the position the force is applied to relative to the ship's center of mass, in world
     *                       coordinates
     */
    fun applyRotDependentForceToPos(force: Vector3dc, pos: Vector3dc)

}