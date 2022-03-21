package org.valkyrienskies.core.pipelines

import org.joml.Vector3d
import org.valkyrienskies.core.game.ships.ShipData
import org.valkyrienskies.core.game.ships.ShipObjectServerWorld
import org.valkyrienskies.core.game.ships.ShipTransform
import org.valkyrienskies.physics_api.voxel_updates.IVoxelShapeUpdate
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class VSGamePipelineStage {
    private val shipWorlds: MutableMap<Int, ShipObjectServerWorld> = HashMap()
    private val physicsFramesQueue: ConcurrentLinkedQueue<VSPhysicsFrame> = ConcurrentLinkedQueue()

    /**
     * Push a physics frame to the game stage
     */
    fun pushPhysicsFrame(physicsFrame: VSPhysicsFrame) {
        if (physicsFramesQueue.size >= 100) {
            throw IllegalStateException("Too many physics frames in the physics frame queue. Is the game stage broken?")
        }
        physicsFramesQueue.add(physicsFrame)
    }

    /**
     * Apply queued physics frames to the game
     */
    fun preTickGame() {
        // Set the values of prevTickShipTransform
        shipWorlds.forEach { (_, shipWorld) ->
            shipWorld.shipObjects.forEach { (_, shipObject) ->
                shipObject.shipData.prevTickShipTransform = shipObject.shipData.shipTransform
            }
        }

        // Apply the physics frames
        while (physicsFramesQueue.isNotEmpty()) {
            val physicsFrame = physicsFramesQueue.remove()
            applyPhysicsFrame(physicsFrame)
        }
    }

    /**
     * Create a new game frame to be sent to the physics
     */
    fun postTickGame(): VSGameFrame {
        // Finally, return the game frame
        return createGameFrame()
    }

    private fun applyPhysicsFrame(physicsFrame: VSPhysicsFrame) {
        physicsFrame.shipDataMap.forEach { (uuid, shipInPhysicsFrameData) ->
            val dimension = shipInPhysicsFrameData.dimensionId
            val shipWorld: ShipObjectServerWorld? = shipWorlds[dimension]
            if (shipWorld != null) {
                // Only apply physics updates to ShipObjects. Do not apply them to ShipData without a ShipObject
                val shipData: ShipData? = shipWorld.shipObjects[uuid]?.shipData
                if (shipData != null) {
                    // TODO: Don't apply the transform if we are forcing the ship to move somewhere else
                    val applyTransform = true // For now just set [applyTransform] to always be true
                    if (applyTransform) {
                        val transformFromPhysics = shipInPhysicsFrameData.shipTransform
                        val voxelOffsetFromPhysics = shipInPhysicsFrameData.shipVoxelOffset

                        val deltaVoxelOffset = shipData.inertiaData.getCenterOfMassInShipSpace().sub(voxelOffsetFromPhysics, Vector3d())

                        val shipPosAccountingForVoxelOffsetDifference = transformFromPhysics.position.add(deltaVoxelOffset, Vector3d())
                        val newShipTransform = ShipTransform.createFromCoordinatesAndRotation(shipPosAccountingForVoxelOffsetDifference, shipData.inertiaData.getCenterOfMassInShipSpace(), transformFromPhysics.rotation)

                        shipData.shipTransform = newShipTransform
                    }
                } else {
                    if (shipWorld.groundBodyUUID != uuid)
                        print("Received physics frame update for ship with uuid: $uuid and dimension $dimension, but a ship with this uuid does not exist!")
                }
            } else {
                print("Received physics frame update for ship with uuid: $uuid and dimension $dimension, but a world with this dimension does not exist!")
            }
        }
    }

    private fun createGameFrame(): VSGameFrame {
        val newShips = ArrayList<NewShipInGameFrameData>() // Ships to be added to the Physics simulation
        val deletedShips = ArrayList<UUID>() // Ships to be deleted from the Physics simulation
        val updatedShips = HashMap<UUID, UpdateShipInGameFrameData>() // Map of ship updates
        val voxelUpdatesMap = HashMap<UUID, List<IVoxelShapeUpdate>>() // Voxel updates applied by this frame

        shipWorlds.forEach { (dimension, shipWorld) ->
            shipWorld.shipObjects.forEach { (uuid, shipObject) ->
                TODO("Implement this")
            }
        }
        return VSGameFrame(newShips, deletedShips, updatedShips, voxelUpdatesMap)
    }

    fun addShipWorld(shipWorld: ShipObjectServerWorld) {
        val dimension = shipWorld.dimension
        if (shipWorlds.containsKey(dimension)) throw IllegalStateException("Ship world with dimension $dimension already exists!")
        shipWorlds[dimension] = shipWorld
    }

    fun removeShipWorld(shipWorld: ShipObjectServerWorld) {
        shipWorlds.remove(shipWorld.dimension)
    }
}
