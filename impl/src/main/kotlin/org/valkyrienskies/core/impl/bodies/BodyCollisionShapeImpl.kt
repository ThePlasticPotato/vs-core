package org.valkyrienskies.core.impl.bodies

import org.joml.Vector3dc
import org.valkyrienskies.core.api.bodies.properties.BodyCollisionShape

class BodyCollisionShapeImpl {
    data class Sphere(override val radius: Double) : BodyCollisionShape.Sphere

    data class Box(override val lengths: Vector3dc) : BodyCollisionShape.Box

    data class Wheel(override val radius: Double, override val halfThickness: Double) : BodyCollisionShape.Wheel

    data class Capsule(override val radius: Double, override val halfLength: Double) : BodyCollisionShape.Capsule

}