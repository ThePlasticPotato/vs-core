package org.valkyrienskies.core.impl.pipelines

import org.joml.Matrix3d
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.Vector3f
import org.joml.Vector3i
import org.joml.primitives.AABBd
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint
import org.valkyrienskies.core.apigame.constraints.VSConstraint
import org.valkyrienskies.core.apigame.constraints.VSConstraintId
import org.valkyrienskies.core.apigame.constraints.VSConstraintType.ATTACHMENT
import org.valkyrienskies.core.apigame.constraints.VSConstraintType.FIXED_ATTACHMENT_ORIENTATION
import org.valkyrienskies.core.apigame.constraints.VSConstraintType.FIXED_ORIENTATION
import org.valkyrienskies.core.apigame.constraints.VSConstraintType.HINGE_ORIENTATION
import org.valkyrienskies.core.apigame.constraints.VSConstraintType.HINGE_SWING_LIMITS
import org.valkyrienskies.core.apigame.constraints.VSConstraintType.HINGE_TARGET_ANGLE
import org.valkyrienskies.core.apigame.constraints.VSConstraintType.POS_DAMPING
import org.valkyrienskies.core.apigame.constraints.VSConstraintType.ROPE
import org.valkyrienskies.core.apigame.constraints.VSConstraintType.ROT_DAMPING
import org.valkyrienskies.core.apigame.constraints.VSConstraintType.SLIDE
import org.valkyrienskies.core.apigame.constraints.VSConstraintType.SPHERICAL_SWING_LIMITS
import org.valkyrienskies.core.apigame.constraints.VSConstraintType.SPHERICAL_TWIST_LIMITS
import org.valkyrienskies.core.apigame.constraints.VSFixedOrientationConstraint
import org.valkyrienskies.core.apigame.constraints.VSHingeOrientationConstraint
import org.valkyrienskies.core.apigame.constraints.VSHingeSwingLimitsConstraint
import org.valkyrienskies.core.apigame.constraints.VSHingeTargetAngleConstraint
import org.valkyrienskies.core.apigame.constraints.VSPosDampingConstraint
import org.valkyrienskies.core.apigame.constraints.VSRopeConstraint
import org.valkyrienskies.core.apigame.constraints.VSRotDampingAxes.ALL_AXES
import org.valkyrienskies.core.apigame.constraints.VSRotDampingAxes.PARALLEL
import org.valkyrienskies.core.apigame.constraints.VSRotDampingAxes.PERPENDICULAR
import org.valkyrienskies.core.apigame.constraints.VSRotDampingConstraint
import org.valkyrienskies.core.apigame.constraints.VSSlideConstraint
import org.valkyrienskies.core.apigame.constraints.VSSphericalSwingLimitsConstraint
import org.valkyrienskies.core.apigame.constraints.VSSphericalTwistLimitsConstraint
import org.valkyrienskies.core.apigame.physics.VSBoxCollisionShapeData
import org.valkyrienskies.core.apigame.physics.VSCapsuleCollisionShapeData
import org.valkyrienskies.core.apigame.physics.VSSphereCollisionShapeData
import org.valkyrienskies.core.apigame.physics.VSVoxelCollisionShapeData
import org.valkyrienskies.core.apigame.physics.VSWheelCollisionShapeData
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.impl.config.PhysicsConfig
import org.valkyrienskies.core.impl.config.VSCoreConfig
import org.valkyrienskies.core.impl.game.BlockTypeImpl
import org.valkyrienskies.core.impl.game.ships.PhysInertia
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl
import org.valkyrienskies.core.impl.game.ships.WingPhysicsSolver
import org.valkyrienskies.core.impl.util.logger
import org.valkyrienskies.physics_api.ConstraintId
import org.valkyrienskies.physics_api.Lod1BlockRegistry
import org.valkyrienskies.physics_api.PhysicsBodyId
import org.valkyrienskies.physics_api.PhysicsBodyInertiaData
import org.valkyrienskies.physics_api.PhysicsBodyReference
import org.valkyrienskies.physics_api.PhysicsWorldReference
import org.valkyrienskies.physics_api.PoseVel
import org.valkyrienskies.physics_api.VSPhysicsFactories
import org.valkyrienskies.physics_api.VoxelShapeReference
import org.valkyrienskies.physics_api.constraints.AttachmentConstraintData
import org.valkyrienskies.physics_api.constraints.ConstraintAndId
import org.valkyrienskies.physics_api.constraints.ConstraintData
import org.valkyrienskies.physics_api.constraints.FixedOrientationConstraintData
import org.valkyrienskies.physics_api.constraints.HingeOrientationConstraintData
import org.valkyrienskies.physics_api.constraints.HingeSwingLimitsConstraintData
import org.valkyrienskies.physics_api.constraints.PosDampingConstraintData
import org.valkyrienskies.physics_api.constraints.RopeConstraintData
import org.valkyrienskies.physics_api.constraints.RotDampingAxes
import org.valkyrienskies.physics_api.constraints.RotDampingConstraintData
import org.valkyrienskies.physics_api.constraints.SlideConstraintData
import org.valkyrienskies.physics_api.constraints.SphericalSwingLimitsConstraintData
import org.valkyrienskies.physics_api.constraints.SphericalTwistLimitsConstraintData
import org.valkyrienskies.physics_api.dummy_impl.DummyLod1BlockRegistry
import org.valkyrienskies.physics_api.dummy_impl.DummyPhysicsWorldReference
import org.valkyrienskies.physics_api.dummy_impl.DummyVSPhysicsFactories
import org.valkyrienskies.physics_api.voxel.CollisionPoint
import org.valkyrienskies.physics_api.voxel.Lod1LiquidBlockState
import org.valkyrienskies.physics_api.voxel.Lod1SolidBlockState
import org.valkyrienskies.physics_api.voxel.Lod1SolidBoxesCollisionShape
import org.valkyrienskies.physics_api.voxel.LodBlockBoundingBox
import org.valkyrienskies.physics_api.voxel.updates.DeleteVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel.updates.DenseVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel.updates.EmptyVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel.updates.IVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel.updates.SparseVoxelShapeUpdate
import org.valkyrienskies.physics_api_krunch.KrunchBootstrap
import org.valkyrienskies.physics_api_krunch.KrunchPhysicsWorldSettings
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

