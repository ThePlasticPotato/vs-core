package org.valkyrienskies.core.apigame.physics

import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipInertiaData
import org.valkyrienskies.core.api.ships.properties.ShipTransform

data class PhysicsEntityData(
    val shipId: ShipId,
    var transform: ShipTransform,
    var inertiaData: ShipInertiaData,
    var linearVelocity: Vector3dc,
    var angularVelocity: Vector3dc,
    var collisionShapeData: VSCollisionShapeData,
    var isStatic: Boolean = false
)
