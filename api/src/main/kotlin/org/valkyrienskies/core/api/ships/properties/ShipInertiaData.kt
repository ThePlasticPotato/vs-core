package org.valkyrienskies.core.api.ships.properties

import org.joml.Matrix3dc
import org.joml.Vector3dc
import org.valkyrienskies.core.api.bodies.properties.BodyInertiaData

interface ShipInertiaData : BodyInertiaData {
    val momentOfInertiaTensor: Matrix3dc

    val centerOfMassInShip: Vector3dc

    @Deprecated("renamed", ReplaceWith("mass"))
    val shipMass: Double get() = mass

    @Deprecated("renamed", ReplaceWith("centerOfMassInShip"))
    val centerOfMassInShipSpace: Vector3dc get() = centerOfMassInShip
}