class VSPhysicsPipelineStage @Inject constructor() {
    private val gameFramesQueue: ConcurrentLinkedQueue<VSGameFrame> = ConcurrentLinkedQueue()
    private val physicsEngines: MutableMap<DimensionId, PhysicsWorldReference> = mutableMapOf()

    // Map dimension to ship ids to rigid bodies, and map rigid bodies to ship ids
    private val dimensionToShipIdToPhysShip: MutableMap<DimensionId, MutableMap<ShipId, PhysShipImpl>> = HashMap()
    private val shipIdToDimension: MutableMap<ShipId, DimensionId> = HashMap()
    private val constraintIdToDimension: MutableMap<VSConstraintId, DimensionId> = HashMap()
    private var physTick = 0

    private var pendingUpdates: MutableList<Pair<Pair<ShipId, DimensionId>, List<IVoxelShapeUpdate>>> = ArrayList()

    private val settings = VSCoreConfig.SERVER.physics.makeKrunchSettings()
    private val factories: VSPhysicsFactories
    // TODO: Register blocks to lod1BlockRegistry
    private val lod1BlockRegistry: Lod1BlockRegistry
    private var hasBeenDeleted = false

    var isUsingDummy = false
        private set

    private val physicsWorldFactory: () -> PhysicsWorldReference

