package org.valkyrienskies.core.api.bodies

import org.joml.Matrix3dc
import org.joml.Vector3dc

interface ServerVSBody : VSBody {

    /**
     * The mass of this body, in kg
     */
    val mass: Double

    /**
     * The center of mass of this body, in world-space
     */
    val centerOfMass: Vector3dc

    /**
     * The moment of inertia tensor for this body, in world-space
     */
    val momentOfInertia: Matrix3dc

}