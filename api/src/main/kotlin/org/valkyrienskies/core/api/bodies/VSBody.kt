package org.valkyrienskies.core.api.bodies

import org.jetbrains.annotations.ApiStatus
import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.bodies.properties.EntityTransform

@ApiStatus.NonExtendable
interface VSBody {

    val id: ShipId

    val transform: EntityTransform
    val prevTickTransform: EntityTransform

    val worldAABB: AABBdc
    val velocity: Vector3dc
    val omega: Vector3dc

}