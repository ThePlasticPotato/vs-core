package org.valkyrienskies.core.impl.pipelines

import org.joml.Matrix3d
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.physics.constraints.*
import org.valkyrienskies.core.api.physics.constraints.VSRotDampingAxes.*
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.impl.config.PhysicsConfig
import org.valkyrienskies.core.impl.config.VSCoreConfig
import org.valkyrienskies.core.impl.game.ships.PhysInertia
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl
import org.valkyrienskies.core.impl.util.logger
import org.valkyrienskies.physics_api.*
import org.valkyrienskies.physics_api.constraints.*
import org.valkyrienskies.physics_api.dummy_impl.DummyPhysicsWorldReference
import org.valkyrienskies.physics_api.voxel.updates.IVoxelShapeUpdate
import org.valkyrienskies.physics_api_krunch.KrunchBootstrap
import org.valkyrienskies.physics_api_krunch.KrunchPhysicsWorldSettings
import org.valkyrienskies.physics_api_krunch.SolverType.GAUSS_SEIDEL
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@OptIn(VSBeta::class)
class VSPhysicsPipelineStage @Inject constructor() {
    private val gameFramesQueue: ConcurrentLinkedQueue<VSGameFrame> = ConcurrentLinkedQueue()
    private val physicsEngines: HashMap<Int, PhysicsWorldReference> = HashMap()

    // Map ships ids to rigid bodies, and map rigid bodies to ship ids
    private val shipIdToPhysShip: MutableMap<ShipId, PhysShipImpl> = HashMap()
    private var physTick = 0

    private var pendingUpdates: MutableList<Pair<ShipId, List<IVoxelShapeUpdate>>> = ArrayList()
    private var pendingUpdatesSize: Int = 0

    private var hasBeenDeleted = false
    private val constraintIdToDimension: MutableMap<VSConstraintId, Int> = HashMap()

    var isUsingDummy = false
        private set

