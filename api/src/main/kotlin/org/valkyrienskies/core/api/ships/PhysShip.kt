package org.valkyrienskies.core.api.ships

import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipTransform

@VSBeta
interface PhysShip {

    val id: ShipId

    var isStatic: Boolean

    @VSBeta
    var buoyantFactor: Double

    @VSBeta
    var doFluidDrag: Boolean

    val transform: ShipTransform

    /**
     * Apply a rotation dependent force in world-space
     */
    @Deprecated("renamed", replaceWith = ReplaceWith("applyRotatingForceInWorld"))
    fun applyRotDependentForce(force: Vector3dc)

    /**
     * Apply a force in world-space
     */
    @Deprecated("renamed", ReplaceWith("applyForceInWorld"))
    fun applyInvariantForce(force: Vector3dc)

    /**
     * Apply a [force] in inertia-local-space to a [pos] in inertia-local-space
     */
    @Deprecated("renamed", ReplaceWith(""))
    fun applyInvariantForceToPos(force: Vector3dc, pos: Vector3dc)

    /**
     * Apply a rotation dependent torque in world-space
     */
    fun applyRotDependentTorque(torque: Vector3dc)

    /**
     * Apply a torque in world-space
     */
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
