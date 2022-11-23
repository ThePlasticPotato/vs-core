package org.valkyrienskies.core.game.ships

import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.Vector3i
import org.joml.Vector3ic
import org.valkyrienskies.core.api.ServerShipInternal
import org.valkyrienskies.core.api.ships.QueryableShipData
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.VSBlockType
import org.valkyrienskies.core.api.world.IPlayer
import org.valkyrienskies.core.api.world.ServerShipWorldGame
import org.valkyrienskies.core.api.world.chunks.ChunkUnwatchTask
import org.valkyrienskies.core.api.world.chunks.ChunkWatchTask
import org.valkyrienskies.core.api.world.chunks.ChunkWatchTasks
import org.valkyrienskies.core.api.world.chunks.TerrainUpdate
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.game.ChunkAllocatorProvider
import org.valkyrienskies.core.game.DimensionInfo
import org.valkyrienskies.core.game.VSBlockTypeImpl
import org.valkyrienskies.core.game.ships.ShipObjectServerWorld.Stages.*
import org.valkyrienskies.core.game.ships.loading.ShipLoadManagerServer
import org.valkyrienskies.core.game.ships.modules.AllShips
import org.valkyrienskies.core.game.ships.types.MutableShipVoxelUpdates
import org.valkyrienskies.core.game.ships.types.ShipVoxelUpdates
import org.valkyrienskies.core.game.ships.types.TerrainUpdateImpl
import org.valkyrienskies.core.hooks.VSEvents
import org.valkyrienskies.core.hooks.VSEvents.ShipLoadEvent
import org.valkyrienskies.core.hooks.VSEvents.TickEndEvent
import org.valkyrienskies.core.networking.NetworkChannel.Companion.logger
import org.valkyrienskies.core.networking.VSNetworking
import org.valkyrienskies.core.util.WorldScoped
import org.valkyrienskies.core.util.assertions.stages.TickStageEnforcer
import org.valkyrienskies.core.util.names.NounListNameGenerator
import org.valkyrienskies.physics_api.voxel_updates.DenseVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel_updates.EmptyVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel_updates.IVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel_updates.SparseVoxelShapeUpdate
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

