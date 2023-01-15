package org.valkyrienskies.core.impl.pipelines

import org.joml.Matrix3d
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint
import org.valkyrienskies.core.apigame.constraints.VSConstraint
import org.valkyrienskies.core.apigame.constraints.VSConstraintAndId
import org.valkyrienskies.core.apigame.constraints.VSConstraintId
import org.valkyrienskies.core.apigame.constraints.VSConstraintType.ATTACHMENT
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
import org.valkyrienskies.core.impl.config.PhysicsConfig
import org.valkyrienskies.core.impl.config.VSCoreConfig
import org.valkyrienskies.core.impl.game.ships.PhysInertia
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl
import org.valkyrienskies.core.impl.game.ships.WingPhysicsSolver
import org.valkyrienskies.core.impl.util.logger
import org.valkyrienskies.physics_api.ConstraintId
import org.valkyrienskies.physics_api.PhysicsWorldReference
import org.valkyrienskies.physics_api.PoseVel
import org.valkyrienskies.physics_api.RigidBodyId
import org.valkyrienskies.physics_api.RigidBodyInertiaData
import org.valkyrienskies.physics_api.SegmentId
import org.valkyrienskies.physics_api.SegmentTracker
import org.valkyrienskies.physics_api.constraints.AttachmentConstraint
import org.valkyrienskies.physics_api.constraints.Constraint
import org.valkyrienskies.physics_api.constraints.ConstraintAndId
import org.valkyrienskies.physics_api.constraints.FixedOrientationConstraint
import org.valkyrienskies.physics_api.constraints.HingeOrientationConstraint
import org.valkyrienskies.physics_api.constraints.HingeSwingLimitsConstraint
import org.valkyrienskies.physics_api.constraints.PosDampingConstraint
import org.valkyrienskies.physics_api.constraints.RopeConstraint
import org.valkyrienskies.physics_api.constraints.RotDampingAxes
import org.valkyrienskies.physics_api.constraints.RotDampingConstraint
import org.valkyrienskies.physics_api.constraints.SlideConstraint
import org.valkyrienskies.physics_api.constraints.SphericalSwingLimitsConstraint
import org.valkyrienskies.physics_api.constraints.SphericalTwistLimitsConstraint
import org.valkyrienskies.physics_api.dummy_impl.DummyPhysicsWorldReference
import org.valkyrienskies.physics_api.voxel_updates.IVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel_updates.VoxelRigidBodyShapeUpdates
import org.valkyrienskies.physics_api_krunch.KrunchBootstrap
import org.valkyrienskies.physics_api_krunch.KrunchPhysicsWorldSettings
import org.valkyrienskies.physics_api_krunch.SolverType.GAUSS_SEIDEL
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

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
            ship.forceInducers.forEach { it.applyForces(ship) }
            WingPhysicsSolver.applyWingForces(ship)
            ship.applyQueuedForces()
        }

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
        gameFrame.deletedShips.forEach { deletedShipId ->
            val shipRigidBodyReferenceAndId = shipIdToPhysShip[deletedShipId]
                ?: throw IllegalStateException(
                    "Tried deleting rigid body from ship with UUID $deletedShipId," +
                        " but no rigid body exists for this ship!"
                )

            val shipRigidBodyReference = shipRigidBodyReferenceAndId.rigidBodyReference
            physicsEngine.deleteRigidBody(shipRigidBodyReference.rigidBodyId)
            shipIdToPhysShip.remove(deletedShipId)
        }

        // Create new ships
        gameFrame.newShips.forEach { newShipInGameFrameData ->
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
            val wingManagerChanges = newShipInGameFrameData.wingManagerChanges

            val newRigidBodyReference =
                physicsEngine.createVoxelRigidBody(
                    dimension, minDefined, maxDefined, totalVoxelRegion
                )
            newRigidBodyReference.inertiaData = physInertiaToRigidBodyInertiaData(inertiaData)
            newRigidBodyReference.poseVel = poseVel
            newRigidBodyReference.collisionShapeOffset = newShipInGameFrameData.voxelOffset
            newRigidBodyReference.isStatic = isStatic
            newRigidBodyReference.isVoxelTerrainFullyLoaded = shipVoxelsFullyLoaded
            // TODO: This will need to be changed when we have multiple segments
            newRigidBodyReference.setSegmentDisplacement(0, segments.segments.values.first().segmentDisplacement)

            val physShip =
                PhysShipImpl(
                    shipId,
                    newRigidBodyReference,
                    newShipInGameFrameData.forcesInducers,
                    inertiaData,
                    poseVel,
                    segments
                )
            if (wingManagerChanges != null) {
                physShip.wingManager.applyChanges(wingManagerChanges)
            }
            shipIdToPhysShip[shipId] = physShip
        }

        // Update existing ships
        gameFrame.updatedShips.forEach { (shipId, shipUpdate) ->
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
            val wingManagerChanges = shipUpdate.wingManagerChanges

            val newShipPoseVel = PoseVel(
                oldPoseVel.pos.sub(deltaVoxelOffset, Vector3d()), oldPoseVel.rot, oldPoseVel.vel, oldPoseVel.omega
            )

            physShip._inertia = shipUpdate.inertiaData
            physShip.forceInducers = shipUpdate.forcesInducers
            physShip.poseVel = newShipPoseVel

            shipRigidBody.collisionShapeOffset = newVoxelOffset
            shipRigidBody.poseVel = newShipPoseVel
            shipRigidBody.inertiaData = physInertiaToRigidBodyInertiaData(shipUpdate.inertiaData)
            shipRigidBody.isStatic = isStatic
            shipRigidBody.isVoxelTerrainFullyLoaded = shipVoxelsFullyLoaded

            if (wingManagerChanges != null) {
                physShip.wingManager.applyChanges(wingManagerChanges)
            }
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
                    VoxelRigidBodyShapeUpdates(shipRigidBodyReference.rigidBodyId, voxelUpdatesList.toTypedArray())
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
                    VoxelRigidBodyShapeUpdates(shipRigidBodyReference.rigidBodyId, curUpdate.second.toTypedArray())
                physicsEngine.queueVoxelShapeUpdates(arrayOf(voxelRigidBodyShapeUpdates))
                updatesSent += curUpdate.second.size
                isAllOfISent = true
            } else {
                // Send part of it
                val toSend = curUpdate.second.subList(0, updatesToSend - updatesSent)
                val toKeepForNextPhysTick = curUpdate.second.subList(updatesToSend - updatesSent, curUpdate.second.size)
                val voxelRigidBodyShapeUpdates =
                    VoxelRigidBodyShapeUpdates(shipRigidBodyReference.rigidBodyId, toSend.toTypedArray())
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
            val inertiaData: RigidBodyInertiaData = rigidBodyReference.inertiaData
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
        settings.solverType = GAUSS_SEIDEL
        settings.iterations = 1

        // Decrease max de-penetration speed so that rigid bodies don't go
        // flying apart when they overlap
        settings.maxDePenetrationSpeed = 10.0

        settings.maxVoxelShapeCollisionPoints = lodDetail
        return settings
    }

    private fun convertVSConstraintToPhysicsConstraint(vsConstraint: VSConstraint): Constraint {
        val body0Id: RigidBodyId = shipIdToPhysShip[vsConstraint.shipId0]!!.rigidBodyReference.rigidBodyId
        val body1Id: RigidBodyId = shipIdToPhysShip[vsConstraint.shipId1]!!.rigidBodyReference.rigidBodyId
        val segment0Id: SegmentId = 0
        val segment1Id: SegmentId = 0
        return when (vsConstraint.constraintType) {
            ATTACHMENT -> {
                val attachmentConstraint = vsConstraint as VSAttachmentConstraint
                AttachmentConstraint(
                    body0Id, body1Id, segment0Id, segment1Id, attachmentConstraint.compliance,
                    attachmentConstraint.localPos0, attachmentConstraint.localPos1, attachmentConstraint.maxForce,
                    attachmentConstraint.fixedDistance
                )
            }
            FIXED_ORIENTATION -> {
                val fixedOrientationConstraint = vsConstraint as VSFixedOrientationConstraint
                FixedOrientationConstraint(
                    body0Id, body1Id, segment0Id, segment1Id, fixedOrientationConstraint.compliance,
                    fixedOrientationConstraint.localRot0, fixedOrientationConstraint.localRot1,
                    fixedOrientationConstraint.maxTorque
                )
            }
            HINGE_ORIENTATION -> {
                val hingeOrientationConstraint = vsConstraint as VSHingeOrientationConstraint
                HingeOrientationConstraint(
                    body0Id, body1Id, segment0Id, segment1Id, hingeOrientationConstraint.compliance,
                    hingeOrientationConstraint.localRot0, hingeOrientationConstraint.localRot1,
                    hingeOrientationConstraint.maxTorque
                )
            }
            HINGE_SWING_LIMITS -> {
                val hingeSwingLimitsConstraint = vsConstraint as VSHingeSwingLimitsConstraint
                HingeSwingLimitsConstraint(
                    body0Id, body1Id, segment0Id, segment1Id, hingeSwingLimitsConstraint.compliance,
                    hingeSwingLimitsConstraint.localRot0, hingeSwingLimitsConstraint.localRot1,
                    hingeSwingLimitsConstraint.maxTorque, hingeSwingLimitsConstraint.minSwingAngle,
                    hingeSwingLimitsConstraint.maxSwingAngle
                )
            }
            HINGE_TARGET_ANGLE -> {
                val hingeTargetAngleConstraint = vsConstraint as VSHingeTargetAngleConstraint
                HingeSwingLimitsConstraint(
                    body0Id, body1Id, segment0Id, segment1Id, hingeTargetAngleConstraint.compliance,
                    hingeTargetAngleConstraint.localRot0, hingeTargetAngleConstraint.localRot1,
                    hingeTargetAngleConstraint.maxTorque, hingeTargetAngleConstraint.targetAngle,
                    hingeTargetAngleConstraint.nextTickTargetAngle
                )
            }
            POS_DAMPING -> {
                val posDampingConstraint = vsConstraint as VSPosDampingConstraint
                PosDampingConstraint(
                    body0Id, body1Id, segment0Id, segment1Id, posDampingConstraint.compliance,
                    posDampingConstraint.localPos0, posDampingConstraint.localPos1, posDampingConstraint.maxForce,
                    posDampingConstraint.posDamping
                )
            }
            ROPE -> {
                val ropeConstraint = vsConstraint as VSRopeConstraint
                RopeConstraint(
                    body0Id, body1Id, segment0Id, segment1Id, ropeConstraint.compliance,
                    ropeConstraint.localPos0, ropeConstraint.localPos1, ropeConstraint.maxForce,
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
                RotDampingConstraint(
                    body0Id, body1Id, segment0Id, segment1Id, rotDampingConstraint.compliance,
                    rotDampingConstraint.localRot0, rotDampingConstraint.localRot1,
                    rotDampingConstraint.maxTorque, rotDampingConstraint.rotDamping,
                    rotDampingAxes
                )
            }
            SLIDE -> {
                val slideConstraint = vsConstraint as VSSlideConstraint
                SlideConstraint(
                    body0Id, body1Id, segment0Id, segment1Id, slideConstraint.compliance,
                    slideConstraint.localPos0, slideConstraint.localPos1, slideConstraint.maxForce,
                    slideConstraint.localSlideAxis0, slideConstraint.maxDistBetweenPoints
                )
            }
            SPHERICAL_SWING_LIMITS -> {
                val sphericalSwingLimitsConstraint = vsConstraint as VSSphericalSwingLimitsConstraint
                SphericalSwingLimitsConstraint(
                    body0Id, body1Id, segment0Id, segment1Id, sphericalSwingLimitsConstraint.compliance,
                    sphericalSwingLimitsConstraint.localRot0, sphericalSwingLimitsConstraint.localRot1,
                    sphericalSwingLimitsConstraint.maxTorque, sphericalSwingLimitsConstraint.minSwingAngle,
                    sphericalSwingLimitsConstraint.maxSwingAngle
                )
            }
            SPHERICAL_TWIST_LIMITS -> {
                val sphericalTwistLimitsConstraint = vsConstraint as VSSphericalTwistLimitsConstraint
                SphericalTwistLimitsConstraint(
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
        private fun physInertiaToRigidBodyInertiaData(inertia: PhysInertia): RigidBodyInertiaData {
            val invMass = 1.0 / inertia.shipMass
            if (!invMass.isFinite())
                throw IllegalStateException("invMass is not finite!")

            val invInertiaMatrix = inertia.momentOfInertiaTensor.invert(Matrix3d())
            if (!invInertiaMatrix.isFinite)
                throw IllegalStateException("invInertiaMatrix is not finite!")

            return RigidBodyInertiaData(invMass, invInertiaMatrix)
        }

        private const val MAX_UPDATES_PER_PHYS_TICK = 1000

        // Store up to 60 physics ticks worth of voxel updates before we force Krunch to process them more quickly
        private const val MAX_PENDING_UPDATES_SIZE = MAX_UPDATES_PER_PHYS_TICK * 60

        private val logger by logger()
    }
}
