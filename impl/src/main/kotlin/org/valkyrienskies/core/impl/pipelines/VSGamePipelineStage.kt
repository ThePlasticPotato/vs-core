package org.valkyrienskies.core.impl.pipelines

import org.joml.Matrix3d
import org.joml.Quaterniond
import org.joml.Quaterniondc
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.Vector3i
import org.valkyrienskies.core.api.ships.WingManager
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipInertiaData
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.apigame.constraints.VSConstraint
import org.valkyrienskies.core.apigame.constraints.VSConstraintAndId
import org.valkyrienskies.core.apigame.constraints.VSConstraintId
import org.valkyrienskies.core.apigame.constraints.VSForceConstraint
import org.valkyrienskies.core.apigame.physics.PhysicsEntityServer
import org.valkyrienskies.core.apigame.physics.RigidBodyDefaults.DEFAULT_COLLISION_MASK
import org.valkyrienskies.core.apigame.physics.RigidBodyDefaults.DEFAULT_DYNAMIC_FRICTION_COEFFICIENT
import org.valkyrienskies.core.apigame.physics.RigidBodyDefaults.DEFAULT_RESTITUTION_COEFFICIENT
import org.valkyrienskies.core.apigame.physics.RigidBodyDefaults.DEFAULT_STATIC_FRICTION_COEFFICIENT
import org.valkyrienskies.core.apigame.physics.VSVoxelCollisionShapeData
import org.valkyrienskies.core.impl.api.ServerShipInternal
import org.valkyrienskies.core.impl.api.Ticked
import org.valkyrienskies.core.impl.game.ships.PhysInertia
import org.valkyrienskies.core.impl.game.ships.ShipData
import org.valkyrienskies.core.impl.game.ships.ShipInertiaDataImpl
import org.valkyrienskies.core.impl.game.ships.ShipObjectServer
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld
import org.valkyrienskies.core.impl.game.ships.ShipPhysicsData
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl
import org.valkyrienskies.core.impl.util.logger
import org.valkyrienskies.physics_api.PhysicsWorldReference
import org.valkyrienskies.physics_api.PoseVel
import org.valkyrienskies.physics_api.voxel.updates.IVoxelShapeUpdate
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

class VSGamePipelineStage @Inject constructor(private val shipWorld: ShipObjectServerWorld) {

    private val physicsFramesQueue: ConcurrentLinkedQueue<VSPhysicsFrame> = ConcurrentLinkedQueue()

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
        val gameFrame = createGameFrame()

