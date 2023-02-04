package org.valkyrienskies.core.api.bodies.properties

import org.joml.Quaterniondc
import org.joml.Vector3dc

interface PoseVelocity {
    val position: Vector3dc
    val rotation: Quaterniondc
    val velocity: Vector3dc
    val omega: Vector3dc
}