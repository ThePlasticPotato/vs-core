package org.valkyrienskies.core.api.bodies.properties

import org.joml.Matrix3dc
import org.joml.Vector3dc

interface BodyInertiaData {

    val mass: Double
    val centerOfMass: Vector3dc
    val momentOfInertia: Matrix3dc

}