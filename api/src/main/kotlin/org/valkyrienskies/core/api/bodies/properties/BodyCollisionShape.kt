package org.valkyrienskies.core.api.bodies.properties

import org.joml.Vector3dc

interface BodyCollisionShape {

    interface Sphere {
        val radius: Double
    }

    interface Box {
        val lengths: Vector3dc
    }

    interface Wheel {
        val radius: Double
        val halfThickness: Double
    }

    interface Capsule {
        val radius: Double
        val halfLength: Double
    }

}