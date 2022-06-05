package org.valkyrienskies.core.game.ships

import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.Vector3i
import org.joml.Vector3ic
import org.valkyrienskies.core.chunk_tracking.ChunkUnwatchTask
import org.valkyrienskies.core.chunk_tracking.ChunkWatchTask
import org.valkyrienskies.core.game.ChunkAllocator
import org.valkyrienskies.core.game.IPlayer
import org.valkyrienskies.core.game.VSBlockType
import org.valkyrienskies.core.pipelines.VSPipeline
import org.valkyrienskies.core.util.names.NounListNameGenerator
import org.valkyrienskies.physics_api.voxel_updates.DenseVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel_updates.EmptyVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel_updates.IVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel_updates.KrunchVoxelStates
import org.valkyrienskies.physics_api.voxel_updates.SparseVoxelShapeUpdate
import java.util.Collections
import java.util.Spliterator
import java.util.TreeSet
import java.util.UUID

class ShipObjectServerWorld(
    override val queryableShipData: MutableQueryableShipDataServer,
    val chunkAllocator: ChunkAllocator,
    val dimension: Int
) : ShipObjectWorld(queryableShipData) {

    init {
        VSPipeline.getVSPipeline().addShipWorld(this)
    }

    private var lastPlayersSet: Set<IPlayer> = setOf()
    private val shipObjectMap = HashMap<UUID, ShipObjectServer>()
    // Explicitly make [shipObjects] a MutableMap so that we can use Iterator::remove()
    override val shipObjects: MutableMap<UUID, ShipObjectServer> = shipObjectMap
    val groundBodyUUID: UUID = UUID.randomUUID() // The UUID used when sending voxel updates for the ground shape

    // These fields are used to generate [VSGameFrame]
    private val newShipObjects: MutableList<ShipObjectServer> = ArrayList()
    private val updatedShipObjects: MutableList<ShipObjectServer> = ArrayList()
    private val deletedShipObjects: MutableList<UUID> = ArrayList()

    /**
     * A map of voxel updates pending to be applied to ships.
     *
     * These updates will be sent to the physics engine, however they are not applied immediately. The physics engine
     * has full control of when the updates are applied.
     */
    private val shipToVoxelUpdates: MutableMap<UUID?, MutableMap<Vector3ic, IVoxelShapeUpdate>> = HashMap()

    private var firstGameFrame = true

    /**
     * Add the update to [shipToVoxelUpdates].
     */
    override fun onSetBlock(
        posX: Int, posY: Int, posZ: Int, oldBlockType: VSBlockType, newBlockType: VSBlockType, oldBlockMass: Double,
        newBlockMass: Double
    ) {
        super.onSetBlock(posX, posY, posZ, oldBlockType, newBlockType, oldBlockMass, newBlockMass)

        if (oldBlockType != newBlockType) {
            val chunkPos: Vector3ic = Vector3i(posX shr 4, posY shr 4, posZ shr 4)

            val shipData: ShipData? = queryableShipData.getShipDataFromChunkPos(chunkPos.x(), chunkPos.z())

            val voxelUpdates = shipToVoxelUpdates.getOrPut(shipData?.shipUUID) { HashMap() }

            val voxelShapeUpdate =
                voxelUpdates.getOrPut(chunkPos) { SparseVoxelShapeUpdate.createSparseVoxelShapeUpdate(chunkPos) }

            val voxelType: Byte = when (newBlockType) {
                VSBlockType.AIR -> KrunchVoxelStates.AIR_STATE
                VSBlockType.SOLID -> KrunchVoxelStates.SOLID_STATE
                VSBlockType.WATER -> KrunchVoxelStates.WATER_STATE
                VSBlockType.LAVA -> KrunchVoxelStates.LAVA_STATE
                else -> throw IllegalArgumentException("Unknown blockType $newBlockType")
            }

            when (voxelShapeUpdate) {
                is SparseVoxelShapeUpdate -> {
                    // Add the update to the sparse voxel update
                    voxelShapeUpdate.addUpdate(posX and 15, posY and 15, posZ and 15, voxelType)
                }
                is DenseVoxelShapeUpdate -> {
                    // Add the update to the dense voxel update
                    voxelShapeUpdate.setVoxel(posX and 15, posY and 15, posZ and 15, voxelType)
                }
                is EmptyVoxelShapeUpdate -> {
                    // Replace the empty voxel update with a sparse update
                    val newVoxelShapeUpdate = SparseVoxelShapeUpdate.createSparseVoxelShapeUpdate(chunkPos)
                    newVoxelShapeUpdate.addUpdate(posX and 15, posY and 15, posZ and 15, voxelType)
                    voxelUpdates[chunkPos] = newVoxelShapeUpdate
                }
            }
        }
    }

    fun tickShips(newLoadedChunks: List<IVoxelShapeUpdate>) {
        val it = shipObjects.iterator()
        while (it.hasNext()) {
            val shipObjectServer = it.next().value
            if (shipObjectServer.shipData.inertiaData.getShipMass() < 1e-8) {
                // Delete this ship
                deletedShipObjects.add(shipObjectServer.shipData.shipUUID)
                queryableShipData.removeShipData(shipObjectServer.shipData)
                shipToVoxelUpdates.remove(shipObjectServer.shipData.shipUUID)
                it.remove()
            }
        }

        // For now just update very ship object every tick
        shipObjects.forEach { (_, shipObjectServer) ->
            updatedShipObjects.add(shipObjectServer)
        }

        // For now, just make a [ShipObject] for every [ShipData]
        for (shipData in queryableShipData) {
            val shipID = shipData.shipUUID
            if (!shipObjectMap.containsKey(shipID)) {
                val newShipObject = ShipObjectServer(shipData)
                newShipObjects.add(newShipObject)
                shipObjectMap[shipID] = newShipObject
            }
        }

        // region Add voxel shape updates for chunks that loaded this tick
        for (newLoadedChunk in newLoadedChunks) {
            val chunkPos: Vector3ic = Vector3i(newLoadedChunk.regionX, newLoadedChunk.regionY, newLoadedChunk.regionZ)
            val shipData: ShipData? = queryableShipData.getShipDataFromChunkPos(chunkPos.x(), chunkPos.z())
            val voxelUpdates = shipToVoxelUpdates.getOrPut(shipData?.shipUUID) { HashMap() }
            voxelUpdates[chunkPos] = newLoadedChunk
        }
        // endregion
    }

    /**
     * If the chunk at [chunkX], [chunkZ] is a ship chunk, then this returns the [IPlayer]s that are watching that ship chunk.
     *
     * If the chunk at [chunkX], [chunkZ] is not a ship chunk, then this returns nothing.
     */
    fun getIPlayersWatchingShipChunk(chunkX: Int, chunkZ: Int): Iterator<IPlayer> {
        // Check if this chunk potentially belongs to a ship
        if (ChunkAllocator.isChunkInShipyard(chunkX, chunkZ)) {
            // Then look for the shipData that owns this chunk
            val shipDataManagingPos = queryableShipData.getShipDataFromChunkPos(chunkX, chunkZ)
            if (shipDataManagingPos != null) {
                // Then check if there exists a ShipObject for this ShipData
                val shipObjectManagingPos = shipObjects[shipDataManagingPos.shipUUID]
                if (shipObjectManagingPos != null) {
                    return shipObjectManagingPos.shipChunkTracker.getPlayersWatchingChunk(chunkX, chunkZ)
                }
            }
        }
        return Collections.emptyIterator()
    }

    /**
     * Determines which ship chunks should be watched/unwatched by the players.
     *
     * It only returns the tasks, it is up to the caller to execute the tasks; however they do not have to execute all of them.
     * It is up to the caller to decide which tasks to execute, and which ones to skip.
     */
    fun tickShipChunkLoading(
        currentPlayers: Iterable<IPlayer>
    ): Pair<Spliterator<ChunkWatchTask>, Spliterator<ChunkUnwatchTask>> {
        val removedPlayers = lastPlayersSet - currentPlayers
        lastPlayersSet = currentPlayers.toHashSet()

        val chunkWatchTasksSorted = TreeSet<ChunkWatchTask>()
        val chunkUnwatchTasksSorted = TreeSet<ChunkUnwatchTask>()

        for (shipObject in shipObjects.values) {
            shipObject.shipChunkTracker.tick(
                players = currentPlayers,
                removedPlayers = removedPlayers,
                shipTransform = shipObject.shipData.shipTransform
            )

            val chunkWatchTasks = shipObject.shipChunkTracker.getChunkWatchTasks()
            val chunkUnwatchTasks = shipObject.shipChunkTracker.getChunkUnwatchTasks()

            chunkWatchTasks.forEach { chunkWatchTasksSorted.add(it) }
            chunkUnwatchTasks.forEach { chunkUnwatchTasksSorted.add(it) }
        }

        return Pair(chunkWatchTasksSorted.spliterator(), chunkUnwatchTasksSorted.spliterator())
    }

    /**
     * Creates a new [ShipData] centered at the block at [blockPosInWorldCoordinates].
     *
     * If [createShipObjectImmediately] is true then a [ShipObject] will be created immediately.
     */
    fun createNewShipAtBlock(
        blockPosInWorldCoordinates: Vector3ic, createShipObjectImmediately: Boolean, scaling: Double = 1.0
    ): ShipData {
        val chunkClaim = chunkAllocator.allocateNewChunkClaim()
        val shipName = NounListNameGenerator.generateName()

        val shipCenterInWorldCoordinates: Vector3dc = Vector3d(blockPosInWorldCoordinates).add(0.5, 0.5, 0.5)
        val blockPosInShipCoordinates: Vector3ic = chunkClaim.getCenterBlockCoordinates(Vector3i())
        val shipCenterInShipCoordinates: Vector3dc = Vector3d(blockPosInShipCoordinates).add(0.5, 0.5, 0.5)

        val newShipData = ShipData.createEmpty(
            name = shipName,
            chunkClaim = chunkClaim,
            shipCenterInWorldCoordinates = shipCenterInWorldCoordinates,
            shipCenterInShipCoordinates = shipCenterInShipCoordinates,
            scaling = scaling
        )

        queryableShipData.addShipData(newShipData)

        if (createShipObjectImmediately) {
            TODO("Not implemented")
        }

        return newShipData
    }

    override fun destroyWorld() {
        try {
            VSPipeline.getVSPipeline().removeShipWorld(this)
        } catch (e: Exception) {
            if (e is NullPointerException) {
                println("Tried unloading ship world $this, but the VS pipeline was already unloaded!")
            } else {
                e.printStackTrace()
            }
        }
    }

    fun getNewGroundRigidBodyObjects(): List<UUID> {
        return if (firstGameFrame) {
            firstGameFrame = false
            listOf(groundBodyUUID)
        } else {
            listOf()
        }
    }

    fun getNewShipObjects(): List<ShipObjectServer> {
        return newShipObjects
    }

    fun getUpdatedShipObjects(): List<ShipObjectServer> {
        return updatedShipObjects
    }

    fun getDeletedShipObjects(): List<UUID> {
        return deletedShipObjects
    }

    fun getShipToVoxelUpdates(): Map<UUID?, Map<Vector3ic, IVoxelShapeUpdate>> {
        return shipToVoxelUpdates
    }

    fun clearNewUpdatedDeletedShipObjectsAndVoxelUpdates() {
        newShipObjects.clear()
        updatedShipObjects.clear()
        deletedShipObjects.clear()
        shipToVoxelUpdates.clear()
    }
}