    init {
        var factoriesTemp: VSPhysicsFactories
        var lod1BlockRegistryTemp: Lod1BlockRegistry
        var physicsWorldFactoryTemp: () -> PhysicsWorldReference
        // Try creating the physics engine
        try {
            physicsWorldFactoryTemp = {
                val physicsWorld = KrunchBootstrap.createKrunchPhysicsWorld()
                // Apply physics engine settings
                KrunchBootstrap.setKrunchSettings(
                    physicsWorld,
                    settings
                )
                physicsWorld
            }

            factoriesTemp = KrunchBootstrap.getKrunchFactories()
            val vsByteBuffer = factoriesTemp.vsByteBufferFactory.createVSByteBuffer(1000000)
            lod1BlockRegistryTemp = factoriesTemp.lod1BlockRegistryFactory.createLod1BlockRegistry(vsByteBuffer)

            // TODO: Register blocks properly somewhere else
            // Register basic blocks
            val fullLodBoundingBox = LodBlockBoundingBox.createVSBoundingBox(0, 0, 0, 15, 15, 15)
            val fullBlockCollisionPoints = listOf(
                CollisionPoint(Vector3f(.25f, .25f, .25f), .25f),
                CollisionPoint(Vector3f(.25f, .25f, .75f), .25f),
                CollisionPoint(Vector3f(.25f, .75f, .25f), .25f),
                CollisionPoint(Vector3f(.25f, .75f, .75f), .25f),
                CollisionPoint(Vector3f(.75f, .25f, .25f), .25f),
                CollisionPoint(Vector3f(.75f, .25f, .75f), .25f),
                CollisionPoint(Vector3f(.75f, .75f, .25f), .25f),
                CollisionPoint(Vector3f(.75f, .75f, .75f), .25f),
            )

            val solidBlockState = Lod1SolidBlockState(
                collisionShape = Lod1SolidBoxesCollisionShape(
                    overallBoundingBox = fullLodBoundingBox,
                    collisionPoints = fullBlockCollisionPoints,
                    solidBoxes = listOf(fullLodBoundingBox),
                    negativeBoxes = listOf(),
                ),
                elasticity = 0.3f,
                friction = 1.0f,
                hardness = 1.0f,
                lod1SolidBlockStateId = BlockTypeImpl.SOLID.toInt(),
            )

            val waterBlockState = Lod1LiquidBlockState(
                boundingBox = fullLodBoundingBox,
                density = 1000.0f,
                dragCoefficient = 0.3f,
                fluidVel = Vector3f(),
                lod1LiquidBlockStateId = BlockTypeImpl.WATER.toInt(),
            )

            val lavaBlockState = Lod1LiquidBlockState(
                boundingBox = fullLodBoundingBox,
                density = 10000.0f,
                dragCoefficient = 0.3f,
                fluidVel = Vector3f(),
                lod1LiquidBlockStateId = BlockTypeImpl.LAVA.toInt(),
            )

            // Register solid/liquid states
            lod1BlockRegistryTemp.registerLod1SolidBlockState(solidBlockState, vsByteBuffer)
            lod1BlockRegistryTemp.registerLod1LiquidBlockState(waterBlockState, vsByteBuffer)
            lod1BlockRegistryTemp.registerLod1LiquidBlockState(lavaBlockState, vsByteBuffer)

            // Register block states
            lod1BlockRegistryTemp.registerLod1BlockState(solidBlockState.lod1SolidBlockStateId, Lod1BlockRegistry.LIQUID_AIR_BLOCK_STATE_ID, BlockTypeImpl.SOLID.toInt())
            lod1BlockRegistryTemp.registerLod1BlockState(Lod1BlockRegistry.SOLID_AIR_BLOCK_STATE_ID, waterBlockState.lod1LiquidBlockStateId, BlockTypeImpl.WATER.toInt())
            lod1BlockRegistryTemp.registerLod1BlockState(Lod1BlockRegistry.SOLID_AIR_BLOCK_STATE_ID, lavaBlockState.lod1LiquidBlockStateId, BlockTypeImpl.LAVA.toInt())
        } catch (e: Exception) {
            // Fallback to dummy physics engine if Krunch isn't supported
            e.printStackTrace()
            isUsingDummy = true
            factoriesTemp = DummyVSPhysicsFactories
            lod1BlockRegistryTemp = DummyLod1BlockRegistry()
            physicsWorldFactoryTemp = ::DummyPhysicsWorldReference
        }
        factories = factoriesTemp
        lod1BlockRegistry = lod1BlockRegistryTemp
        physicsWorldFactory = physicsWorldFactoryTemp
    }

    /**
     * Push a game frame to the physics engine stage
     */
    fun pushGameFrame(gameFrame: VSGameFrame) {
        if (gameFramesQueue.size >= 100) {
            logger.warn("Too many game frames in the game frame queue. Is the physics stage broken?")
            Thread.sleep(1000L)
        }
        gameFramesQueue.add(gameFrame)
    }

    /**
     * Process queued game frames, tick the physics, then create a new physics frame
     */
    fun tickPhysics(gravity: Vector3dc, timeStep: Double, simulatePhysics: Boolean): VSPhysicsFrame {
        // Apply game frames
        while (gameFramesQueue.isNotEmpty()) {
            val gameFrame = gameFramesQueue.remove()
            applyGameFrame(gameFrame)
        }

        // Update the [poseVel] stored in [PhysShip]
        dimensionToShipIdToPhysShip.values.forEach { shipIdToPhysShip ->
            shipIdToPhysShip.values.forEach {
                it.poseVel = it.rigidBodyReference.poseVel
                // TODO: In the future update the segment tracker too, probably do this after we've added portals to Krunch
                // it.segments = it.rigidBodyReference.segments
            }
        }

        // Compute and apply forces/torques for ships
        dimensionToShipIdToPhysShip.values.forEach { shipIdToPhysShip ->
            shipIdToPhysShip.values.forEach { ship ->
                ship.forceInducers.forEach {
                    it.applyForces(ship)
                    it.applyForcesAndLookupPhysShips(ship) { shipId -> shipIdToPhysShip[shipId] }
                }
                // region Wing physics
                val shipTransform = ship.transform
                val poseVel = ship.poseVel
                val wingManager = ship.wingManager
                val momentOfInertia = ship._inertia.momentOfInertiaTensor

                val (force, torque) = WingPhysicsSolver.applyWingForces(
                    shipTransform, poseVel, wingManager, momentOfInertia
                )
                ship.applyInvariantForce(force)
                ship.applyInvariantTorque(torque)
                // endregion
                ship.applyQueuedForces()
            }
        }

        // Run the physics engine
        tickTemp(gravity, timeStep, simulatePhysics)

        // Return a new physics frame
        return createPhysicsFrame()
    }

