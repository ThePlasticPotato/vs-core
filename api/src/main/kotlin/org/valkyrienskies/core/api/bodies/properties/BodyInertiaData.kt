package org.valkyrienskies.core.api.bodies.properties

import org.joml.Matrix3dc
import org.joml.Vector3dc

interface BodyInertiaData {

    /**
     * The moment of inertia tensor, in model space
     */
    val momentOfInertia: Matrix3dc

    /**
     * The center of mass, in model space
     */
    val centerOfMass: Vector3dc

    /**
     * The mass, in kg
     */
    val mass: Double

    fun copy(): BodyInertiaData
}