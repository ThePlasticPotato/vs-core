package org.valkyrienskies.core.api.vsentities

import org.jetbrains.annotations.ApiStatus
import org.joml.Matrix4dc
import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.api.vsentities.properties.EntityTransform

@ApiStatus.NonExtendable
interface VSEntity {

    val id: ShipId

    val transform: EntityTransform
    val prevTickTransform: EntityTransform

    val worldAABB: AABBdc
    val velocity: Vector3dc
    val omega: Vector3dc

}