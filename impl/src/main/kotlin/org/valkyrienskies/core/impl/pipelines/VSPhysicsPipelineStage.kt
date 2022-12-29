package org.valkyrienskies.core.impl.pipelines

import org.joml.Matrix3d
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.physics.constraints.HingeTargetAngleConstraint
import org.valkyrienskies.core.api.physics.constraints.MaxDistanceConstraint
import org.valkyrienskies.core.api.physics.constraints.VSConstraint
import org.valkyrienskies.core.api.physics.constraints.VSConstraintAndId
import org.valkyrienskies.core.api.physics.constraints.VSConstraintId
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.ATTACHMENT
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.FIXED_ORIENTATION
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.HINGE_ORIENTATION
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.HINGE_SWING_LIMITS
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.HINGE_TARGET_ANGLE
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.POS_DAMPING
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.ROPE
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.ROT_DAMPING
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.SLIDE
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.SPHERICAL_SWING_LIMITS
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.SPHERICAL_TWIST_LIMITS
import org.valkyrienskies.core.api.physics.constraints.VSRotDampingAxes.ALL_AXES
import org.valkyrienskies.core.api.physics.constraints.VSRotDampingAxes.PARALLEL
import org.valkyrienskies.core.api.physics.constraints.VSRotDampingAxes.PERPENDICULAR
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.impl.config.PhysicsConfig
import org.valkyrienskies.core.impl.config.VSCoreConfig
import org.valkyrienskies.core.impl.game.ships.PhysInertia
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl
import org.valkyrienskies.core.impl.util.logger
import org.valkyrienskies.physics_api.ConstraintId
import org.valkyrienskies.physics_api.PhysicsBodyId
import org.valkyrienskies.physics_api.PhysicsBodyInertiaData
import org.valkyrienskies.physics_api.PhysicsWorldReference
import org.valkyrienskies.physics_api.PoseVel
import org.valkyrienskies.physics_api.SegmentId
import org.valkyrienskies.physics_api.SegmentTracker
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
import org.valkyrienskies.physics_api.dummy_impl.DummyPhysicsWorldReference
import org.valkyrienskies.physics_api.voxel.VoxelRigidBodyShapeUpdates
import org.valkyrienskies.physics_api.voxel.updates.IVoxelShapeUpdate
import org.valkyrienskies.physics_api_krunch.KrunchBootstrap
import org.valkyrienskies.physics_api_krunch.KrunchPhysicsWorldSettings
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@OptIn(VSBeta::class)
class VSPhysicsPipelineStage @Inject constructor() {
    private val gameFramesQueue: ConcurrentLinkedQueue<VSGameFrame> = ConcurrentLinkedQueue()
    private val physicsEngine: PhysicsWorldReference

    // Map ships ids to rigid bodies, and map rigid bodies to ship ids
    private val shipIdToPhysShip: MutableMap<ShipId, PhysShipImpl> = HashMap()
    private var physTick = 0

    private var pendingUpdates: MutableList<Pair<ShipId, List<IVoxelShapeUpdate>>> = ArrayList()
    private var pendingUpdatesSize: Int = 0

    var isUsingDummy = false
        private set

    init {
        // Try creating the physics engine
        physicsEngine = try {
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
            // TODO: In the future update the segment tracker too, probably do this after we've added portals to Krunch
            // it.segments = it.rigidBodyReference.segments
        }

        // Compute and apply forces/torques for ships
        shipIdToPhysShip.values.forEach { ship ->
            // TODO replace with better api
            ship.forceInducers.forEach { it.applyForces(ship) }
            ship.applyQueuedForces()
        }
        
        // TODO Tick physics tick hooks

        // Run the physics engine
        physicsEngine.tick(gravity, timeStep, simulatePhysics)

        // Return a new physics frame
        return createPhysicsFrame()
    }

