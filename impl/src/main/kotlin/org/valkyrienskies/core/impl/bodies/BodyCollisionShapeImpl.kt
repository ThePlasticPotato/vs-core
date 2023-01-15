package org.valkyrienskies.core.impl.bodies

import org.joml.Vector3dc
import org.valkyrienskies.core.api.bodies.shape.BodyShape

class BodyCollisionShapeImpl {
    data class Sphere(override val radius: Double) : BodyShape.Sphere

    data class Box(override val lengths: Vector3dc) : BodyShape.Box

    data class Wheel(override val radius: Double, override val halfThickness: Double) : BodyShape.Wheel

    data class Capsule(override val radius: Double, override val halfLength: Double) : BodyShape.Capsule


}