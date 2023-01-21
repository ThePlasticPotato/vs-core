package org.valkyrienskies.core.impl.bodies.physics.world

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.attachment.AttachmentHolder
import org.valkyrienskies.core.api.bodies.PhysicsVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.physics.constraints.*
import org.valkyrienskies.core.api.reference.VSRef
import org.valkyrienskies.core.api.world.ValkyrienPhysicsWorld
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.impl.bodies.*
import org.valkyrienskies.core.impl.bodies.storage.QueryableBodiesImpl
import org.valkyrienskies.core.impl.config.PhysicsConfig
import org.valkyrienskies.core.impl.config.VSCoreConfig
import org.valkyrienskies.core.impl.util.TickableExecutor
import org.valkyrienskies.core.impl.util.WorldScoped
import org.valkyrienskies.core.impl.util.assertions.assertIsPhysicsThread
import org.valkyrienskies.physics_api.ConstraintId
import org.valkyrienskies.physics_api.PhysicsBodyId
import org.valkyrienskies.physics_api.PhysicsWorldReference
import org.valkyrienskies.physics_api.constraints.*
import org.valkyrienskies.physics_api.dummy_impl.DummyPhysicsWorldReference
import org.valkyrienskies.physics_api_krunch.KrunchBootstrap
import org.valkyrienskies.physics_api_krunch.KrunchPhysicsWorldSettings
import org.valkyrienskies.physics_api_krunch.SolverType
import javax.inject.Inject