    // TODO: Optimize this to be multi threaded
    private fun tickTemp(gravity: Vector3dc, timeStep: Double, simulatePhysics: Boolean) {
        physicsEngines.forEach { (dimension, physicsEngine) ->
            val islands = physicsEngine.preStep(gravity, timeStep)
            val subTimeStep = timeStep / settings.subSteps
            islands.forEach { island ->
                island.preStep(gravity, timeStep)
                if (simulatePhysics) {
                    for (i in 0 until settings.subSteps) {
                        island.subStep(gravity, subTimeStep, i.toDouble() / settings.subSteps.toDouble())
                    }
                }
                island.postStep()
            }
            physicsEngine.postStep()
        }
    }

    fun deleteResources() {
        println("Deleting VSPhysicsPipelineStage resources!")
        if (hasBeenDeleted) throw IllegalStateException("Physics engine has already been deleted!")
        physicsEngines.forEach { (_, physicsEngine) ->
            physicsEngine.deletePhysicsWorldResources()
        }
        // TODO: Also send updates to delete entire dimensions
        // Close all collision shape resources (this isn't strictly necessary because the gc should clean this up, but it's better to do this sooner than waiting for gc)
        dimensionToShipIdToPhysShip.forEach { (_, shipIdToPhysShip) ->
            shipIdToPhysShip.forEach { (_, physShip) ->
                physShip.rigidBodyReference.collisionShape?.close()
            }
        }
        dimensionToShipIdToPhysShip.clear()
        shipIdToDimension.clear()
        constraintIdToDimension.clear()
        pendingUpdates.clear()

        physicsEngines.clear()
        lod1BlockRegistry.close()
        hasBeenDeleted = true
    }

