package org.valkyrienskies.core.api.bodies

import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyTransform
import org.valkyrienskies.core.api.util.HasId
import org.valkyrienskies.core.api.world.properties.DimensionId

interface BaseVSBody : HasId {

    override val id: BodyId

    val dimension: DimensionId

    val transform: BodyTransform
    val prevTickTransform: BodyTransform

    val aabb: AABBdc
    val velocity: Vector3dc
    val omega: Vector3dc

}