@WorldScoped
class ValkyrienPhysicsWorldImpl @Inject constructor(
    private val attachments: AttachmentHolder,
    private val queues: PipelineQueues,
    private val idAllocator: PhysicsIdAllocator
) : ValkyrienPhysicsWorld, AttachmentHolder by attachments {

    var isUsingDummy: Boolean = false

    override val preTickExecutor = TickableExecutor()
    override val dumbForceExecutor = TickableExecutor()
    override val smartForceExecutor = TickableExecutor()
    override val postTickExecutor = TickableExecutor()

    private val worlds = HashMap<DimensionId, PhysicsWorldReference>()

    private val bodies = QueryableBodiesImpl<PhysicsVSBodyImpl>()
    private val constraints = Long2ObjectOpenHashMap<VSConstraintAndId>()
    private val physConstraints = Long2ObjectOpenHashMap<ConstraintAndId>()

    private val gravity = Vector3d(0.0, -9.8, 0.0)

    fun tick(timeStep: Double) {
        queues.bodiesToPhysics.forEach { applyBodyAction(it) }
        queues.constraintsToPhysics.forEach { applyConstraintAction(it) }
        bodies.forEach { it.preTickPhysics() }

        worlds.values.parallelStream().forEach { it.tick(gravity, timeStep, true) }
    }

    override fun createSphereBody(radius: Double, dimension: DimensionId): PhysicsVSBody {
        assertIsPhysicsThread()

        return createBodyFromPhysics(BodyShapeInternal.Sphere(radius), dimension)
    }

    override fun createBoxBody(lengths: Vector3dc, dimension: DimensionId): PhysicsVSBody {
        assertIsPhysicsThread()

        return createBodyFromPhysics(BodyShapeInternal.Box(Vector3d(lengths)), dimension)
    }

    override fun createWheelBody(radius: Double, halfThickness: Double, dimension: DimensionId): PhysicsVSBody {
        assertIsPhysicsThread()

        return createBodyFromPhysics(BodyShapeInternal.Wheel(radius, halfThickness), dimension)
    }

    override fun createCapsuleBody(radius: Double, halfLength: Double, dimension: DimensionId): PhysicsVSBody {
        assertIsPhysicsThread()

        return createBodyFromPhysics(BodyShapeInternal.Capsule(radius, halfLength), dimension)
    }

    override fun createVoxelBody(definedArea: AABBic, totalVoxelRegion: AABBic, dimension: DimensionId): PhysicsVSBody {
        assertIsPhysicsThread()

        return createBodyFromPhysics(BodyShapeInternal.Voxel(definedArea, totalVoxelRegion), dimension)
    }

    private fun createBodyFromPhysics(shape: BodyShapeInternal, dimension: DimensionId): PhysicsVSBodyImpl {
        val id = idAllocator.nextBodyId.getAndIncrement()
        val data = ServerBaseVSBodyData.createEmpty(id, dimension, shape)
        val body = createBodyInternal(data)
        queues.bodiesToServer.create(data)

        return body
    }

    private fun createBodyInternal(data: ServerBaseVSBodyData): PhysicsVSBodyImpl {
        val physicsWorld = getPhysicsWorld(data.dimension)
        data.shape.createRef(physicsWorld)

        val phys = physicsWorld.createRigidBody(data.shape.ref)
        val body = PhysicsVSBodyImpl(data, phys)

        bodies.add(body)

        return body
    }

    private fun applyBodyAction(action: Action<ServerBaseVSBodyData, VSBodyUpdateToPhysics>) {
        when (action) {
            is Action.Delete -> removeBodyInternal(action.id)
            is Action.Create -> createBodyInternal(action.create)
            is Action.Update -> updateBodyFromGame(action.update)
        }
    }

    private fun applyConstraintAction(action: Action<VSConstraintAndId, VSConstraintAndId>) {
        when (action) {
            is Action.Delete -> removeConstraintInternal(action.id)
            is Action.Create -> createConstraintInternal(action.create)
            is Action.Update -> updateConstraintInternal(action.update)
        }
    }

    private fun updateBodyFromGame(data: VSBodyUpdateToPhysics) {
        assertIsPhysicsThread()

        val body = bodies.getById(data.id) ?: return


        body.updateFrom(data)
    }

    private fun removeBodyInternal(id: BodyId) {
        assertIsPhysicsThread()

        val body = getBody(id) ?: return
        getPhysicsWorld(body.dimension).deleteRigidBody(body.phys.physicsBodyId)
        bodies.remove(id)
    }

    private fun createConstraintInternal(constraint: VSConstraintAndId): Boolean {
        val body0 = getBody(constraint.vsConstraint.bodyId0) ?: return false
        val body1 = getBody(constraint.vsConstraint.bodyId1) ?: return false

        val world = getPhysicsWorld(checkSameDimension(body0, body1))
        world.addConstraint(constraint.convertToPhysics())

        constraints.put(constraint.constraintId, constraint)

        return true
    }

    private fun updateConstraintInternal(constraintAndId: VSConstraintAndId): Boolean {
        val updated = constraintAndId.vsConstraint
        val id = constraintAndId.id

        val body0 = getBody(updated.bodyId0) ?: return false
        val body1 = getBody(updated.bodyId1) ?: return false

        val newDimension = checkSameDimension(body0, body1)
        val prevDimension = constraints.get(id)?.vsConstraint?.bodyId0?.let { getBody(it)?.dimension }

        require(prevDimension == newDimension) {
            "Cannot update constraint across dimensions"
        }

        val world = getPhysicsWorld(newDimension)
        world.updateConstraint(constraintAndId.convertToPhysics())

        constraints.put(id, constraintAndId)

        return true
    }

    private fun removeConstraintInternal(id: VSConstraintId) {
        constraints.remove(id)
    }


    override fun removeBody(id: BodyId) {
        removeBodyInternal(id)
        queues.bodiesToServer.delete(id)
    }

    override fun getBody(id: BodyId): PhysicsVSBodyImpl? = bodies.getById(id)

    override fun getBodyReference(id: BodyId): VSRef<PhysicsVSBody> = PhysicsBodyRef(id, this)

    override fun createConstraint(constraint: VSConstraint): VSConstraintId? {
        val id = idAllocator.nextConstraintId.getAndIncrement()
        val constraintAndId = VSConstraintAndId(id, constraint)
        if (!createConstraintInternal(constraintAndId)) return null
        queues.constraintsToServer.create(constraintAndId)

        return id
    }

    override fun updateConstraint(constraintId: VSConstraintId, updatedConstraint: VSConstraint): Boolean {
        val constraintAndId = VSConstraintAndId(constraintId, updatedConstraint)
        if (!updateConstraintInternal(constraintAndId)) return false
        queues.constraintsToServer.update(constraintAndId)

        return true
    }

    override fun removeConstraint(constraintId: VSConstraintId): Boolean {
        removeConstraintInternal(constraintId)
        queues.constraintsToServer.delete(constraintId)

        return true
    }


    private fun getPhysicsWorld(dimension: DimensionId): PhysicsWorldReference = worlds.getOrPut(dimension) {
        // Try creating the physics engine
        try {
            val temp = KrunchBootstrap.createKrunchPhysicsWorld()
            // Apply physics engine settings
            KrunchBootstrap.setKrunchSettings(
                temp,
                VSCoreConfig.SERVER.physics.makeKrunchSettings()
            )
            temp
        } catch (e: Exception) {
            // Fallback to dummy physics engine if Krunch isn't supported
            e.printStackTrace()
            isUsingDummy = true
            DummyPhysicsWorldReference()
        }
    }

    /**
     * Converts the local positions of [vsConstraint] from shipyard coordinates to be relative to the center of mass of
     * the ship.
     *
     * This is used before we send the constraint to Krunch, since Krunch expects constraint positions to be relative to
     * the center of mass.
     */
    private fun adjustConstraintLocalPositions(constraint: VSConstraint): VSConstraint {
        if (constraint !is VSForceConstraint) return constraint

        val body0 = getBody(constraint.bodyId0)!!
        val body1 = getBody(constraint.bodyId1)!!

        val cm0: Vector3dc = body0.centerOfMass
        val cm1: Vector3dc = body1.centerOfMass

        // TODO: Make a helper for this
        val ship0Scaling = body0.transform.scaling.x()
        val ship1Scaling = body1.transform.scaling.x()

        // Offset force constraints by the center of mass before sending them to the physics pipeline
        // TODO: I'm not entirely sure why I have to subtract 0.5 here, but it works
        return constraint.withLocalPositions(
            cm0.mul(-1.0, Vector3d()).sub(0.5, 0.5, 0.5).add(constraint.localPos0).mul(ship0Scaling),
            cm1.mul(-1.0, Vector3d()).sub(0.5, 0.5, 0.5).add(constraint.localPos1).mul(ship1Scaling),
        )
    }

    private fun PhysicsConfig.makeKrunchSettings(): KrunchPhysicsWorldSettings {
        val settings = KrunchPhysicsWorldSettings()
        // Only use 10 sub-steps
        settings.subSteps = 10
        settings.solverType = SolverType.GAUSS_SEIDEL
        settings.iterations = 1

        // Decrease max de-penetration speed so that rigid bodies don't go
        // flying apart when they overlap
        settings.maxDePenetrationSpeed = 10.0

        settings.maxVoxelShapeCollisionPoints = lodDetail
        return settings
    }

    private val VSConstraintAndId.physConstraint get() = physConstraints.get(id)
    private val VSConstraintId.physConstraint get() = physConstraints.get(this)

    private fun VSConstraint.convertToPhysics(): ConstraintData {
        val body0Id: PhysicsBodyId = getBody(this.bodyId0)!!.phys.physicsBodyId
        val body1Id: PhysicsBodyId = getBody(this.bodyId1)!!.phys.physicsBodyId
        return when (this) {
            is AttachmentConstraint -> AttachmentConstraintData(
                body0Id, body1Id, this.compliance,
                this.localPos0, this.localPos1, this.maxForce,
                this.fixedDistance
            )

            is FixedOrientationConstraint -> FixedOrientationConstraintData(
                body0Id, body1Id, this.compliance,
                this.localRot0, this.localRot1,
                this.maxTorque
            )

            is HingeOrientationConstraint -> HingeOrientationConstraintData(
                body0Id, body1Id, this.compliance,
                this.localRot0, this.localRot1,
                this.maxTorque
            )

            is HingeSwingLimitsConstraint -> HingeSwingLimitsConstraintData(
                body0Id, body1Id, this.compliance,
                this.localRot0, this.localRot1,
                this.maxTorque, this.minSwingAngle,
                this.maxSwingAngle
            )

            is HingeTargetAngleConstraint -> HingeSwingLimitsConstraintData(
                body0Id, body1Id, this.compliance,
                this.localRot0, this.localRot1,
                this.maxTorque, this.targetAngle,
                this.nextTickTargetAngle
            )

            is PosDampingConstraint -> PosDampingConstraintData(
                body0Id, body1Id, this.compliance,
                this.localPos0, this.localPos1, this.maxForce,
                this.posDamping
            )

            is MaxDistanceConstraint -> RopeConstraintData(
                body0Id, body1Id, this.compliance,
                this.localPos0, this.localPos1, this.maxForce,
                this.maxLength
            )

            is RotDampingConstraint -> {
                val rotDampingAxes: RotDampingAxes = when (this.rotDampingAxes) {
                    VSRotDampingAxes.PARALLEL -> RotDampingAxes.PARALLEL
                    VSRotDampingAxes.PERPENDICULAR -> RotDampingAxes.PERPENDICULAR
                    VSRotDampingAxes.ALL_AXES -> RotDampingAxes.ALL_AXES
                }
                RotDampingConstraintData(
                    body0Id, body1Id, this.compliance,
                    this.localRot0, this.localRot1,
                    this.maxTorque, this.rotDamping,
                    rotDampingAxes
                )
            }

            is SlideConstraint -> SlideConstraintData(
                body0Id, body1Id, this.compliance,
                this.localPos0, this.localPos1, this.maxForce,
                this.localSlideAxis0, this.maxDistBetweenPoints
            )

            is SphericalSwingLimitsConstraint -> SphericalSwingLimitsConstraintData(
                body0Id, body1Id, this.compliance,
                this.localRot0, this.localRot1,
                this.maxTorque, this.minSwingAngle,
                this.maxSwingAngle
            )

            is SphericalTwistLimitsConstraint -> SphericalTwistLimitsConstraintData(
                body0Id, body1Id, this.compliance,
                this.localRot0, this.localRot1,
                this.maxTorque, this.minTwistAngle,
                this.maxTwistAngle
            )

            else -> {
                throw IllegalArgumentException("Unknown this type ${this::class}")
            }
        }
    }

    private fun VSConstraintId.convertToPhysics(): ConstraintId = Math.toIntExact(this)

    private fun VSConstraintAndId.convertToPhysics() =
        ConstraintAndId(this.id.convertToPhysics(), adjustConstraintLocalPositions(this.vsConstraint).convertToPhysics())

    private fun checkSameDimension(body0: PhysicsVSBody, body1: PhysicsVSBody): DimensionId {
        check(body0.dimension == body1.dimension) {
            "Body with ID ${body0.id} is in dimension ${body0.dimension} but body with ID ${body1.id} is in ${body1.dimension}"
        }
        return body0.dimension
    }
}