    private fun getOrCreatePhysicsWorld(dimension: Int): PhysicsWorldReference {
        return physicsEngines.getOrPut(dimension) {
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
        shipIdToPhysShip.values.forEach {
            it.poseVel = it.rigidBodyReference.poseVel
        }

        // Compute and apply forces/torques for ships
        shipIdToPhysShip.values.forEach { ship ->
            // TODO replace with better api
            ship.forceInducers.forEach { it.applyForces(ship) }
            ship.applyQueuedForces()
        }
        
        // TODO Tick physics tick hooks

        // Run the physics engines in parallel
        physicsEngines.entries.parallelStream().forEach { entry ->
            entry.value.tick(gravity, timeStep, simulatePhysics)
        }

        // Return a new physics frame
        return createPhysicsFrame()
    }

    fun deleteResources() {
        if (hasBeenDeleted) throw IllegalStateException("Physics engine has already been deleted!")
        physicsEngines.forEach { (_, physicsWorld) ->
            physicsWorld.deletePhysicsWorldResources()
        }
        physicsEngines.clear()
        hasBeenDeleted = true
    }

    private fun applyGameFrame(gameFrame: VSGameFrame) {
        // Delete deleted ships
        gameFrame.deletedBodies.forEach { deletedShipId ->
            val shipRigidBodyReferenceAndId = shipIdToPhysShip[deletedShipId]
                ?: throw IllegalStateException(
                    "Tried deleting rigid body from ship with UUID $deletedShipId," +
                        " but no rigid body exists for this ship!"
                )

            val shipRigidBodyReference = shipRigidBodyReferenceAndId.rigidBodyReference
            physicsEngines[shipRigidBodyReferenceAndId.dimension]!!.deleteRigidBody(shipRigidBodyReference.physicsBodyId)
            shipIdToPhysShip.remove(deletedShipId)
        }

        // Create new ships
        gameFrame.newBodies.forEach { newShipInGameFrameData ->
            val shipId = newShipInGameFrameData.uuid
            if (shipIdToPhysShip.containsKey(shipId)) {
                throw IllegalStateException(
                    "Tried creating rigid body from ship with UUID $shipId," +
                        " but a rigid body already exists for this ship!"
                )
            }
            val dimension = newShipInGameFrameData.dimension
            val minDefined = newShipInGameFrameData.minDefined
            val maxDefined = newShipInGameFrameData.maxDefined
            val totalVoxelRegion = newShipInGameFrameData.totalVoxelRegion
            val inertiaData = newShipInGameFrameData.inertiaData
            val poseVel = newShipInGameFrameData.poseVel
            val isStatic = newShipInGameFrameData.isStatic
            val shipVoxelsFullyLoaded = newShipInGameFrameData.shipVoxelsFullyLoaded

            val physicsEngine = getOrCreatePhysicsWorld(dimension)
            val voxelShape = physicsEngine.makeVoxelShapeReference(minDefined, maxDefined, totalVoxelRegion)
            val newRigidBodyReference = physicsEngine.createRigidBody(voxelShape)

            newRigidBodyReference.inertiaData = physInertiaToRigidBodyInertiaData(inertiaData)
            newRigidBodyReference.poseVel = poseVel
            newRigidBodyReference.collisionShapeOffset = newShipInGameFrameData.voxelOffset
            newRigidBodyReference.collisionShapeScaling = newShipInGameFrameData.shipScaling
            newRigidBodyReference.isStatic = isStatic
            voxelShape.isVoxelTerrainFullyLoaded = shipVoxelsFullyLoaded

            shipIdToPhysShip[shipId] =
                PhysShipImpl(
                    shipId,
                    newRigidBodyReference,
                    newShipInGameFrameData.forcesInducers,
                    inertiaData,
                    poseVel,
                    dimension
                )
        }

        // Update existing ships
        gameFrame.updatedBodies.forEach { (shipId, shipUpdate) ->
            val physShip = shipIdToPhysShip[shipId]
                ?: throw IllegalStateException(
                    "Tried updating rigid body from ship with UUID $shipId, but no rigid body exists for this ship!"
                )

            val shipRigidBody = physShip.rigidBodyReference
            val oldPoseVel = shipRigidBody.poseVel

            val oldScaling = shipRigidBody.collisionShapeScaling
            val newScaling = shipUpdate.newScaling
            val oldVoxelOffset = shipRigidBody.collisionShapeOffset
            val newVoxelOffset = shipUpdate.newVoxelOffset
            val deltaVoxelOffset = oldPoseVel.rot.transform(Vector3d(newVoxelOffset).mul(newScaling).sub(Vector3d(oldVoxelOffset).mul(oldScaling)))
            val isStatic = shipUpdate.isStatic
            val shipVoxelsFullyLoaded = shipUpdate.shipVoxelsFullyLoaded

            val newShipPoseVel = PoseVel(
                oldPoseVel.pos.sub(deltaVoxelOffset, Vector3d()), oldPoseVel.rot, oldPoseVel.vel, oldPoseVel.omega
            )

            physShip._inertia = shipUpdate.inertiaData
            physShip.forceInducers = shipUpdate.forcesInducers

            shipRigidBody.collisionShapeOffset = newVoxelOffset
            shipRigidBody.collisionShapeScaling = newScaling
            shipRigidBody.poseVel = newShipPoseVel
            physShip.poseVel = newShipPoseVel
            shipRigidBody.inertiaData = physInertiaToRigidBodyInertiaData(shipUpdate.inertiaData)
            shipRigidBody.isStatic = isStatic
            shipRigidBody.collisionShape.isVoxelTerrainFullyLoaded = shipVoxelsFullyLoaded
        }

        // Send voxel updates
        gameFrame.voxelUpdatesMap.forEach { (shipId, voxelUpdatesList) ->
            val shipRigidBodyReferenceAndId = shipIdToPhysShip[shipId]
                ?: throw IllegalStateException(
                    "Tried sending voxel updates to rigid body from ship with UUID $shipId," +
                        " but no rigid body exists for this ship!"
                )
            val shipRigidBodyReference = shipRigidBodyReferenceAndId.rigidBodyReference
            if (!shipRigidBodyReference.isStatic) {
                // Always process updates to non-static ships immediately
                val voxelShape = shipRigidBodyReference.collisionShape
                voxelShape.queueUpdates(voxelUpdatesList)
            } else {
                // Queue updates to static ships for later
                pendingUpdates.add(Pair(shipId, voxelUpdatesList))
                pendingUpdatesSize += voxelUpdatesList.size
            }
        }

        // region Create/Update/Delete constraints
        gameFrame.constraintsCreatedThisTick.forEach { vsConstraintAndId: VSConstraintAndId ->
            val physicsBody0 = shipIdToPhysShip[vsConstraintAndId.vsConstraint.bodyId0]!!
            val physicsBody1 = shipIdToPhysShip[vsConstraintAndId.vsConstraint.bodyId1]!!
            if (physicsBody0.dimension != physicsBody1.dimension) {
                throw IllegalStateException("Cannot create constraints across dimensions!")
            }
            val dimension = physicsBody0.dimension
            val physicsEngine = physicsEngines[dimension]!!
            physicsEngine.addConstraint(vsConstraintAndId.convertToPhysics())
            constraintIdToDimension[vsConstraintAndId.constraintId] = dimension
        }
        gameFrame.constraintsUpdatedThisTick.forEach { vsConstraintAndId: VSConstraintAndId ->
            val physicsBody0 = shipIdToPhysShip[vsConstraintAndId.vsConstraint.bodyId0]!!
            val physicsBody1 = shipIdToPhysShip[vsConstraintAndId.vsConstraint.bodyId1]!!
            val dimension = constraintIdToDimension[vsConstraintAndId.constraintId]!!
            if (physicsBody0.dimension != dimension || physicsBody1.dimension != dimension) {
                throw IllegalStateException("Cannot update constraints to be in different dimensions!")
            }
            val physicsEngine = physicsEngines[dimension]!!
            physicsEngine.updateConstraint(vsConstraintAndId.convertToPhysics())
        }
        gameFrame.constraintsDeletedThisTick.forEach { vsConstraintId: VSConstraintId ->
            val dimension = constraintIdToDimension.remove(vsConstraintId)!!
            val physicsEngine = physicsEngines[dimension]!!
            physicsEngine.removeConstraint(vsConstraintId.convertToPhysics())
        }
        // endregion

        // region Send updates to static ships, staggered to limit number of updates per tick
        val updatesToSend =
            max(min(pendingUpdatesSize, MAX_UPDATES_PER_PHYS_TICK), pendingUpdatesSize - MAX_PENDING_UPDATES_SIZE)
        var updatesSent = 0
        for (i in 0 until pendingUpdates.size) {
            val curUpdate = pendingUpdates[i]
            val shipId = curUpdate.first
            val shipRigidBodyReferenceAndId = shipIdToPhysShip[shipId]
                ?: throw IllegalStateException(
                    "Tried sending voxel updates to rigid body from ship with UUID $shipId," +
                        " but no rigid body exists for this ship!"
                )
            val shipRigidBodyReference = shipRigidBodyReferenceAndId.rigidBodyReference

            var isAllOfISent = false

            if (curUpdate.second.size <= updatesToSend - updatesSent) {
                // Send all of it
                val voxelShape = shipRigidBodyReference.collisionShape as VoxelShapeReference
                voxelShape.queueUpdates(curUpdate.second)
                updatesSent += curUpdate.second.size
                isAllOfISent = true
            } else {
                // Send part of it
                val toSend = curUpdate.second.subList(0, updatesToSend - updatesSent)
                val toKeepForNextPhysTick = curUpdate.second.subList(updatesToSend - updatesSent, curUpdate.second.size)
                val voxelShape = shipRigidBodyReference.collisionShape as VoxelShapeReference
                voxelShape.queueUpdates(toSend)
                updatesSent += toSend.size
                pendingUpdates[i] = Pair(shipId, toKeepForNextPhysTick)
            }
            if (updatesSent == updatesToSend) {
                pendingUpdates = if (isAllOfISent)
                    ArrayList(pendingUpdates.subList(i + 1, pendingUpdates.size))
                else
                    ArrayList(pendingUpdates.subList(i, pendingUpdates.size))
                break
            }
        }
        pendingUpdatesSize -= updatesSent
        // endregion
    }

    private fun createPhysicsFrame(): VSPhysicsFrame {
        val shipDataMap: MutableMap<ShipId, ShipInPhysicsFrameData> = HashMap()
        // For now the physics doesn't send voxel updates, but it will in the future
        val voxelUpdatesMap: Map<ShipId, List<IVoxelShapeUpdate>> = emptyMap()
        shipIdToPhysShip.forEach { (shipId, shipIdAndRigidBodyReference) ->
            val rigidBodyReference = shipIdAndRigidBodyReference.rigidBodyReference
            val inertiaData: PhysicsBodyInertiaData = rigidBodyReference.inertiaData
            val poseVel: PoseVel = rigidBodyReference.poseVel
            val shipScaling: Double = rigidBodyReference.collisionShapeScaling
            val shipVoxelOffset: Vector3dc = rigidBodyReference.collisionShapeOffset
            val aabb = AABBd()
            rigidBodyReference.getAABB(aabb)

            shipDataMap[shipId] =
                ShipInPhysicsFrameData(
                    shipId, inertiaData, poseVel, shipScaling, shipVoxelOffset, aabb
                )
        }
        return VSPhysicsFrame(shipDataMap, voxelUpdatesMap, physTick++)
    }

    private fun PhysicsConfig.makeKrunchSettings(): KrunchPhysicsWorldSettings {
        val settings = KrunchPhysicsWorldSettings()
        // Only use 10 sub-steps
        settings.subSteps = 10
        settings.solverType = GAUSS_SEIDEL
        settings.iterations = 1

        // Decrease max de-penetration speed so that rigid bodies don't go
        // flying apart when they overlap
        settings.maxDePenetrationSpeed = 10.0

        settings.maxVoxelShapeCollisionPoints = lodDetail
        return settings
    }
    
    private fun VSConstraint.convertToPhysics(): ConstraintData {
        val body0Id: PhysicsBodyId = shipIdToPhysShip[this.bodyId0]!!.rigidBodyReference.physicsBodyId
        val body1Id: PhysicsBodyId = shipIdToPhysShip[this.bodyId1]!!.rigidBodyReference.physicsBodyId
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
                    PARALLEL -> RotDampingAxes.PARALLEL
                    PERPENDICULAR -> RotDampingAxes.PERPENDICULAR
                    ALL_AXES -> RotDampingAxes.ALL_AXES
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
        ConstraintAndId(this.id.convertToPhysics(), this.vsConstraint.convertToPhysics())
    
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
