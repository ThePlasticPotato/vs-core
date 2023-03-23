package org.valkyrienskies.core.api.bodies.properties

import org.joml.Vector3dc

interface BodyVelocities {
    val velocity: Vector3dc
    val omega: Vector3dc
}