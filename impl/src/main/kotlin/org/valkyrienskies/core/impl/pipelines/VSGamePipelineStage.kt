package org.valkyrienskies.core.impl.pipelines

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.joml.*
import org.valkyrienskies.core.api.physics.constraints.VSConstraint
import org.valkyrienskies.core.api.physics.constraints.VSConstraintAndId
import org.valkyrienskies.core.api.physics.constraints.VSConstraintId
import org.valkyrienskies.core.api.physics.constraints.VSForceConstraint
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipInertiaData
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.impl.api.ServerShipInternal
import org.valkyrienskies.core.impl.api.Ticked
import org.valkyrienskies.core.impl.game.ships.*
import org.valkyrienskies.core.impl.util.logger
import org.valkyrienskies.physics_api.PhysicsWorldReference
import org.valkyrienskies.physics_api.PoseVel
import org.valkyrienskies.physics_api.voxel.updates.IVoxelShapeUpdate
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

class VSGamePipelineStage @Inject constructor(private val shipWorld: ShipObjectServerWorld) {

    private val physicsFramesQueue: ConcurrentLinkedQueue<VSPhysicsFrame> = ConcurrentLinkedQueue()
    private val dimensionIntIdToString = Int2ObjectOpenHashMap<String>()

    /**
     * Push a physics frame to the game stage
     */
    fun pushPhysicsFrame(physicsFrame: VSPhysicsFrame) {
        if (physicsFramesQueue.size >= 300) {
            // throw IllegalStateException("Too many physics frames in the physics frame queue. Is the game stage broken?")
            logger.warn("Too many physics frames in the physics frame queue. Is the game stage broken?")
            Thread.sleep(1000L)
        }
        physicsFramesQueue.add(physicsFrame)
    }

    /**
     * Apply queued physics frames to the game
     */
    fun preTickGame() {
        // Tick every attachment that wants to get ticked
        shipWorld.shipObjects.forEach {
            it.value.toBeTicked.forEach(Ticked::tick)
        }

        shipWorld.preTick()
    }

    /**
     * Create a new game frame to be sent to the physics
     */
    fun postTickGame(): VSGameFrame {
        // Set the values of prevTickShipTransform
        shipWorld.shipObjects.forEach { (_, shipObject) ->
            shipObject.shipData.updatePrevTickShipTransform()
        }

        // Apply the physics frames
        while (physicsFramesQueue.isNotEmpty()) {
            val physicsFrame = physicsFramesQueue.remove()
            applyPhysicsFrame(physicsFrame)
        }

        shipWorld.postTick()
        // Finally, return the game frame
        return createGameFrame()
    }

    private fun applyPhysicsFrame(physicsFrame: VSPhysicsFrame) {
        physicsFrame.shipDataMap.forEach { (shipId, shipInPhysicsFrameData) ->
            // Only apply physics updates to ShipObjects. Do not apply them to ShipData without a ShipObject
            val shipObject: ShipObjectServer? = shipWorld.shipObjects[shipId]
            val shipData: ShipData? = shipObject?.shipData
            if (shipData != null) {
                // TODO: Don't apply the transform if we are forcing the ship to move somewhere else
                val applyTransform = true // For now just set [applyTransform] to always be true
                if (applyTransform) {
                    val newShipTransform = generateTransformFromPhysicsFrameData(shipInPhysicsFrameData, shipData)

                    shipData.physicsData.linearVelocity = shipInPhysicsFrameData.poseVel.vel
                    shipData.physicsData.angularVelocity = shipInPhysicsFrameData.poseVel.omega
                    shipData.transform = newShipTransform
                }
            } else {
                // Check ground rigid body objects
                if (!shipWorld.dimensionToGroundBodyIdImmutable.containsValue(shipId))
                    logger.warn(
                        "Received physics frame update for ship with ShipId: $shipId, " +
                            "but a ship with this ShipId does not exist!"
                    )
            }
        }
    }

