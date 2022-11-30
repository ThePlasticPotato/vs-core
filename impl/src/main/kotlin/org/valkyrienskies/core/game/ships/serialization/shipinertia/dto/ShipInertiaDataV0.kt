package org.valkyrienskies.core.game.ships.serialization.shipinertia.dto

import org.joml.Matrix3d
import org.joml.Vector3d

data class ShipInertiaDataV0 constructor(
    val centerOfMassInShipSpace: Vector3d,
    var shipMass: Double,
    val momentOfInertiaTensor: Matrix3d
)