        shipWorld.shipObjects.forEach {
            val shipAsWingManager: WingManager = it.value.getAttachment(WingManager::class.java)!!
            shipAsWingManager.clearWingChanges()
        }
        // Finally, return the game frame
        return gameFrame
    }

    private fun applyPhysicsFrame(physicsFrame: VSPhysicsFrame) {
        physicsFrame.shipDataMap.forEach { (shipId, shipInPhysicsFrameData) ->
            // Only apply physics updates to ShipObjects. Do not apply them to ShipData without a ShipObject
            val shipObject: ShipObjectServer? = shipWorld.shipObjects[shipId]
            val shipData: ShipData? = shipObject?.shipData
            if (shipData != null) {
                // TODO: Don't apply the transform if we are forcing the ship to move somewhere else
                val applyTransform = (shipObject.shipTeleportId == shipInPhysicsFrameData.lastShipTeleportId)
                if (applyTransform) {
                    val newShipTransform = generateTransformFromPhysicsFrameData(shipInPhysicsFrameData, shipData)

                    shipData.physicsData.linearVelocity = shipInPhysicsFrameData.poseVel.vel
                    shipData.physicsData.angularVelocity = shipInPhysicsFrameData.poseVel.omega
                    shipData.transform = newShipTransform
                }
            } else {
                // Check physics entities
                val physicsEntity: PhysicsEntityServer? = shipWorld.loadedPhysicsEntities[shipId]
                if (physicsEntity != null) {
                    val applyTransform = (physicsEntity.shipTeleportId == shipInPhysicsFrameData.lastShipTeleportId)
                    if (applyTransform) {
                        val newShipTransform = ShipTransformImpl.create(
                            shipInPhysicsFrameData.poseVel.pos,
                            Vector3d(),
                            shipInPhysicsFrameData.poseVel.rot,
                        )

                        physicsEntity.linearVelocity = shipInPhysicsFrameData.poseVel.vel
                        physicsEntity.angularVelocity = shipInPhysicsFrameData.poseVel.omega
                        physicsEntity.shipTransform = newShipTransform
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
    }

    private fun createGameFrame(): VSGameFrame {
        val newShips = ArrayList<NewShipInGameFrameData>() // Ships to be added to the Physics simulation
        val deletedShips = ArrayList<ShipId>() // Ships to be deleted from the Physics simulation
        val updatedShips = HashMap<ShipId, UpdateShipInGameFrameData>() // Map of ship updates
        val gameFrameVoxelUpdatesMap = HashMap<ShipId, List<IVoxelShapeUpdate>>() // Voxel updates applied by this frame

        val lastTickChanges = shipWorld.getCurrentTickChanges()

        val newGroundRigidBodyObjects = lastTickChanges.getNewGroundRigidBodyObjects()
        val newShipObjects = lastTickChanges.newShipObjects
        val newPhysicsEntities = lastTickChanges.newPhysicsEntities
        val updatedShipObjects = lastTickChanges.updatedShipObjects
        val updatedPhysicsEntities = lastTickChanges.updatedPhysicsEntities
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
            // Set the transform to be the origin with no rotation
            val poseVel = PoseVel.createPoseVel(Vector3d(), Quaterniond())
            // No voxel offset
            val voxelOffset = Vector3d(.5, .5, .5)
            val scaling = 1.0
            val isStatic = true
            val isVoxelsFullyLoaded = false

            // Create a voxel shape
            val collisionShapeData = VSVoxelCollisionShapeData(
                minDefined = minDefined,
                maxDefined = maxDefined,
                totalVoxelRegion = totalVoxelRegion,
                shipVoxelsFullyLoaded = isVoxelsFullyLoaded,
            )

            val collisionMask = DEFAULT_COLLISION_MASK
            val staticFrictionCoefficient = DEFAULT_STATIC_FRICTION_COEFFICIENT
            val dynamicFrictionCoefficient = DEFAULT_DYNAMIC_FRICTION_COEFFICIENT
            val restitutionCoefficient = DEFAULT_RESTITUTION_COEFFICIENT

            val newShipInGameFrameData = NewShipInGameFrameData(
                shipId,
                dimensionId,
                collisionShapeData,
                voxelOffset,
                scaling,
                inertiaData,
                ShipPhysicsData(Vector3d(), Vector3d()),
                poseVel,
                isStatic,
                emptyList(),
                null,
                0,
                collisionMask,
                staticFrictionCoefficient,
                dynamicFrictionCoefficient,
                restitutionCoefficient,
            )
            newShips.add(newShipInGameFrameData)
        }

        newShipObjects.forEach {
            val uuid = it.shipData.id
            val minDefined = Vector3i()
            val maxDefined = Vector3i()
            it.shipData.activeChunksSet.getMinMaxWorldPos(minDefined, maxDefined)

            val totalVoxelRegion = it.chunkClaim.getTotalVoxelRegion(shipWorld.getYRange(it.chunkClaimDimension))

            val scaling = it.shipData.transform.shipToWorldScaling.x()

            val poseVel = PoseVel.createPoseVel(
                it.shipData.transform.positionInWorld,
                it.shipData.transform.shipToWorldRotation
            )
            val voxelOffset = getShipVoxelOffset(it.shipData.inertiaData)
            val isStatic = it.shipData.isStatic
            val isVoxelsFullyLoaded = it.shipData.areVoxelsFullyLoaded()
            val shipAsWingManager: WingManager = it.getAttachment(WingManager::class.java)!!
            val shipTeleportId: Int = it.shipTeleportId

            // Create a voxel shape
            val collisionShapeData = VSVoxelCollisionShapeData(
                minDefined = minDefined,
                maxDefined = maxDefined,
                totalVoxelRegion = totalVoxelRegion,
                shipVoxelsFullyLoaded = isVoxelsFullyLoaded,
            )

            val collisionMask = DEFAULT_COLLISION_MASK
            val staticFrictionCoefficient = DEFAULT_STATIC_FRICTION_COEFFICIENT
            val dynamicFrictionCoefficient = DEFAULT_DYNAMIC_FRICTION_COEFFICIENT
            val restitutionCoefficient = DEFAULT_RESTITUTION_COEFFICIENT

            // Deep copy objects from ShipData, since we don't want VSGameFrame to be modified
            val newShipInGameFrameData = NewShipInGameFrameData(
                uuid,
                it.shipData.chunkClaimDimension,
                collisionShapeData,
                voxelOffset,
                scaling,
                it.shipData.inertiaData.copyToPhyInertia(),
                it.shipData.physicsData.copy(),
                poseVel,
                isStatic,
                it.forceInducers.toMutableList(), //Copy the list
                shipAsWingManager.getWingChanges(),
                shipTeleportId,
                collisionMask,
                staticFrictionCoefficient,
                dynamicFrictionCoefficient,
                restitutionCoefficient,
            )
            newShips.add(newShipInGameFrameData)
        }

        newPhysicsEntities.forEach {
            val uuid = it.id
            val dimension = it.dimensionId
            val poseVel = PoseVel.createPoseVel(
                it.shipTransform.positionInWorld,
                it.shipTransform.shipToWorldRotation
            )
            val voxelOffset = getShipVoxelOffset(it.inertiaData)
            val scaling = 1.0
            val isStatic = it.isStatic
            val shipTeleportId: Int = it.shipTeleportId

            // Create a voxel shape
            val collisionShapeData = it.collisionShapeData
            val physicsData = ShipPhysicsData(it.linearVelocity, it.angularVelocity)

            val collisionMask = it.collisionMask
            val staticFrictionCoefficient = it.staticFrictionCoefficient
            val dynamicFrictionCoefficient = it.dynamicFrictionCoefficient
            val restitutionCoefficient = it.restitutionCoefficient

            // Deep copy objects from ShipData, since we don't want VSGameFrame to be modified
            val newShipInGameFrameData = NewShipInGameFrameData(
                uuid,
                dimension,
                collisionShapeData,
                voxelOffset,
                scaling,
                (it.inertiaData as ShipInertiaDataImpl).copyToPhyInertia(),
                physicsData,
                poseVel,
                isStatic,
                listOf(),
                null,
                shipTeleportId,
                collisionMask,
                staticFrictionCoefficient,
                dynamicFrictionCoefficient,
                restitutionCoefficient,
            )
            newShips.add(newShipInGameFrameData)
        }

        updatedShipObjects.forEach {
            val uuid = it.shipData.id
            val newVoxelOffset = getShipVoxelOffset(it.shipData.inertiaData)
            val scaling = it.shipData.transform.shipToWorldScaling.x()
            val isStatic = it.shipData.isStatic
            val isVoxelsFullyLoaded = it.shipData.areVoxelsFullyLoaded()
            val shipAsWingManager: WingManager = it.getAttachment(WingManager::class.java)!!
            val shipTeleportId: Int = it.shipTeleportId
            val currentShipPos: Vector3dc = it.shipData.transform.positionInWorld
            val currentShipRot: Quaterniondc = it.shipData.transform.shipToWorldRotation
            val currentShipVel: Vector3dc = it.shipData.physicsData.linearVelocity
            val currentShipOmega: Vector3dc = it.shipData.physicsData.angularVelocity

            // Deep copy objects from ShipData, since we don't want VSGameFrame to be modified
            val updateShipInGameFrameData = UpdateShipInGameFrameData(
                uuid,
                newVoxelOffset,
                scaling,
                it.shipData.inertiaData.copyToPhyInertia(),
                it.shipData.physicsData.copy(),
                isStatic,
                isVoxelsFullyLoaded,
                it.forceInducers.toMutableList(), //Copy the list
                shipAsWingManager.getWingChanges(),
                shipTeleportId,
                currentShipPos,
                currentShipRot,
                currentShipVel,
                currentShipOmega,
            )
            updatedShips[uuid] = updateShipInGameFrameData
        }

        updatedPhysicsEntities.forEach {
            val uuid = it.id
            val newVoxelOffset = getShipVoxelOffset(it.inertiaData)
            val scaling = 1.0
            val isStatic = it.isStatic
            val isVoxelsFullyLoaded = true
            val shipTeleportId: Int = it.shipTeleportId
            val currentShipPos: Vector3dc = it.shipTransform.positionInWorld
            val currentShipRot: Quaterniondc = it.shipTransform.shipToWorldRotation
            val currentShipVel: Vector3dc = it.linearVelocity
            val currentShipOmega: Vector3dc = it.angularVelocity
            val physicsData = ShipPhysicsData(it.linearVelocity, it.angularVelocity)

            // Deep copy objects from ShipData, since we don't want VSGameFrame to be modified
            val updateShipInGameFrameData = UpdateShipInGameFrameData(
                uuid,
                newVoxelOffset,
                scaling,
                (it.inertiaData as ShipInertiaDataImpl).copyToPhyInertia(),
                physicsData,
                isStatic,
                isVoxelsFullyLoaded,
                listOf(),
                null,
                shipTeleportId,
                currentShipPos,
                currentShipRot,
                currentShipVel,
                currentShipOmega,
            )
            updatedShips[uuid] = updateShipInGameFrameData
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
        val ship0 = shipWorld.loadedShips.getById(vsConstraint.shipId0)
        val ship1 = shipWorld.loadedShips.getById(vsConstraint.shipId1)

        val cm0: Vector3dc = if (!shipWorld.dimensionToGroundBodyIdImmutable.containsValue(vsConstraint.shipId0)) {
            if (ship0 == null) return null
            ship0.shipData.inertiaData.centerOfMassInShip
        } else {
            // Account for the center of mass of ground bodies being (-0.5, -0.5, -0.5)
            Vector3d(-0.5, -0.5, -0.5)
        }

        val cm1: Vector3dc = if (!shipWorld.dimensionToGroundBodyIdImmutable.containsValue(vsConstraint.shipId1)) {
            if (ship1 == null) return null
            ship1.shipData.inertiaData.centerOfMassInShip
        } else {
            // Account for the center of mass of ground bodies being (-0.5, -0.5, -0.5)
            Vector3d(-0.5, -0.5, -0.5)
        }

        val scaling0 = ship0?.shipData?.transform?.shipToWorldScaling?.x() ?: 1.0
        val scaling1 = ship1?.shipData?.transform?.shipToWorldScaling?.x() ?: 1.0
        // Offset force constraints by the center of mass, then scale them, then send them to the physics pipeline
        // TODO: I'm not entirely sure why I have to subtract 0.5 here, but it works
        return vsConstraint.offsetLocalPositions(
            cm0.mul(-1.0, Vector3d()).sub(0.5, 0.5, 0.5),
            cm1.mul(-1.0, Vector3d()).sub(0.5, 0.5, 0.5),
        ).scaleLocalPositions(scaling0, scaling1)
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
            val scaling = physicsFrameData.scaling

            val deltaVoxelOffset = poseVelFromPhysics.rot.transform(
                voxelOffsetFromGame.sub(voxelOffsetFromPhysics, Vector3d())
            )
            deltaVoxelOffset.mul(scaling)

            val shipPosAccountingForVoxelOffsetDifference =
                poseVelFromPhysics.pos.sub(deltaVoxelOffset, Vector3d())

            return ShipTransformImpl.create(
                shipPosAccountingForVoxelOffsetDifference,
                shipData.inertiaData.centerOfMassInShip.add(.5, .5, .5, Vector3d()),
                poseVelFromPhysics.rot,
                Vector3d(scaling)
            )
        }

        private val logger by logger()
    }
}
