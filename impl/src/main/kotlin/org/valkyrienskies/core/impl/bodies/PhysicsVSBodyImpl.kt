package org.valkyrienskies.core.impl.bodies

import org.joml.Matrix3dc
import org.joml.Matrix4dc
import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.bodies.PhysicsVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyTransform
import org.valkyrienskies.core.api.bodies.shape.BodyShape
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl

class PhysicsVSBodyImpl(
    override val id: BodyId,
    transform: BodyTransform,
    override val worldAABB: AABBdc,
    override var velocity: Vector3dc,
    override var omega: Vector3dc,
    override val shape: BodyShape,
    override var mass: Double,
    override var centerOfMass: Vector3dc,
    override var momentOfInertia: Matrix3dc,
    override var isStatic: Boolean,
    override var buoyantFactor: Double,
    override var doFluidDrag: Boolean
) : PhysicsVSBody {

    override var transform = transform
        set(value) {
            prevTickTransform = field
            field = value
        }

    override var prevTickTransform: BodyTransform = transform
    override fun setTransform(modelToWorld: Matrix4dc) {
        this.transform = ShipTransformImpl.create(transform, modelToWorld)
    }

    override fun applyRotDependentForce(force: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyInvariantForce(force: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyInvariantForceToPos(force: Vector3dc, pos: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyRotDependentTorque(torque: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyInvariantTorque(torque: Vector3dc) {
        TODO("Not yet implemented")
    }

    override fun applyRotDependentForceToPos(force: Vector3dc, pos: Vector3dc) {
        TODO("Not yet implemented")
    }

}