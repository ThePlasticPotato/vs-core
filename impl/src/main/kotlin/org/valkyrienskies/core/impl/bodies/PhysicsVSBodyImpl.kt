package org.valkyrienskies.core.impl.bodies

import org.joml.*
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.bodies.PhysicsVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyTransform
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl
import org.valkyrienskies.physics_api.PhysicsBodyInertiaData
import org.valkyrienskies.physics_api.PhysicsBodyReference
import org.valkyrienskies.physics_api.PoseVel

class PhysicsVSBodyImpl(
    override val id: BodyId,
    override val dimension: DimensionId,
    transform: BodyTransform,
    override var velocity: Vector3dc,
    override var omega: Vector3dc,
    override val shape: BodyShapeInternal,
    override var mass: Double,
    override var momentOfInertia: Matrix3dc,
    override var isStatic: Boolean,
    override var buoyantFactor: Double,
    override var doFluidDrag: Boolean,
    val phys: PhysicsBodyReference<*>
) : PhysicsVSBody {

    var lastServerTickNumber: Long = 0

    override val aabb: AABBd = AABBd(shape.aabb).transform(transform.toWorld)

    private val impulseThisTick = Vector3d()
    private val angularImpulseThisTick = Vector3d()

    init {
        if (shape is AABBUpdateNotifier) {
            shape.aabbUpdateEvent.on(::updateAABB)
        }
    }

    constructor(body: ServerBaseVSBodyData, phys: PhysicsBodyReference<*>) : this(
        body.id,
        body.dimension,
        body.transform,
        body.velocity,
        body.omega,
        body.shape,
        body.mass,
        body.momentOfInertia,
        body.isStatic,
        body.buoyantFactor,
        body.doFluidDrag,
        phys
    )

    override var transform = transform
        set(value) {
            prevTickTransform = field
            field = value
        }

    override var prevTickTransform: BodyTransform = transform

    /**
     * Call before ticking physics to update the attached reference
     */
    fun preTickPhysics() {
        phys.poseVel = createPoseVel(transform, velocity, omega)
        phys.collisionShapeOffset = Vector3d(transform.positionInModel).negate()
        phys.inertiaData = createPhysicsInertia(mass, momentOfInertia)
        phys.buoyantFactor = buoyantFactor
        phys.doFluidDrag = doFluidDrag
    }

    fun updateFrom(data: VSBodyUpdateToPhysics) {
        lastServerTickNumber = data.serverTickNumber
        data.velocity?.let { this.velocity = it }
        data.omega?.let { this.omega = it }
        data.mass?.let { this.mass = it }
        data.momentOfInertia?.let { this.momentOfInertia = it }
        data.isStatic?.let { isStatic = it }
        data.buoyantFactor?.let { this.buoyantFactor = it }
        data.doFluidDrag?.let { this.doFluidDrag = it }

        if (data.position != null || data.positionInModel != null || data.scaling != null || data.rotation != null) {
            val newPosition: Vector3dc =
                // If only the center of mass was updated, compensate the position in the world
                if (data.position == null && data.positionInModel != null) {
                    Vector3d(data.positionInModel)
                        .sub(transform.positionInModel)
                        .add(transform.position)
                } else {
                    // Use the newest position available otherwise
                    data.position ?: transform.position
                }

            val newScaling = data.scaling ?: transform.scaling
            val newRotation = data.rotation ?: transform.rotation
            val newPositionInModel = data.positionInModel ?: transform.positionInModel

            this.transform = ShipTransformImpl(newPosition, newPositionInModel, newRotation, newScaling)
        }
    }

    private fun createPoseVel(transform: BodyTransform, velocity: Vector3dc, omega: Vector3dc): PoseVel {
        return PoseVel(transform.position, transform.rotation, velocity, omega)
    }

    private fun createPhysicsInertia(mass: Double, momentOfInertia: Matrix3dc): PhysicsBodyInertiaData {
        val invMass = 1.0 / mass
        if (!invMass.isFinite())
            throw IllegalStateException("invMass is not finite!")

        val invInertiaMatrix = momentOfInertia.invert(Matrix3d())
        if (!invInertiaMatrix.isFinite)
            throw IllegalStateException("invInertiaMatrix is not finite!")

        return PhysicsBodyInertiaData(invMass, invInertiaMatrix)
    }

    private fun updateAABB(aabb: AABBdc) {
        this.aabb.set(aabb).transform(transform.toWorld)
    }

    override fun setTransform(position: Vector3dc, rotation: Quaterniondc, scaling: Vector3dc) {
        transform = ShipTransformImpl(
            Vector3d(position), transform.positionInModel, Quaterniond(rotation), Vector3d(scaling)
        )
    }

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

}
