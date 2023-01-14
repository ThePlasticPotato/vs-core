package org.valkyrienskies.core.api.bodies

import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyTransform

interface BaseVSBody {

    val id: BodyId

    val transform: BodyTransform
    val prevTickTransform: BodyTransform

    val worldAABB: AABBdc
    val velocity: Vector3dc
    val omega: Vector3dc

}