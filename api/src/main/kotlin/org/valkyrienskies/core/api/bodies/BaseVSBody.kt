package org.valkyrienskies.core.api.bodies

import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyTransformVelocity
import org.valkyrienskies.core.api.util.HasId
import org.valkyrienskies.core.api.world.properties.DimensionId

interface BaseVSBody : HasId {

    override val id: BodyId

    val dimension: DimensionId

    val transform: BodyTransformVelocity
    val prevTickTransform: BodyTransformVelocity

    val aabb: AABBdc
    val velocity: Vector3dc get() = transform.velocity
    val omega: Vector3dc get() = transform.omega

}
