package org.valkyrienskies.core.impl.entities.bodies

import org.joml.Matrix3dc
import org.joml.Quaterniondc
import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.bodies.PhysicsVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyTransformVelocity
import org.valkyrienskies.core.api.world.properties.DimensionId

class PhysicsVSBodyImpl(
    override val id: BodyId,
    override val dimension: DimensionId,
    override val transform: BodyTransformVelocity,
    override val prevTickTransform: BodyTransformVelocity,
    override val aabb: AABBdc,
    override val mass: Double,
    override val momentOfInertia: Matrix3dc,
) : PhysicsVSBody {


    

    override fun applyForceInWorld(reason: String, force: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyForceInWorld(reason: String, force: Vector3dc, pos: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyForceInLocal(reason: String, force: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyForceInLocal(reason: String, force: Vector3dc, pos: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyRotatingForceInWorld(reason: String, force: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyRotatingForceInWorld(reason: String, force: Vector3dc, pos: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyRotatingForceInLocal(reason: String, force: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyRotatingForceInLocal(reason: String, force: Vector3dc, pos: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyTorqueInWorld(reason: String, torque: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyTorqueInLocal(reason: String, torque: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyRotatingTorqueInWorld(reason: String, torque: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyRotatingTorqueInLocal(reason: String, torque: Vector3dc) {
        TODO("Not yet implemented")
    }

    override var isStatic: Boolean = TODO()
    override var buoyantFactor: Double = TODO()
    override var doFluidDrag: Boolean = TODO()
    override fun setTransform(position: Vector3dc, rotation: Quaterniondc, scaling: Vector3dc) {
        TODO("Not yet implemented")
    }
}