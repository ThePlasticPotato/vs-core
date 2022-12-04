package org.valkyrienskies.core.impl.pipelines

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.joml.*
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipInertiaData
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.impl.api.Ticked
import org.valkyrienskies.core.impl.game.ships.*
import org.valkyrienskies.core.impl.util.logger
import org.valkyrienskies.physics_api.PhysicsWorldReference
import org.valkyrienskies.physics_api.PoseVel
import org.valkyrienskies.physics_api.voxel_updates.IVoxelShapeUpdate
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
        val newShips = ArrayList<NewShipInGameFrameData>() // Ships to be added to the Physics simulation
        val deletedShips = ArrayList<ShipId>() // Ships to be deleted from the Physics simulation
        val updatedShips = HashMap<ShipId, UpdateShipInGameFrameData>() // Map of ship updates
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
            val minDefined = Vector3i(Int.MIN_VALUE, 0, Int.MIN_VALUE)
            val maxDefined = Vector3i(Int.MAX_VALUE, 255, Int.MAX_VALUE)
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
            val segments = SegmentUtils.createSegmentTrackerFromScaling(krunchDimensionId, 1.0)
            // No voxel offset
            val voxelOffset = Vector3d(.5, .5, .5)
            val isStatic = true
            val isVoxelsFullyLoaded = false
            val newShipInGameFrameData = NewShipInGameFrameData(
                shipId,
                krunchDimensionId,
                minDefined,
                maxDefined,
                totalVoxelRegion,
                inertiaData,
                ShipPhysicsData(Vector3d(), Vector3d()),
                poseVel,
                segments,
                voxelOffset,
                isStatic,
                isVoxelsFullyLoaded,
                emptyList()
            )
            newShips.add(newShipInGameFrameData)
        }

        newShipObjects.forEach {
            val uuid = it.shipData.id
            val minDefined = Vector3i()
            val maxDefined = Vector3i()
            it.shipData.activeChunksSet.getMinMaxWorldPos(minDefined, maxDefined)

            val totalVoxelRegion = it.chunkClaim.getTotalVoxelRegion(shipWorld.getYRange(it.chunkClaimDimension))

            val krunchDimensionId = getKrunchDimensionId(it.shipData.chunkClaimDimension)
            val scaling = it.shipData.transform.shipToWorldScaling.x()

            // TODO: Support more advanced segments than just basic scaling from origin
            val poseVel = PoseVel.createPoseVel(
                it.shipData.transform.positionInWorld.div(scaling, Vector3d()),
                it.shipData.transform.shipToWorldRotation
            )
            val segments = SegmentUtils.createSegmentTrackerFromScaling(krunchDimensionId, scaling)
            val voxelOffset = getShipVoxelOffset(it.shipData.inertiaData)
            val isStatic = it.shipData.isStatic
            val isVoxelsFullyLoaded = it.shipData.areVoxelsFullyLoaded()
            // Deep copy objects from ShipData, since we don't want VSGameFrame to be modified
            val newShipInGameFrameData = NewShipInGameFrameData(
                uuid,
                krunchDimensionId,
                minDefined,
                maxDefined,
                totalVoxelRegion,
                it.shipData.inertiaData.copyToPhyInertia(),
                it.shipData.physicsData.copy(),
                poseVel,
                segments,
                voxelOffset,
                isStatic,
                isVoxelsFullyLoaded,
                it.forceInducers.toMutableList() //Copy the list
            )
            newShips.add(newShipInGameFrameData)
        }

        updatedShipObjects.forEach {
            val uuid = it.shipData.id
            val newVoxelOffset = getShipVoxelOffset(it.shipData.inertiaData)
            val isStatic = it.shipData.isStatic
            val isVoxelsFullyLoaded = it.shipData.areVoxelsFullyLoaded()
            // Deep copy objects from ShipData, since we don't want VSGameFrame to be modified
            val updateShipInGameFrameData = UpdateShipInGameFrameData(
                uuid,
                newVoxelOffset,
                it.shipData.inertiaData.copyToPhyInertia(),
                it.shipData.physicsData.copy(),
                isStatic,
                isVoxelsFullyLoaded,
                it.forceInducers.toMutableList() //Copy the list
            )
            updatedShips[uuid] = updateShipInGameFrameData
        }

        deletedShips.addAll(deletedShipObjects)

        shipVoxelUpdates.forEach forEachVoxelUpdate@{ (shipId, voxelUpdatesMap) ->
            gameFrameVoxelUpdatesMap[shipId] = voxelUpdatesMap.values.toList()
        }

        shipWorld.clearNewUpdatedDeletedShipObjectsAndVoxelUpdates() // can we move this into [ShipObjectServerWorld]?
        return VSGameFrame(newShips, deletedShips, updatedShips, gameFrameVoxelUpdatesMap)
    }

    companion object {
        const val GAME_TPS = 20

        private fun getShipVoxelOffset(inertiaData: ShipInertiaData): Vector3dc {
            val cm = inertiaData.centerOfMassInShip
            return Vector3d(-cm.x(), -cm.y(), -cm.z())
        }

        fun generateTransformFromPhysicsFrameData(
            physicsFrameData: ShipInPhysicsFrameData, shipData: org.valkyrienskies.core.impl.api.ServerShipInternal
        ): ShipTransform {
            val poseVelFromPhysics = physicsFrameData.poseVel
            val voxelOffsetFromPhysics = physicsFrameData.shipVoxelOffset
            val voxelOffsetFromGame = getShipVoxelOffset(shipData.inertiaData)

            val deltaVoxelOffset = poseVelFromPhysics.rot.transform(
                voxelOffsetFromGame.sub(voxelOffsetFromPhysics, Vector3d())
            )

            val shipPosAccountingForVoxelOffsetDifference =
                poseVelFromPhysics.pos.sub(deltaVoxelOffset, Vector3d())

            val scaling = physicsFrameData.segments.segments.values.first().segmentDisplacement.scaling
            val shipPosAccountingForSegment = shipPosAccountingForVoxelOffsetDifference.mul(scaling, Vector3d())

            return ShipTransformImpl.create(
                shipPosAccountingForSegment,
                shipData.inertiaData.centerOfMassInShip.add(.5, .5, .5, Vector3d()),
                poseVelFromPhysics.rot,
                Vector3d(scaling)
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