    private fun applyGameFrame(gameFrame: VSGameFrame) {
        // Delete deleted ships
        gameFrame.deletedShips.forEach { deletedShipId ->
            val dimensionId = shipIdToDimension[deletedShipId]
            val physicsEngine = physicsEngines[dimensionId]!!
            val shipIdToPhysShip = dimensionToShipIdToPhysShip[dimensionId]!!
            val shipRigidBodyReferenceAndId = shipIdToPhysShip[deletedShipId]
                ?: throw IllegalStateException(
                    "Tried deleting rigid body from ship with UUID $deletedShipId," +
                        " but no rigid body exists for this ship!"
                )

            val shipRigidBodyReference = shipRigidBodyReferenceAndId.rigidBodyReference
            physicsEngine.deleteRigidBody(shipRigidBodyReference.physicsBodyId)
            shipIdToPhysShip.remove(deletedShipId)
            shipIdToDimension.remove(deletedShipId)
        }

        // Create new ships
        gameFrame.newShips.forEach { newShipInGameFrameData ->
            val shipId = newShipInGameFrameData.uuid
            val dimensionId = newShipInGameFrameData.dimension
            shipIdToDimension[shipId] = dimensionId
            // Create a new dimension
            // TODO: Create dedicated updates for creating/deleting dimensions
            if (!(dimensionToShipIdToPhysShip.containsKey(dimensionId))) {
                dimensionToShipIdToPhysShip[dimensionId] = HashMap()
                val newPhysicsWorld = physicsWorldFactory()
                physicsEngines[dimensionId] = newPhysicsWorld
            }
            val shipIdToPhysShip = dimensionToShipIdToPhysShip[dimensionId]!!

            if (shipIdToPhysShip.containsKey(shipId)) {
                throw IllegalStateException(
                    "Tried creating rigid body from ship with UUID $shipId," +
                        " but a rigid body already exists for this ship!"
                )
            }

            val physicsEngine = physicsEngines[dimensionId]!!
            val newRigidBodyReference: PhysicsBodyReference<*>

            when (val collisionShapeData = newShipInGameFrameData.collisionShapeData) {
                is VSVoxelCollisionShapeData -> {
                    val minDefined = collisionShapeData.minDefined
                    val maxDefined = collisionShapeData.maxDefined
                    val totalVoxelRegion = collisionShapeData.totalVoxelRegion
                    val shipVoxelsFullyLoaded = collisionShapeData.shipVoxelsFullyLoaded
                    val voxelCollisionShape = factories.collisionShapeFactory.makeVoxelShapeReference(minDefined, maxDefined, totalVoxelRegion, lod1BlockRegistry)
                    voxelCollisionShape.isVoxelTerrainFullyLoaded = shipVoxelsFullyLoaded
                    newRigidBodyReference = physicsEngine.createRigidBody(voxelCollisionShape)
                }
                is VSSphereCollisionShapeData -> {
                    val sphereCollisionShape = factories.collisionShapeFactory.makeSphereShapeReference(collisionShapeData.radius)
                    newRigidBodyReference = physicsEngine.createRigidBody(sphereCollisionShape)
                }
                is VSWheelCollisionShapeData -> {
                    val wheelCollisionShape = factories.collisionShapeFactory.makeWheelShapeReference(collisionShapeData.wheelRadius, collisionShapeData.halfThickness)
                    newRigidBodyReference = physicsEngine.createRigidBody(wheelCollisionShape)
                }
                is VSBoxCollisionShapeData -> {
                    val boxCollisionShape = factories.collisionShapeFactory.makeBoxShapeReference(Vector3d(collisionShapeData.lengthX, collisionShapeData.lengthY, collisionShapeData.lengthZ))
                    newRigidBodyReference = physicsEngine.createRigidBody(boxCollisionShape)
                }
                is VSCapsuleCollisionShapeData -> {
                    val capsuleCollisionShape = factories.collisionShapeFactory.makeCapsuleShapeReference(collisionShapeData.radius, collisionShapeData.length)
                    newRigidBodyReference = physicsEngine.createRigidBody(capsuleCollisionShape)
                }
                else -> throw IllegalArgumentException("What is newShipInGameFrameData.collisionShapeData? ${newShipInGameFrameData.collisionShapeData}")
            }

            val inertiaData = newShipInGameFrameData.inertiaData
            val poseVel = newShipInGameFrameData.poseVel
            val isStatic = newShipInGameFrameData.isStatic

            val wingManagerChanges = newShipInGameFrameData.wingManagerChanges
            val shipTeleportId = newShipInGameFrameData.shipTeleportId

            newRigidBodyReference.inertiaData = physInertiaToRigidBodyInertiaData(inertiaData)
            newRigidBodyReference.poseVel = poseVel
            newRigidBodyReference.collisionShapeOffset = newShipInGameFrameData.collisionShapeOffset
            newRigidBodyReference.collisionShapeScaling = newShipInGameFrameData.collisionShapeScaling
            newRigidBodyReference.isStatic = isStatic

            val physShip =
                PhysShipImpl(
                    shipId,
                    newRigidBodyReference,
                    newShipInGameFrameData.forcesInducers,
                    inertiaData,
                    poseVel,
                    shipTeleportId,
                    isStatic = isStatic,
                )
            if (wingManagerChanges != null) {
                physShip.wingManager.applyChanges(wingManagerChanges)
            }
            shipIdToPhysShip[shipId] = physShip
        }

        // Update existing ships
        gameFrame.updatedShips.forEach { (shipId, shipUpdate) ->
            val dimensionId = shipIdToDimension[shipId]
            val shipIdToPhysShip = dimensionToShipIdToPhysShip[dimensionId]!!

            val physShip = shipIdToPhysShip[shipId]
                ?: throw IllegalStateException(
                    "Tried updating rigid body from ship with UUID $shipId, but no rigid body exists for this ship!"
                )

            val shipRigidBody = physShip.rigidBodyReference
            val oldPoseVel = shipRigidBody.poseVel

            val oldVoxelOffset = shipRigidBody.collisionShapeOffset
            val newVoxelOffset = shipUpdate.collisionShapeOffset
            val deltaVoxelOffset = oldPoseVel.rot.transform(newVoxelOffset.sub(oldVoxelOffset, Vector3d()))
            deltaVoxelOffset.mul(shipUpdate.collisionShapeScaling)
            val isStatic = shipUpdate.isStatic
            val shipVoxelsFullyLoaded = shipUpdate.shipVoxelsFullyLoaded
            val wingManagerChanges = shipUpdate.wingManagerChanges
            val shipTeleportId = shipUpdate.shipTeleportId
            val currentShipPos = shipUpdate.currentShipPos
            val currentShipRot = shipUpdate.currentShipRot
            val currentShipVel = shipUpdate.currentShipVel
            val currentShipOmega = shipUpdate.currentShipOmega
            val updatePoseVelFromGame = physShip.lastShipTeleportId != shipTeleportId

            val newShipPoseVel = if (!updatePoseVelFromGame) {
                PoseVel(
                    oldPoseVel.pos.sub(deltaVoxelOffset, Vector3d()), oldPoseVel.rot, oldPoseVel.vel, oldPoseVel.omega
                )
            } else {
                PoseVel(
                    currentShipPos, currentShipRot, currentShipVel, currentShipOmega
                )
            }

            physShip._inertia = shipUpdate.inertiaData
            physShip.forceInducers = shipUpdate.forcesInducers
            physShip.poseVel = newShipPoseVel

            shipRigidBody.collisionShapeOffset = newVoxelOffset
            shipRigidBody.poseVel = newShipPoseVel
            shipRigidBody.inertiaData = physInertiaToRigidBodyInertiaData(shipUpdate.inertiaData)
            shipRigidBody.collisionShapeScaling = shipUpdate.collisionShapeScaling
            shipRigidBody.isStatic = isStatic

            val collisionShapeCopy = shipRigidBody.collisionShape
            if (collisionShapeCopy is VoxelShapeReference) {
                collisionShapeCopy.isVoxelTerrainFullyLoaded = shipVoxelsFullyLoaded
            }

            if (wingManagerChanges != null) {
                physShip.wingManager.applyChanges(wingManagerChanges)
            }

            physShip.lastShipTeleportId = shipTeleportId
            physShip.isStatic = isStatic
        }

        // Send voxel updates
        gameFrame.voxelUpdatesMap.forEach { (shipId, voxelUpdatesList) ->
            val dimensionId = shipIdToDimension[shipId]!!
            val shipIdToPhysShip = dimensionToShipIdToPhysShip[dimensionId]!!

            val shipRigidBodyReferenceAndId = shipIdToPhysShip[shipId]
                ?: throw IllegalStateException(
                    "Tried sending voxel updates to rigid body from ship with UUID $shipId," +
                        " but no rigid body exists for this ship!"
                )
            val shipRigidBodyReference = shipRigidBodyReferenceAndId.rigidBodyReference
            if (false && !shipRigidBodyReference.isStatic) {
                // TODO: Re-implement this later
                // Always process updates to non-static ships immediately
                // val voxelRigidBodyShapeUpdates =
                //     VoxelRigidBodyShapeUpdates(shipRigidBodyReference.rigidBodyId, voxelUpdatesList.toTypedArray())
                // physicsEngine.queueVoxelShapeUpdates(arrayOf(voxelRigidBodyShapeUpdates))
            } else {
                // Queue updates to static ships for later
                pendingUpdates.add(Pair(Pair(shipId, dimensionId), voxelUpdatesList))
            }
        }

        // region Create/Update/Delete constraints
        gameFrame.constraintsCreatedThisTick.forEach { vsConstraintAndId ->
            val dimensionId = shipIdToDimension[vsConstraintAndId.vsConstraint.shipId0]!!
            constraintIdToDimension[vsConstraintAndId.constraintId] = dimensionId
            val physicsEngine = physicsEngines[dimensionId]!!
            physicsEngine.addConstraint(
                ConstraintAndId(
                    vsConstraintAndId.constraintId,
                    convertVSConstraintToPhysicsConstraint(vsConstraintAndId.vsConstraint, dimensionId)
                )
            )
        }
        gameFrame.constraintsUpdatedThisTick.forEach { vsConstraintAndId ->
            val dimensionId = shipIdToDimension[vsConstraintAndId.vsConstraint.shipId0]!!
            val physicsEngine = physicsEngines[dimensionId]!!
            physicsEngine.updateConstraint(
                ConstraintAndId(
                    vsConstraintAndId.constraintId,
                    convertVSConstraintToPhysicsConstraint(vsConstraintAndId.vsConstraint, dimensionId)
                )
            )
        }
        gameFrame.constraintsDeletedThisTick.forEach { vsConstraintId ->
            val dimensionId = constraintIdToDimension[vsConstraintId]!!
            val physicsEngine = physicsEngines[dimensionId]!!
            physicsEngine.removeConstraint(convertVSConstraintIdToConstraintId(vsConstraintId))
            constraintIdToDimension.remove(vsConstraintId)
        }
        // endregion

        // region Send updates to static ships, staggered to limit number of updates per tick
        val vsByteBuffer = factories.vsByteBufferFactory.createVSByteBuffer(1000000)
        for (i in 0 until pendingUpdates.size) {
            val curUpdate = pendingUpdates[i]
            val (shipId, physicsEngineId) = curUpdate.first
            val shipIdToPhysShip = dimensionToShipIdToPhysShip[physicsEngineId]!!
            val shipRigidBodyReferenceAndId = shipIdToPhysShip[shipId]
                ?: throw IllegalStateException(
                    "Tried sending voxel updates to rigid body from ship with UUID $shipId," +
                        " but no rigid body exists for this ship!"
                )
            val shipRigidBodyReference = shipRigidBodyReferenceAndId.rigidBodyReference
            val voxelShape = shipRigidBodyReference.collisionShape as VoxelShapeReference

            // Send all of it
            for (update in curUpdate.second) {
                when (update) {
                    is DeleteVoxelShapeUpdate -> {
                        voxelShape.deleteChunk(Vector3i(update.regionX, update.regionY, update.regionZ))
                    }
                    is EmptyVoxelShapeUpdate -> {
                        val alreadyExists = voxelShape.voxel16ChunkExists(Vector3i(update.regionX, update.regionY, update.regionZ))
                        if (!alreadyExists || update.overwriteExistingVoxels) {
                            voxelShape.insertAirChunk(
                                Vector3i(update.regionX, update.regionY, update.regionZ)
                            )
                        }
                    }
                    is DenseVoxelShapeUpdate -> {
                        val voxelChunk16 = factories.voxelChunk16Factory.createEmptyVoxelChunk16(lod1BlockRegistry)
                        voxelChunk16.queueUpdate(update)
                        voxelChunk16.bakeChunk(vsByteBuffer)
                        voxelShape.insertChunk(Vector3i(update.regionX, update.regionY, update.regionZ), voxelChunk16)
                    }
                    is SparseVoxelShapeUpdate -> {
                        var voxelChunk16 = voxelShape.copyVoxel16Chunk(Vector3i(update.regionX, update.regionY, update.regionZ))
                        if (voxelChunk16 == null) {
                            voxelChunk16 = factories.voxelChunk16Factory.createEmptyVoxelChunk16(lod1BlockRegistry)
                        }
                        voxelChunk16.queueUpdate(update)
                        voxelChunk16.bakeChunk(vsByteBuffer)
                        voxelShape.insertChunk(Vector3i(update.regionX, update.regionY, update.regionZ), voxelChunk16)
                    }
                }
            }
            // Bake changes
            voxelShape.bakeVoxelShape()
        }
        pendingUpdates = ArrayList()
        // endregion
    }

