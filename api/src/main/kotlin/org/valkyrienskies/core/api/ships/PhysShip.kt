package org.valkyrienskies.core.api.ships

import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipTransform

interface PhysShip {

    val id: ShipId

//    val transform: ShipTransform
//    val velocity: Vector3dc
//    val omega: Vector3dc
//
//    val worldAABB: AABBdc
//    val shipAABB: AABBic

    var isStatic: Boolean

    @VSBeta
    var buoyantFactor: Double

    @VSBeta
    var doFluidDrag: Boolean

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
     * @param forceInWorld is the force the block makes in world coordinates
     * @param relPosInWorld is the position the force is applied to relative to the ship's center of mass, in world
     *                       coordinates
     */
    fun applyRotDependentForceToPos(forceInWorld: Vector3dc, relPosInWorld: Vector3dc)
}
