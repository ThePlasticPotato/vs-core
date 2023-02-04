package org.valkyrienskies.core.impl.bodies

import org.joml.Matrix3dc
import org.joml.Quaterniondc
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.bodies.ServerVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyTransform
import org.valkyrienskies.core.api.bodies.shape.BodyShape
import org.valkyrienskies.core.api.world.properties.DimensionId

class ServerVSBodyImpl(
    override val id: BodyId,
    override val dimension: DimensionId,
    override var transform: BodyTransform,
    override var velocity: Vector3dc,
    override var omega: Vector3dc,
    override val shape: BodyShape,
    override val mass: Double,
    override val momentOfInertia: Matrix3dc,
    override var isStatic: Boolean,
    override var buoyantFactor: Double,
    @property:VSBeta
    override var doFluidDrag: Boolean
) : ServerVSBody {

    private var clock = 0
    override fun setTransform(position: Vector3dc, rotation: Quaterniondc, scaling: Vector3dc) {
        TODO("Not yet implemented")
    }

    override var prevTickTransform = transform
    override val aabb: AABBd = AABBd(shape.aabb).transform(transform.toWorld)

    private fun updateAABB(aabb: AABBdc) {
        this.aabb.set(aabb).transform(transform.toWorld)
    }

    constructor(data: VSBodyCreateDataToPhysics) : this(
        data.id,
        data.dimension,
        data.transform,
        data.velocity,
        data.omega,
        data.shape,
        data.mass,
        data.momentOfInertia,
        data.isStatic,
        data.buoyantFactor,
        data.doFluidDrag
    )

    fun updateFrom(update: VSBodyUpdateToServer) {
        if (update.clock <= clock) {
            return
        }


        prevTickTransform = transform
        transform = update.transform
        velocity = update.velocity
        omega = update.omega
        isStatic = update.isStatic
        buoyantFactor = update.buoyantFactor
        doFluidDrag = update.doFluidDrag
    }
}