    fun deleteResources() {
        if (physicsEngine.hasBeenDeleted()) throw IllegalStateException("Physics engine has already been deleted!")
        physicsEngine.deletePhysicsWorldResources()
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
            physicsEngine.deleteRigidBody(shipRigidBodyReference.physicsBodyId)
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
            val segments = newShipInGameFrameData.segments
            val isStatic = newShipInGameFrameData.isStatic
            val shipVoxelsFullyLoaded = newShipInGameFrameData.shipVoxelsFullyLoaded

            val voxelShape = physicsEngine.makeVoxelShape(minDefined, maxDefined, totalVoxelRegion)
            val newRigidBodyReference = physicsEngine.createRigidBody(dimension, voxelShape)

            newRigidBodyReference.inertiaData = physInertiaToRigidBodyInertiaData(inertiaData)
            newRigidBodyReference.poseVel = poseVel
            newRigidBodyReference.collisionShapeOffset = newShipInGameFrameData.voxelOffset
            newRigidBodyReference.isStatic = isStatic
            newRigidBodyReference.isVoxelTerrainFullyLoaded = shipVoxelsFullyLoaded
            // TODO: This will need to be changed when we have multiple segments
            newRigidBodyReference.setSegmentDisplacement(0, segments.segments.values.first().segmentDisplacement)

            shipIdToPhysShip[shipId] =
                PhysShipImpl(
                    shipId,
                    newRigidBodyReference,
                    newShipInGameFrameData.forcesInducers,
                    inertiaData,
                    poseVel,
                    segments
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

            val oldVoxelOffset = shipRigidBody.collisionShapeOffset
            val newVoxelOffset = shipUpdate.newVoxelOffset
            val deltaVoxelOffset = oldPoseVel.rot.transform(newVoxelOffset.sub(oldVoxelOffset, Vector3d()))
            val isStatic = shipUpdate.isStatic
            val shipVoxelsFullyLoaded = shipUpdate.shipVoxelsFullyLoaded

            val newShipPoseVel = PoseVel(
                oldPoseVel.pos.sub(deltaVoxelOffset, Vector3d()), oldPoseVel.rot, oldPoseVel.vel, oldPoseVel.omega
            )

            physShip._inertia = shipUpdate.inertiaData
            physShip.forceInducers = shipUpdate.forcesInducers

            shipRigidBody.collisionShapeOffset = newVoxelOffset
            shipRigidBody.poseVel = newShipPoseVel
            shipRigidBody.inertiaData = physInertiaToRigidBodyInertiaData(shipUpdate.inertiaData)
            shipRigidBody.isStatic = isStatic
            shipRigidBody.isVoxelTerrainFullyLoaded = shipVoxelsFullyLoaded
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
                val voxelRigidBodyShapeUpdates =
                    VoxelRigidBodyShapeUpdates(shipRigidBodyReference.physicsBodyId, voxelUpdatesList.toTypedArray())
                physicsEngine.queueVoxelShapeUpdates(arrayOf(voxelRigidBodyShapeUpdates))
            } else {
                // Queue updates to static ships for later
                pendingUpdates.add(Pair(shipId, voxelUpdatesList))
                pendingUpdatesSize += voxelUpdatesList.size
            }
        }

