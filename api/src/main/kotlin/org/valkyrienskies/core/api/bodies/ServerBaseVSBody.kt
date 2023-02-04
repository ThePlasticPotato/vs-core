package org.valkyrienskies.core.api.bodies

import org.joml.Matrix3dc
import org.joml.Quaterniondc
import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.bodies.shape.BodyShape

interface ServerBaseVSBody : BaseVSBody {

    // val segments: List<BodySegment>
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

    fun setTransform(position: Vector3dc, rotation: Quaterniondc, scaling: Vector3dc)

    fun setPosition(position: Vector3dc) = setTransform(position, transform.rotation, transform.scaling)

    fun setRotation(rotation: Quaterniondc) = setTransform(transform.position, rotation, transform.scaling)

    fun setScaling(scaling: Vector3dc) = setTransform(transform.position, transform.rotation, scaling)
}
