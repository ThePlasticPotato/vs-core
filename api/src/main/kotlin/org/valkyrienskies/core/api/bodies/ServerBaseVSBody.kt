package org.valkyrienskies.core.api.bodies

import org.joml.Matrix3dc
import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.bodies.shape.BodyShape

interface ServerBaseVSBody : BaseVSBody {
    val shape: BodyShape

    /**
     * The mass of this body, in kg
     */
    val mass: Double

    /**
     * The center of mass of this body, in world-space
     */
    val centerOfMass: Vector3dc get() = transform.positionInModel

    /**
     * The moment of inertia tensor for this body, in world-space
     */
    val momentOfInertia: Matrix3dc

    var isStatic: Boolean

    var buoyantFactor: Double

    @VSBeta
    var doFluidDrag: Boolean
}