    private fun createGameFrame(): VSGameFrame {
        val newShips = ArrayList<NewVoxelRigidBodyFrameData>() // Ships to be added to the Physics simulation
        val deletedShips = ArrayList<ShipId>() // Ships to be deleted from the Physics simulation
        val updatedShips = HashMap<ShipId, UpdateRigidBodyFrameData>() // Map of ship updates
        val gameFrameVoxelUpdatesMap = HashMap<ShipId, List<IVoxelShapeUpdate>>() // Voxel updates applied by this frame

        val lastTickChanges = shipWorld.getCurrentTickChanges()

        val newGroundRigidBodyObjects = lastTickChanges.getNewGroundRigidBodyObjects()
        val newShipObjects = lastTickChanges.newShipObjects
        val updatedShipObjects = lastTickChanges.updatedShipObjects
        val deletedShipObjects = lastTickChanges.getDeletedShipObjectsIncludingGround()
        val shipVoxelUpdates = lastTickChanges.shipToVoxelUpdates

        newGroundRigidBodyObjects.forEach { newGroundObjectData ->
            val dimensionId = newGroundObjectData.first
            val shipId = newGroundObjectData.second
            val yRange = shipWorld.getYRange(dimensionId)
            val minDefined = Vector3i(Int.MIN_VALUE, yRange.first, Int.MIN_VALUE)
            val maxDefined = Vector3i(Int.MAX_VALUE, yRange.last, Int.MAX_VALUE)
            val totalVoxelRegion = PhysicsWorldReference.INFINITE_VOXEL_REGION
            // Some random inertia values, the ground body is static so these don't matter
            val inertiaData = PhysInertia(
                10.0,
                Matrix3d(
                    10.0, 0.0, 0.0,
                    0.0, 10.0, 0.0,
                    0.0, 0.0, 10.0
                )
            )
            val krunchDimensionId = getKrunchDimensionId(dimensionId)
            // Set the transform to be the origin with no rotation
            val poseVel = PoseVel.createPoseVel(Vector3d(), Quaterniond())
            val shipScaling = 1.0
            // No voxel offset
            val voxelOffset = Vector3d(.5, .5, .5)
            val isStatic = true
            val isVoxelsFullyLoaded = false
            val newVoxelRigidBodyFrameData = NewVoxelRigidBodyFrameData(
                shipId,
                krunchDimensionId,
                minDefined,
                maxDefined,
                totalVoxelRegion,
                inertiaData,
                ShipPhysicsData(Vector3d(), Vector3d()),
                poseVel,
                shipScaling,
                voxelOffset,
                isStatic,
                isVoxelsFullyLoaded,
                emptyList()
            )
            newShips.add(newVoxelRigidBodyFrameData)
        }

        newShipObjects.forEach {
            val uuid = it.shipData.id
            val minDefined = Vector3i()
            val maxDefined = Vector3i()
            val yRange = shipWorld.getYRange(it.chunkClaimDimension)

            it.shipData.activeChunksSet.getMinMaxWorldPos(minDefined, maxDefined, yRange)
            val totalVoxelRegion = it.chunkClaim.getTotalVoxelRegion(yRange)

            val krunchDimensionId = getKrunchDimensionId(it.shipData.chunkClaimDimension)
            val scaling = it.shipData.transform.scaling.x()

            val poseVel = PoseVel.createPoseVel(
                it.shipData.transform.position, it.shipData.transform.rotation
            )
            val voxelOffset = getShipVoxelOffset(it.shipData.inertiaData)
            val isStatic = it.shipData.isStatic
            val isVoxelsFullyLoaded = it.shipData.areVoxelsFullyLoaded()
            // Deep copy objects from ShipData, since we don't want VSGameFrame to be modified
            val newVoxelRigidBodyFrameData = NewVoxelRigidBodyFrameData(
                uuid,
                krunchDimensionId,
                minDefined,
                maxDefined,
                totalVoxelRegion,
                it.shipData.inertiaData.copyToPhyInertia(),
                it.shipData.physicsData.copy(),
                poseVel,
                scaling,
                voxelOffset,
                isStatic,
                isVoxelsFullyLoaded,
                it.forceInducers.toMutableList() //Copy the list
            )
            newShips.add(newVoxelRigidBodyFrameData)
        }

        updatedShipObjects.forEach {
            val uuid = it.shipData.id
            val newVoxelOffset = getShipVoxelOffset(it.shipData.inertiaData)
            val newScaling = it.shipData.transform.scaling.x()
            val isStatic = it.shipData.isStatic
            val isVoxelsFullyLoaded = it.shipData.areVoxelsFullyLoaded()
            // Deep copy objects from ShipData, since we don't want VSGameFrame to be modified
            val updateRigidBodyFrameData = UpdateRigidBodyFrameData(
                uuid,
                newVoxelOffset,
                newScaling,
                it.shipData.inertiaData.copyToPhyInertia(),
                it.shipData.physicsData.copy(),
                isStatic,
                isVoxelsFullyLoaded,
                it.forceInducers.toMutableList() //Copy the list
            )
            updatedShips[uuid] = updateRigidBodyFrameData
        }

        deletedShips.addAll(deletedShipObjects)

        shipVoxelUpdates.forEach forEachVoxelUpdate@{ (shipId, voxelUpdatesMap) ->
            gameFrameVoxelUpdatesMap[shipId] = voxelUpdatesMap.values.toList()
        }

        // region Convert the coordinates of the constraints to be relative to center of mass
        val constraintsCreatedThisTick: MutableList<VSConstraintAndId> = ArrayList()
        lastTickChanges.constraintsCreatedThisTick.forEach {
            val vsConstraint = it.vsConstraint
            if (vsConstraint !is VSForceConstraint) {
                constraintsCreatedThisTick.add(it)
            } else {
                val adjusted = adjustConstraintLocalPositions(vsConstraint)
                if (adjusted == null) {
                    logger.warn("Failed to adjust a constraint. Was a ship deleted?")
                    return@forEach
                }
                constraintsCreatedThisTick.add(VSConstraintAndId(it.constraintId, adjusted))
            }
        }

        val constraintsUpdatedThisTick: MutableList<VSConstraintAndId> = ArrayList()
        lastTickChanges.constraintsUpdatedThisTick.forEach {
            val vsConstraint = it.vsConstraint
            if (vsConstraint !is VSForceConstraint) {
                constraintsUpdatedThisTick.add(it)
            } else {
                val adjusted = adjustConstraintLocalPositions(vsConstraint)
                if (adjusted == null) {
                    logger.warn("Failed to adjust a constraint. Was a ship deleted?")
                    return@forEach
                }
                constraintsUpdatedThisTick.add(VSConstraintAndId(it.constraintId, adjusted))
            }
        }

        val constraintsDeletedThisTick: List<VSConstraintId> = ArrayList(lastTickChanges.constraintsDeletedThisTick)
        // endregion

        shipWorld.clearNewUpdatedDeletedShipObjectsAndVoxelUpdates() // can we move this into [ShipObjectServerWorld]?

        return VSGameFrame(
            newShips, deletedShips, updatedShips, gameFrameVoxelUpdatesMap, constraintsCreatedThisTick,
            constraintsUpdatedThisTick, constraintsDeletedThisTick
        )
    }

