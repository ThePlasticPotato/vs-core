package org.valkyrienskies.core.game.ships

import org.joml.Vector3dc

interface PhysShip {

    val id: ShipId

    var isStatic: Boolean

    var buoyantFactor: Double


    fun applyRotDependentForce(force: Vector3dc)

    fun applyInvariantForce(force: Vector3dc)

    fun applyInvariantForceToPos(force: Vector3dc, pos: Vector3dc)

    fun applyRotDependentTorque(torque: Vector3dc)

    fun applyInvariantTorque(torque: Vector3dc)
}
