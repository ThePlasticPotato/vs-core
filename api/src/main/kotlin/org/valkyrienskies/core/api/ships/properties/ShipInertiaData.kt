package org.valkyrienskies.core.api.ships.properties

import org.joml.Matrix3dc
import org.joml.Vector3dc

interface ShipInertiaData {
    val momentOfInertiaTensor: Matrix3dc

    val centerOfMassInShip: Vector3dc
    val mass: Double

    @Deprecated("renamed", ReplaceWith("mass"))
    val shipMass: Double get() = mass

    @Deprecated("renamed", ReplaceWith("centerOfMassInShip"))
    val centerOfMassInShipSpace: Vector3dc get() = centerOfMassInShip

    // The moment of inertia tensor unmodified, as we should save it
    val momentOfInertiaTensorToSave: Matrix3dc
}