        // region Create/Update/Delete constraints
        gameFrame.constraintsCreatedThisTick.forEach { vsConstraintAndId: VSConstraintAndId ->
            physicsEngine.addConstraint(
                ConstraintAndId(
                    vsConstraintAndId.constraintId,
                    convertVSConstraintToPhysicsConstraint(vsConstraintAndId.vsConstraint)
                )
            )
        }
        gameFrame.constraintsUpdatedThisTick.forEach { vsConstraintAndId: VSConstraintAndId ->
            physicsEngine.updateConstraint(
                ConstraintAndId(
                    vsConstraintAndId.constraintId,
                    convertVSConstraintToPhysicsConstraint(vsConstraintAndId.vsConstraint)
                )
            )
        }
        gameFrame.constraintsDeletedThisTick.forEach { vsConstraintId: VSConstraintId ->
            physicsEngine.removeConstraint(convertVSConstraintIdToConstraintId(vsConstraintId))
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
                val voxelRigidBodyShapeUpdates =
                    VoxelRigidBodyShapeUpdates(shipRigidBodyReference.physicsBodyId, curUpdate.second.toTypedArray())
                physicsEngine.queueVoxelShapeUpdates(arrayOf(voxelRigidBodyShapeUpdates))
                updatesSent += curUpdate.second.size
                isAllOfISent = true
            } else {
                // Send part of it
                val toSend = curUpdate.second.subList(0, updatesToSend - updatesSent)
                val toKeepForNextPhysTick = curUpdate.second.subList(updatesToSend - updatesSent, curUpdate.second.size)
                val voxelRigidBodyShapeUpdates =
                    VoxelRigidBodyShapeUpdates(shipRigidBodyReference.physicsBodyId, toSend.toTypedArray())
                physicsEngine.queueVoxelShapeUpdates(arrayOf(voxelRigidBodyShapeUpdates))
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
            val segments: SegmentTracker = rigidBodyReference.segmentTracker
            val shipVoxelOffset: Vector3dc = rigidBodyReference.collisionShapeOffset
            val aabb = AABBd()
            rigidBodyReference.getAABB(aabb)

            shipDataMap[shipId] =
                ShipInPhysicsFrameData(
                    shipId, inertiaData, poseVel, segments, shipVoxelOffset, aabb
                )
        }
        return VSPhysicsFrame(shipDataMap, voxelUpdatesMap, physTick++)
    }

    private fun PhysicsConfig.makeKrunchSettings(): KrunchPhysicsWorldSettings {
        val settings = KrunchPhysicsWorldSettings()
        // Only use 10 sub-steps
        settings.subSteps = 10

        // Decrease max de-penetration speed so that rigid bodies don't go
        // flying apart when they overlap
        settings.maxDePenetrationSpeed = 10.0

        settings.maxVoxelShapeCollisionPoints = lodDetail
        return settings
    }

    private fun convertVSConstraintToPhysicsConstraint(vsConstraint: VSConstraint): ConstraintData {
        val body0Id: PhysicsBodyId = shipIdToPhysShip[vsConstraint.shipId0]!!.rigidBodyReference.physicsBodyId
        val body1Id: PhysicsBodyId = shipIdToPhysShip[vsConstraint.shipId1]!!.rigidBodyReference.physicsBodyId
        val segment0Id: SegmentId = 0
        val segment1Id: SegmentId = 0
        return when (vsConstraint.constraintType) {
            ATTACHMENT -> {
                val attachmentConstraint =
                    vsConstraint as org.valkyrienskies.core.api.physics.constraints.AttachmentConstraint
                AttachmentConstraintData(
                    body0Id, body1Id, segment0Id, segment1Id, attachmentConstraint.compliance,
                    attachmentConstraint.localPos0, attachmentConstraint.localPos1, attachmentConstraint.maxForce,
                    attachmentConstraint.fixedDistance
                )
            }

            FIXED_ORIENTATION -> {
                val fixedOrientationConstraint =
                    vsConstraint as org.valkyrienskies.core.api.physics.constraints.FixedOrientationConstraint
                FixedOrientationConstraintData(
                    body0Id, body1Id, segment0Id, segment1Id, fixedOrientationConstraint.compliance,
                    fixedOrientationConstraint.localRot0, fixedOrientationConstraint.localRot1,
                    fixedOrientationConstraint.maxTorque
                )
            }

            HINGE_ORIENTATION -> {
                val hingeOrientationConstraint =
                    vsConstraint as org.valkyrienskies.core.api.physics.constraints.HingeOrientationConstraint
                HingeOrientationConstraintData(
                    body0Id, body1Id, segment0Id, segment1Id, hingeOrientationConstraint.compliance,
                    hingeOrientationConstraint.localRot0, hingeOrientationConstraint.localRot1,
                    hingeOrientationConstraint.maxTorque
                )
            }

            HINGE_SWING_LIMITS -> {
                val hingeSwingLimitsConstraint =
                    vsConstraint as org.valkyrienskies.core.api.physics.constraints.HingeSwingLimitsConstraint
                HingeSwingLimitsConstraintData(
                    body0Id, body1Id, segment0Id, segment1Id, hingeSwingLimitsConstraint.compliance,
                    hingeSwingLimitsConstraint.localRot0, hingeSwingLimitsConstraint.localRot1,
                    hingeSwingLimitsConstraint.maxTorque, hingeSwingLimitsConstraint.minSwingAngle,
                    hingeSwingLimitsConstraint.maxSwingAngle
                )
            }

            HINGE_TARGET_ANGLE -> {
                val hingeTargetAngleConstraint = vsConstraint as HingeTargetAngleConstraint
                HingeSwingLimitsConstraintData(
                    body0Id, body1Id, segment0Id, segment1Id, hingeTargetAngleConstraint.compliance,
                    hingeTargetAngleConstraint.localRot0, hingeTargetAngleConstraint.localRot1,
                    hingeTargetAngleConstraint.maxTorque, hingeTargetAngleConstraint.targetAngle,
                    hingeTargetAngleConstraint.nextTickTargetAngle
                )
            }

            POS_DAMPING -> {
                val posDampingConstraint =
                    vsConstraint as org.valkyrienskies.core.api.physics.constraints.PosDampingConstraint
                PosDampingConstraintData(
                    body0Id, body1Id, segment0Id, segment1Id, posDampingConstraint.compliance,
                    posDampingConstraint.localPos0, posDampingConstraint.localPos1, posDampingConstraint.maxForce,
                    posDampingConstraint.posDamping
                )
            }

            ROPE -> {
                val ropeConstraint = vsConstraint as MaxDistanceConstraint
                RopeConstraintData(
                    body0Id, body1Id, segment0Id, segment1Id, ropeConstraint.compliance,
                    ropeConstraint.localPos0, ropeConstraint.localPos1, ropeConstraint.maxForce,
                    ropeConstraint.maxLength
                )
            }

            ROT_DAMPING -> {
                val rotDampingConstraint =
                    vsConstraint as org.valkyrienskies.core.api.physics.constraints.RotDampingConstraint
                val rotDampingAxes: RotDampingAxes = when (rotDampingConstraint.rotDampingAxes) {
                    PARALLEL -> RotDampingAxes.PARALLEL
                    PERPENDICULAR -> RotDampingAxes.PERPENDICULAR
                    ALL_AXES -> RotDampingAxes.ALL_AXES
                }
                RotDampingConstraintData(
                    body0Id, body1Id, segment0Id, segment1Id, rotDampingConstraint.compliance,
                    rotDampingConstraint.localRot0, rotDampingConstraint.localRot1,
                    rotDampingConstraint.maxTorque, rotDampingConstraint.rotDamping,
                    rotDampingAxes
                )
            }

            SLIDE -> {
                val slideConstraint = vsConstraint as org.valkyrienskies.core.api.physics.constraints.SlideConstraint
                SlideConstraintData(
                    body0Id, body1Id, segment0Id, segment1Id, slideConstraint.compliance,
                    slideConstraint.localPos0, slideConstraint.localPos1, slideConstraint.maxForce,
                    slideConstraint.localSlideAxis0, slideConstraint.maxDistBetweenPoints
                )
            }

            SPHERICAL_SWING_LIMITS -> {
                val sphericalSwingLimitsConstraint =
                    vsConstraint as org.valkyrienskies.core.api.physics.constraints.SphericalSwingLimitsConstraint
                SphericalSwingLimitsConstraintData(
                    body0Id, body1Id, segment0Id, segment1Id, sphericalSwingLimitsConstraint.compliance,
                    sphericalSwingLimitsConstraint.localRot0, sphericalSwingLimitsConstraint.localRot1,
                    sphericalSwingLimitsConstraint.maxTorque, sphericalSwingLimitsConstraint.minSwingAngle,
                    sphericalSwingLimitsConstraint.maxSwingAngle
                )
            }

            SPHERICAL_TWIST_LIMITS -> {
                val sphericalTwistLimitsConstraint =
                    vsConstraint as org.valkyrienskies.core.api.physics.constraints.SphericalTwistLimitsConstraint
                SphericalTwistLimitsConstraintData(
                    body0Id, body1Id, segment0Id, segment1Id, sphericalTwistLimitsConstraint.compliance,
                    sphericalTwistLimitsConstraint.localRot0, sphericalTwistLimitsConstraint.localRot1,
                    sphericalTwistLimitsConstraint.maxTorque, sphericalTwistLimitsConstraint.minTwistAngle,
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