    private fun createPhysicsFrame(): VSPhysicsFrame {
        val shipDataMap: MutableMap<ShipId, ShipInPhysicsFrameData> = HashMap()
        // For now the physics doesn't send voxel updates, but it will in the future
        val voxelUpdatesMap: Map<ShipId, List<IVoxelShapeUpdate>> = emptyMap()
        dimensionToShipIdToPhysShip.values.forEach { shipIdToPhysShip ->
            shipIdToPhysShip.forEach { (shipId, shipIdAndRigidBodyReference) ->
                val rigidBodyReference = shipIdAndRigidBodyReference.rigidBodyReference
                val inertiaData: PhysicsBodyInertiaData = rigidBodyReference.inertiaData
                val poseVel: PoseVel = rigidBodyReference.poseVel
                val shipVoxelOffset: Vector3dc = rigidBodyReference.collisionShapeOffset
                val scaling = rigidBodyReference.collisionShapeScaling
                val aabb = AABBd()
                rigidBodyReference.getAABB(aabb)
                val lastShipTeleportId: Int = shipIdAndRigidBodyReference.lastShipTeleportId

                shipDataMap[shipId] =
                    ShipInPhysicsFrameData(
                        shipId, inertiaData, poseVel, shipVoxelOffset, scaling, aabb, lastShipTeleportId
                    )
            }
        }
        return VSPhysicsFrame(shipDataMap, voxelUpdatesMap, physTick++)
    }

