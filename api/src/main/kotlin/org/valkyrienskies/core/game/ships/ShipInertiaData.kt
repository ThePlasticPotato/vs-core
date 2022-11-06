package org.valkyrienskies.core.game.ships

import org.joml.Matrix3dc
import org.joml.Vector3dc

interface ShipInertiaData {
    val momentOfInertiaTensor: Matrix3dc
    val centerOfMassInShipSpace: Vector3dc
    val shipMass: Double
}