    /**
     * Converts the local positions of [vsConstraint] from shipyard coordinates to be relative to the center of mass of
     * the ship.
     *
     * This is used before we send the constraint to Krunch, since Krunch expects constraint positions to be relative to
     * the center of mass.
     */
    private fun adjustConstraintLocalPositions(vsConstraint: VSForceConstraint): VSConstraint? {
        val ship0 = shipWorld.loadedShips.getById(vsConstraint.bodyId0)
        val ship1 = shipWorld.loadedShips.getById(vsConstraint.bodyId1)

        val cm0: Vector3dc = if (!shipWorld.dimensionToGroundBodyIdImmutable.containsValue(vsConstraint.bodyId0)) {
            if (ship0 == null) return null
            ship0.shipData.inertiaData.centerOfMassInShip
        } else {
            // Account for the center of mass of ground bodies being (-0.5, -0.5, -0.5)
            Vector3d(-0.5, -0.5, -0.5)
        }

        val cm1: Vector3dc = if (!shipWorld.dimensionToGroundBodyIdImmutable.containsValue(vsConstraint.bodyId1)) {
            if (ship1 == null) return null
            ship1.shipData.inertiaData.centerOfMassInShip
        } else {
            // Account for the center of mass of ground bodies being (-0.5, -0.5, -0.5)
            Vector3d(-0.5, -0.5, -0.5)
        }

        // TODO: Make a helper for this
        val ship0Scaling = ship0?.shipData?.transform?.scaling?.x() ?: 1.0
        val ship1Scaling = ship1?.shipData?.transform?.scaling?.x() ?: 1.0

        // Offset force constraints by the center of mass before sending them to the physics pipeline
        // TODO: I'm not entirely sure why I have to subtract 0.5 here, but it works
        return vsConstraint.withLocalPositions(
            cm0.mul(-1.0, Vector3d()).sub(0.5, 0.5, 0.5).add(vsConstraint.localPos0).mul(ship0Scaling),
            cm1.mul(-1.0, Vector3d()).sub(0.5, 0.5, 0.5).add(vsConstraint.localPos1).mul(ship1Scaling),
        )
    }

    companion object {
        const val GAME_TPS = 20

        private fun getShipVoxelOffset(inertiaData: ShipInertiaData): Vector3dc {
            val cm = inertiaData.centerOfMassInShip
            return Vector3d(-cm.x(), -cm.y(), -cm.z())
        }

        fun generateTransformFromPhysicsFrameData(
            physicsFrameData: ShipInPhysicsFrameData, shipData: ServerShipInternal
        ): ShipTransform {
            val poseVelFromPhysics = physicsFrameData.poseVel
            val voxelOffsetFromPhysics = physicsFrameData.shipVoxelOffset
            val voxelOffsetFromGame = getShipVoxelOffset(shipData.inertiaData)

            val oldScaling = shipData.transform.scaling.x()
            val newScaling = physicsFrameData.shipScaling
            val deltaVoxelOffset = poseVelFromPhysics.rot.transform(Vector3d(voxelOffsetFromGame).mul(newScaling).sub(Vector3d(voxelOffsetFromPhysics).mul(oldScaling)))

            val shipPosAccountingForVoxelOffsetDifference =
                poseVelFromPhysics.pos.sub(deltaVoxelOffset, Vector3d())

            return ShipTransformImpl.create(
                shipPosAccountingForVoxelOffsetDifference,
                shipData.inertiaData.centerOfMassInShip.add(.5, .5, .5, Vector3d()),
                poseVelFromPhysics.rot,
                Vector3d(physicsFrameData.shipScaling)
            )
        }

        private val logger by logger()
    }

    private fun getKrunchDimensionId(dimensionId: DimensionId): Int {
        // TODO maybe don't use hashcode
        val id = dimensionId.hashCode()
        dimensionIntIdToString.put(id, dimensionId)
        return id
    }
}