    private fun PhysicsConfig.makeKrunchSettings(): KrunchPhysicsWorldSettings {
        val settings = KrunchPhysicsWorldSettings()

        // Set settings based on physicsConfig
        settings.subSteps = subSteps
        settings.solverType = solver
        settings.iterations = iterations
        settings.maxDePenetrationSpeed = maxDePenetrationSpeed
        settings.maxVoxelShapeCollisionPoints = lodDetail

        return settings
    }

    private fun convertVSConstraintToPhysicsConstraint(vsConstraint: VSConstraint, dimensionId: DimensionId): ConstraintData {
        val shipIdToPhysShip = dimensionToShipIdToPhysShip[dimensionId]!!
        // Shit, do we allow shipIdToPhysShip to store ships from different physics engines?
        val body0Id: PhysicsBodyId = shipIdToPhysShip[vsConstraint.shipId0]!!.rigidBodyReference.physicsBodyId
        val body1Id: PhysicsBodyId = shipIdToPhysShip[vsConstraint.shipId1]!!.rigidBodyReference.physicsBodyId
        return when (vsConstraint.constraintType) {
            ATTACHMENT -> {
                val attachmentConstraint = vsConstraint as VSAttachmentConstraint
                AttachmentConstraintData(
                    body0Id, body1Id, attachmentConstraint.compliance,
                    attachmentConstraint.localPos0, attachmentConstraint.localPos1, attachmentConstraint.maxForce,
                    true, attachmentConstraint.fixedDistance
                )
            }

            FIXED_ATTACHMENT_ORIENTATION -> {
                TODO("Not implemented")
            }

            FIXED_ORIENTATION -> {
                val fixedOrientationConstraint = vsConstraint as VSFixedOrientationConstraint
                FixedOrientationConstraintData(
                    body0Id, body1Id, fixedOrientationConstraint.compliance,
                    fixedOrientationConstraint.localRot0, fixedOrientationConstraint.localRot1,
                    fixedOrientationConstraint.maxTorque, true
                )
            }

            HINGE_ORIENTATION -> {
                val hingeOrientationConstraint = vsConstraint as VSHingeOrientationConstraint
                HingeOrientationConstraintData(
                    body0Id, body1Id, hingeOrientationConstraint.compliance,
                    hingeOrientationConstraint.localRot0, hingeOrientationConstraint.localRot1,
                    hingeOrientationConstraint.maxTorque, true
                )
            }

            HINGE_SWING_LIMITS -> {
                val hingeSwingLimitsConstraint = vsConstraint as VSHingeSwingLimitsConstraint
                HingeSwingLimitsConstraintData(
                    body0Id, body1Id, hingeSwingLimitsConstraint.compliance,
                    hingeSwingLimitsConstraint.localRot0, hingeSwingLimitsConstraint.localRot1,
                    hingeSwingLimitsConstraint.maxTorque, true, hingeSwingLimitsConstraint.minSwingAngle,
                    hingeSwingLimitsConstraint.maxSwingAngle
                )
            }

            HINGE_TARGET_ANGLE -> {
                val hingeTargetAngleConstraint = vsConstraint as VSHingeTargetAngleConstraint
                HingeSwingLimitsConstraintData(
                    body0Id, body1Id, hingeTargetAngleConstraint.compliance,
                    hingeTargetAngleConstraint.localRot0, hingeTargetAngleConstraint.localRot1,
                    hingeTargetAngleConstraint.maxTorque, true, hingeTargetAngleConstraint.targetAngle,
                    hingeTargetAngleConstraint.nextTickTargetAngle
                )
            }

            POS_DAMPING -> {
                val posDampingConstraint = vsConstraint as VSPosDampingConstraint
                PosDampingConstraintData(
                    body0Id, body1Id, posDampingConstraint.compliance,
                    posDampingConstraint.localPos0, posDampingConstraint.localPos1, posDampingConstraint.maxForce,
                    true, posDampingConstraint.posDamping
                )
            }

            ROPE -> {
                val ropeConstraint = vsConstraint as VSRopeConstraint
                RopeConstraintData(
                    body0Id, body1Id, ropeConstraint.compliance,
                    ropeConstraint.localPos0, ropeConstraint.localPos1, ropeConstraint.maxForce, true,
                    ropeConstraint.ropeLength
                )
            }

            ROT_DAMPING -> {
                val rotDampingConstraint = vsConstraint as VSRotDampingConstraint
                val rotDampingAxes: RotDampingAxes = when (rotDampingConstraint.rotDampingAxes) {
                    PARALLEL -> RotDampingAxes.PARALLEL
                    PERPENDICULAR -> RotDampingAxes.PERPENDICULAR
                    ALL_AXES -> RotDampingAxes.ALL_AXES
                }
                RotDampingConstraintData(
                    body0Id, body1Id, rotDampingConstraint.compliance,
                    rotDampingConstraint.localRot0, rotDampingConstraint.localRot1,
                    rotDampingConstraint.maxTorque, true, rotDampingConstraint.rotDamping,
                    rotDampingAxes
                )
            }

            SLIDE -> {
                val slideConstraint = vsConstraint as VSSlideConstraint
                SlideConstraintData(
                    body0Id, body1Id, slideConstraint.compliance,
                    slideConstraint.localPos0, slideConstraint.localPos1, slideConstraint.maxForce, true,
                    slideConstraint.localSlideAxis0, slideConstraint.maxDistBetweenPoints
                )
            }

            SPHERICAL_SWING_LIMITS -> {
                val sphericalSwingLimitsConstraint = vsConstraint as VSSphericalSwingLimitsConstraint
                SphericalSwingLimitsConstraintData(
                    body0Id, body1Id, sphericalSwingLimitsConstraint.compliance,
                    sphericalSwingLimitsConstraint.localRot0, sphericalSwingLimitsConstraint.localRot1,
                    sphericalSwingLimitsConstraint.maxTorque, true, sphericalSwingLimitsConstraint.minSwingAngle,
                    sphericalSwingLimitsConstraint.maxSwingAngle
                )
            }

            SPHERICAL_TWIST_LIMITS -> {
                val sphericalTwistLimitsConstraint = vsConstraint as VSSphericalTwistLimitsConstraint
                SphericalTwistLimitsConstraintData(
                    body0Id, body1Id, sphericalTwistLimitsConstraint.compliance,
                    sphericalTwistLimitsConstraint.localRot0, sphericalTwistLimitsConstraint.localRot1,
                    sphericalTwistLimitsConstraint.maxTorque, true, sphericalTwistLimitsConstraint.minTwistAngle,
                    sphericalTwistLimitsConstraint.maxTwistAngle
                )
            }

            else -> throw IllegalArgumentException("Unknown constraint type ${vsConstraint.constraintType}")
        }
    }

    private fun convertVSConstraintIdToConstraintId(vsConstraintId: VSConstraintId): ConstraintId = vsConstraintId

    companion object {
        private fun physInertiaToRigidBodyInertiaData(inertia: PhysInertia): PhysicsBodyInertiaData {
            val invMass = 1.0 / inertia.shipMass
            if (!invMass.isFinite())
                throw IllegalStateException("invMass is not finite!")

            val invInertiaMatrix = inertia.momentOfInertiaTensor.invert(Matrix3d())
            if (!invInertiaMatrix.isFinite)
                throw IllegalStateException("invInertiaMatrix is not finite!")

            return PhysicsBodyInertiaData(invMass, invInertiaMatrix)
        }

        private const val MAX_UPDATES_PER_PHYS_TICK = 1000

        // Store up to 60 physics ticks worth of voxel updates before we force Krunch to process them more quickly
        private const val MAX_PENDING_UPDATES_SIZE = MAX_UPDATES_PER_PHYS_TICK * 60

        private val logger by logger()
    }
}
