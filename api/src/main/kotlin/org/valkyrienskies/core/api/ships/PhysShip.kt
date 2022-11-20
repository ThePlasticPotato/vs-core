package org.valkyrienskies.core.api.ships

import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.ships.properties.ShipId


interface PhysShip {

    val id: ShipId

    var isStatic: Boolean

    @VSBeta
    var buoyantFactor: Double

    fun applyRotDependentForce(force: Vector3dc)

    fun applyInvariantForce(force: Vector3dc)

    fun applyInvariantForceToPos(force: Vector3dc, pos: Vector3dc)

    fun applyRotDependentTorque(torque: Vector3dc)

    fun applyInvariantTorque(torque: Vector3dc)
}