@WorldScoped
class ShipObjectServerWorld @Inject constructor(
    @AllShips override val allShips: MutableQueryableShipDataServer,
    val chunkAllocators: ChunkAllocatorProvider,
    private val loadManager: ShipLoadManagerServer,
    networking: VSNetworking
) : ShipObjectWorld<ShipObjectServer>(chunkAllocators), ServerShipWorldGame {

    private enum class Stages {
        PRE_TICK,
        POST_TICK_START,

        /**
         * Fires once [ShipObjectServerWorld.getCurrentTickChanges] can be called and is updated with the correct data
         */
        POST_TICK_GENERATED,
        POST_TICK_FINISH,
        GET_CURRENT_TICK_CHANGES,
        GET_LAST_TICK_CHANGES,
        UPDATE_DIMENSIONS,
        UPDATE_BLOCKS,
        UPDATE_CHUNKS,
        CLEAR_FOR_RESET
    }

    private val enforcer = TickStageEnforcer(PRE_TICK) {
        ignoreUntilFirstReset()

        requireOrder {
            single(PRE_TICK)
            anyOf(UPDATE_DIMENSIONS, UPDATE_BLOCKS, UPDATE_CHUNKS)
            single(POST_TICK_START)
            single(POST_TICK_GENERATED)
            single(POST_TICK_FINISH)
            single(CLEAR_FOR_RESET)
        }

        requireOrder(POST_TICK_GENERATED, GET_CURRENT_TICK_CHANGES, CLEAR_FOR_RESET)
        requireStages(PRE_TICK, POST_TICK_START, POST_TICK_GENERATED, POST_TICK_FINISH, CLEAR_FOR_RESET)

        requireFinal(CLEAR_FOR_RESET)
    }

    var lastTickPlayers: Set<IPlayer> = setOf()
        private set

    var players: Set<IPlayer> = setOf()
        set(value) {
            lastTickPlayers = field
            field = value
        }

    val playersToTrackedShips by loadManager::playersToTrackedShips

    private val _loadedShips = QueryableShipDataImpl<ShipObjectServer>()

    override val loadedShips: QueryableShipData<ShipObjectServer>
        get() = _loadedShips

    private val dimensionToGroundBodyId: MutableMap<DimensionId, ShipId> = HashMap()

    // An immutable view of [dimensionToGroundBodyId]
    val dimensionToGroundBodyIdImmutable: Map<DimensionId, ShipId>
        get() = dimensionToGroundBodyId

    private val dimensionsAddedThisTick = ArrayList<DimensionId>()
    private val dimensionsRemovedThisTick = ArrayList<DimensionId>()
    private val voxelShapeUpdatesList = ArrayList<LevelVoxelUpdates>()

    // These fields are used to generate [VSGameFrame]
    private val newShipObjects: MutableList<ShipObjectServer> = ArrayList()
    private val updatedShipObjects: MutableList<ShipObjectServer> = ArrayList()
    private val deletedShipObjects: MutableList<ShipData> = ArrayList()
    private var lastTickDeletedShipObjects: List<ShipData> = listOf()
        get() = enforcer.stage(GET_LAST_TICK_CHANGES).run { field }

    private val udpServer = networking.tryUdpServer()

    private val dimensionInfo = mutableMapOf<DimensionId, DimensionInfo>()

    /**
     * A map of voxel updates pending to be applied to ships.
     *
     * These updates will be sent to the physics engine, however they are not applied immediately. The physics engine
     * has full control of when the updates are applied.
     */
    private val shipToVoxelUpdates: MutableShipVoxelUpdates = HashMap()

    @Deprecated(
        message = "All events moved to VSEvents",
        replaceWith = ReplaceWith("ShipLoadEvent", "org.valkyrienskies.core.hooks.VSEvents.ShipLoadEvent")
    )
    val shipLoadEvent by VSEvents::shipLoadEvent

    /**
     * A class containing the result of the current tick. **This object is only valid for the tick it was produced in!**
     * Many of the maps/sets will be reused for efficiency's sake.
     */
    inner class CurrentTickChanges(
        val newShipObjects: Collection<ShipObjectServer>,
        val updatedShipObjects: Collection<ShipObjectServer>,
        val deletedShipObjects: Collection<ShipData>,
        val shipToVoxelUpdates: ShipVoxelUpdates,
        val dimensionsAddedThisTick: Collection<DimensionId>,
        val dimensionsRemovedThisTick: Collection<DimensionId>
    ) {
        fun getNewGroundRigidBodyObjects(): List<Pair<DimensionId, ShipId>> {
            return dimensionsAddedThisTick.map { dimensionId: DimensionId ->
                Pair(dimensionId, dimensionToGroundBodyId[dimensionId]!!)
            }
        }

        fun getDeletedShipObjectsIncludingGround(): List<ShipId> {
            val deletedGroundShips = dimensionsRemovedThisTick.map { dim: DimensionId ->
                dimensionToGroundBodyId[dim]!!
            }
            return deletedGroundShips + deletedShipObjects.map { it.id }
        }
    }

    /**
     * Add the update to [shipToVoxelUpdates].
     */
    override fun onSetBlock(
        posX: Int,
        posY: Int,
        posZ: Int,
        dimensionId: DimensionId,
        oldBlockType: VSBlockType,
        newBlockType: VSBlockType,
        oldBlockMass: Double,
        newBlockMass: Double
    ) {
        super.onSetBlock(posX, posY, posZ, dimensionId, oldBlockType, newBlockType, oldBlockMass, newBlockMass)

        if (oldBlockType != newBlockType) {
            val chunkPos: Vector3ic = Vector3i(posX shr 4, posY shr 4, posZ shr 4)

            val shipData: ShipData? = allShips.getShipDataFromChunkPos(chunkPos.x(), chunkPos.z(), dimensionId)

            val shipId: ShipId? = shipData?.id ?: dimensionToGroundBodyId[dimensionId]

            if (shipId == null) {
                logger.error(
                    "Could not find ship or dimension body for block update at $posX, $posY, $posZ in dimension $dimensionId"
                )
                return
            }

            val voxelUpdates = shipToVoxelUpdates.getOrPut(shipId) { HashMap() }

            val voxelShapeUpdate =
                voxelUpdates.getOrPut(chunkPos) { SparseVoxelShapeUpdate.createSparseVoxelShapeUpdate(chunkPos) }

            val voxelType: Byte = (newBlockType as VSBlockTypeImpl).state

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

    override fun addTerrainUpdates(dimensionId: DimensionId, terrainUpdates: List<TerrainUpdate>) {
        enforcer.stage(UPDATE_CHUNKS)

        val voxelShapeUpdates = terrainUpdates.map { (it as TerrainUpdateImpl).update }
        voxelShapeUpdatesList.add(LevelVoxelUpdates(dimensionId, voxelShapeUpdates))
    }

    fun getShipObject(ship: ServerShipInternal): ShipObjectServer? {
        return loadedShips.getById(ship.id)
    }

    override fun preTick() {
        enforcer.stage(PRE_TICK)
        super.preTick()

        loadManager.preTick(players, lastTickPlayers, allShips, lastTickDeletedShipObjects)
    }

    fun postTick() {
        enforcer.stage(POST_TICK_START)

        val shipsLoadedThisTick = mutableListOf<ShipObjectServer>()
        val it = _loadedShips.iterator()
        while (it.hasNext()) {
            val shipObjectServer = it.next()
            if (shipObjectServer.shipData.inertiaData.mass < 1e-8) {
                // Delete this ship
                deletedShipObjects.add(shipObjectServer.shipData)
                allShips.removeShipData(shipObjectServer.shipData)
                shipToVoxelUpdates.remove(shipObjectServer.shipData.id)
                it.remove()
            }
        }

        // For now just update very ship object every tick
        shipObjects.forEach { (_, shipObjectServer) ->
            updatedShipObjects.add(shipObjectServer)
        }

        // For now, just make a [ShipObject] for every [ShipData]
        for (shipData in allShips) {
            val shipID = shipData.id

            // save us from the invMass is not finite! error
            if (shipData.inertiaData.mass == 0.0) {
                logger.warn("Ship with ID $shipID has a mass of 0.0, not creating a ShipObject")
                continue
            }

            if (!_loadedShips.contains(shipID)) {
                val newShipObject = ShipObjectServer(shipData)
                newShipObjects.add(newShipObject)
                _loadedShips.addShipData(newShipObject)
                shipsLoadedThisTick.add(newShipObject)
            }
        }

        // region Add voxel shape updates for chunks that loaded this tick
        for (newLoadedChunkAndDimension in voxelShapeUpdatesList) {
            val (dimensionId, shapeUpdates) = newLoadedChunkAndDimension
            for (newLoadedChunk in shapeUpdates) {
                val chunkPos: Vector3ic =
                    Vector3i(newLoadedChunk.regionX, newLoadedChunk.regionY, newLoadedChunk.regionZ)
                val shipData: ShipData? =
                    allShips.getShipDataFromChunkPos(chunkPos.x(), chunkPos.z(), dimensionId)

                val shipId: ShipId = shipData?.id ?: dimensionToGroundBodyId[dimensionId]!!

                val voxelUpdates = shipToVoxelUpdates.getOrPut(shipId) { HashMap() }
                voxelUpdates[chunkPos] = newLoadedChunk
            }
        }
        // endregion
        enforcer.stage(POST_TICK_GENERATED)

        loadManager.postTick(players)

        shipsLoadedThisTick.forEach { VSEvents.shipLoadEvent.emit(ShipLoadEvent(it)) }

        VSEvents.tickEndEvent.emit(TickEndEvent(this))

        enforcer.stage(POST_TICK_FINISH)
    }

    /**
     * If the chunk at [chunkX], [chunkZ] is a ship chunk, then this returns the [IPlayer]s that are watching that ship chunk.
     *
     * If the chunk at [chunkX], [chunkZ] is not a ship chunk, then this returns nothing.
     */
    override fun getIPlayersWatchingShipChunk(chunkX: Int, chunkZ: Int, dimensionId: DimensionId): Iterator<IPlayer> {
        return loadManager.getIPlayersWatchingShipChunk(chunkX, chunkZ, dimensionId)
    }

    /**
     * Determines which ship chunks should be watched/unwatched by the players.
     *
     * It only returns the tasks, it is up to the caller to execute the tasks; however they do not have to execute all of them.
     * It is up to the caller to decide which tasks to execute, and which ones to skip.
     */
    override fun getChunkWatchTasks(): ChunkWatchTasks {
        return loadManager.chunkWatchTasks
    }

    override fun setExecutedChunkWatchTasks(watchTasks: Iterable<ChunkWatchTask>, unwatchTasks: Iterable<ChunkUnwatchTask>) {
        loadManager.setExecutedChunkWatchTasks(watchTasks, unwatchTasks)
    }

    /**
     * Queues a new ship to be created and returns a future that executes on the
     * game thread when the ship is loaded.
     */
    fun createNewShipObjectAtBlock(
        blockPosInWorldCoordinates: Vector3ic, createShipObjectImmediately: Boolean, scaling: Double = 1.0,
        dimensionId: DimensionId
    ): CompletableFuture<ShipObjectServer> {
        val future = CompletableFuture<ShipObjectServer>()
        val shipData =
            createNewShipAtBlock(blockPosInWorldCoordinates, createShipObjectImmediately, scaling, dimensionId)

        ShipLoadEvent.once({ it.ship.shipData.id == shipData.id }) {
            future.complete(it.ship)
        }

        return future
    }

    /**
     * Creates a new [ShipData] centered at the block at [blockPosInWorldCoordinates].
     *
     * If [createShipObjectImmediately] is true then a [ShipObject] will be created immediately.
     */
    override fun createNewShipAtBlock(
        blockPosInWorldCoordinates: Vector3ic, createShipObjectImmediately: Boolean, scaling: Double,
        dimensionId: DimensionId
    ): ShipData {
        val chunkAllocator = chunkAllocators.forDimension(dimensionId)
        val chunkClaim = chunkAllocator.allocateNewChunkClaim()
        val shipName = NounListNameGenerator.generateName()

        val shipCenterInWorldCoordinates: Vector3dc = Vector3d(blockPosInWorldCoordinates).add(0.5, 0.5, 0.5)
        val blockPosInShipCoordinates: Vector3ic = chunkClaim.getCenterBlockCoordinates(Vector3i())
        val shipCenterInShipCoordinates: Vector3dc = Vector3d(blockPosInShipCoordinates).add(0.5, 0.5, 0.5)
        val newShipData = ShipData.createEmpty(
            name = shipName,
            shipId = chunkAllocator.allocateShipId(),
            chunkClaim = chunkClaim,
            chunkClaimDimension = dimensionId,
            shipCenterInWorldCoordinates = shipCenterInWorldCoordinates,
            shipCenterInShipCoordinates = shipCenterInShipCoordinates,
            scaling = scaling
        )

        allShips.addShipData(newShipData)

        if (createShipObjectImmediately) {
            TODO("Not implemented")
        }

        return newShipData
    }

    override fun destroyWorld() {
    }

    fun getCurrentTickChanges(): CurrentTickChanges {
        enforcer.stage(GET_CURRENT_TICK_CHANGES)

        return CurrentTickChanges(
            newShipObjects,
            updatedShipObjects,
            deletedShipObjects,
            shipToVoxelUpdates,
            dimensionsAddedThisTick,
            dimensionsRemovedThisTick
        )
    }

    fun clearNewUpdatedDeletedShipObjectsAndVoxelUpdates() {
        enforcer.stage(CLEAR_FOR_RESET)
        newShipObjects.clear()
        updatedShipObjects.clear()
        lastTickDeletedShipObjects = deletedShipObjects.toList()
        deletedShipObjects.clear()
        shipToVoxelUpdates.clear()
        voxelShapeUpdatesList.clear()
        dimensionsAddedThisTick.clear()
        dimensionsRemovedThisTick.forEach { dimensionRemovedThisTick: DimensionId ->
            val removedSuccessfully = dimensionToGroundBodyId.remove(dimensionRemovedThisTick) != null
            check(removedSuccessfully)
        }
        dimensionsRemovedThisTick.clear()
    }

    override fun addDimension(dimensionId: DimensionId, yRange: IntRange) {
        enforcer.stage(UPDATE_DIMENSIONS)
        require(!dimensionInfo.contains(dimensionId))
        require(!dimensionToGroundBodyId.contains(dimensionId))

        dimensionsAddedThisTick.add(dimensionId)
        dimensionToGroundBodyId[dimensionId] = chunkAllocators.forDimension(dimensionId).allocateShipId()
        dimensionInfo[dimensionId] = DimensionInfo(dimensionId, yRange)
    }

    override fun removeDimension(dimensionId: DimensionId) {
        enforcer.stage(UPDATE_DIMENSIONS)
        require(dimensionInfo.contains(dimensionId))
        require(dimensionToGroundBodyId.contains(dimensionId))

        dimensionsRemovedThisTick.add(dimensionId)
        dimensionInfo.remove(dimensionId)
    }

    override fun onDisconnect(player: IPlayer) {
        udpServer?.disconnect(player)
    }

    data class LevelVoxelUpdates(
        val dimensionId: DimensionId,
        val updates: List<IVoxelShapeUpdate>
    )
}
