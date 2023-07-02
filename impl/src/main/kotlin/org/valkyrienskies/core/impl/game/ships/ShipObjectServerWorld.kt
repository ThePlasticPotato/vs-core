package org.valkyrienskies.core.impl.game.ships

import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.Vector3i
import org.joml.Vector3ic
import org.valkyrienskies.core.api.ships.LoadedServerShip
import org.valkyrienskies.core.api.ships.QueryableShipData
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.world.LevelYRange
import org.valkyrienskies.core.apigame.ShipTeleportData
import org.valkyrienskies.core.apigame.constraints.VSConstraint
import org.valkyrienskies.core.apigame.constraints.VSConstraintAndId
import org.valkyrienskies.core.apigame.constraints.VSConstraintId
import org.valkyrienskies.core.apigame.constraints.VSForceConstraint
import org.valkyrienskies.core.apigame.physics.PhysicsEntityData
import org.valkyrienskies.core.apigame.physics.PhysicsEntityServer
import org.valkyrienskies.core.apigame.world.IPlayer
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore
import org.valkyrienskies.core.apigame.world.chunks.BlockType
import org.valkyrienskies.core.apigame.world.chunks.BlockTypes
import org.valkyrienskies.core.apigame.world.chunks.ChunkUnwatchTask
import org.valkyrienskies.core.apigame.world.chunks.ChunkWatchTask
import org.valkyrienskies.core.apigame.world.chunks.ChunkWatchTasks
import org.valkyrienskies.core.apigame.world.chunks.TerrainUpdate
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.impl.api.LoadedServerShipInternal
import org.valkyrienskies.core.impl.api.ServerShipInternal
import org.valkyrienskies.core.impl.datastructures.dynconn.BlockPosVertex
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnVertex
import org.valkyrienskies.core.impl.game.BlockTypeImpl
import org.valkyrienskies.core.impl.game.ChunkAllocatorProvider
import org.valkyrienskies.core.impl.game.DimensionInfo
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld.Stages.CLEAR_FOR_RESET
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld.Stages.GET_CURRENT_TICK_CHANGES
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld.Stages.GET_LAST_TICK_CHANGES
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld.Stages.POST_TICK_FINISH
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld.Stages.POST_TICK_GENERATED
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld.Stages.POST_TICK_START
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld.Stages.PRE_TICK
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld.Stages.UPDATE_BLOCKS
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld.Stages.UPDATE_CHUNKS
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld.Stages.UPDATE_DIMENSIONS
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld.Stages.UPDATE_PLAYERS
import org.valkyrienskies.core.impl.game.ships.loading.ShipLoadManagerServer
import org.valkyrienskies.core.impl.game.ships.modules.AllShips
import org.valkyrienskies.core.impl.game.ships.types.MutableShipVoxelUpdates
import org.valkyrienskies.core.impl.game.ships.types.ShipVoxelUpdates
import org.valkyrienskies.core.impl.game.ships.types.TerrainUpdateImpl
import org.valkyrienskies.core.impl.hooks.VSEvents
import org.valkyrienskies.core.impl.hooks.VSEvents.ShipLoadEvent
import org.valkyrienskies.core.impl.hooks.VSEvents.TickEndEvent
import org.valkyrienskies.core.impl.networking.NetworkChannel.Companion.logger
import org.valkyrienskies.core.impl.networking.VSNetworking
import org.valkyrienskies.core.impl.util.WorldScoped
import org.valkyrienskies.core.impl.util.assertions.stages.TickStageEnforcer
import org.valkyrienskies.core.impl.util.names.NounListNameGenerator
import org.valkyrienskies.physics_api.voxel.updates.DeleteVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel.updates.DenseVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel.updates.EmptyVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel.updates.IVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel.updates.SparseVoxelShapeUpdate
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import javax.inject.Named

@WorldScoped
class ShipObjectServerWorld @Inject constructor(
    @AllShips override val allShips: MutableQueryableShipDataServer,
    val chunkAllocators: ChunkAllocatorProvider,
    private val loadManager: ShipLoadManagerServer,
    networking: VSNetworking,
    private val blockTypes: BlockTypes,
    @Named("mutableDimensionInfo") private val dimensionInfo: MutableMap<DimensionId, DimensionInfo>
) : ShipObjectWorld<ShipObjectServer>(chunkAllocators), ServerShipWorldCore {

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
        UPDATE_PLAYERS,
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
            single(UPDATE_PLAYERS)
        }

        requireOrder(POST_TICK_GENERATED, GET_CURRENT_TICK_CHANGES, CLEAR_FOR_RESET)
        requireStages(PRE_TICK, POST_TICK_START, POST_TICK_GENERATED, POST_TICK_FINISH, CLEAR_FOR_RESET)
    }

    var lastTickPlayers: Set<IPlayer> = setOf()
        private set

    override var players: Set<IPlayer> = setOf()
        set(value) {
            enforcer.stage(UPDATE_PLAYERS)
            lastTickPlayers = field
            field = value
        }

    val playersToTrackedShips by loadManager::playersToTrackedShips

    private val _loadedShips = QueryableShipDataImpl<ShipObjectServer>()

    override val loadedShips: QueryableShipData<ShipObjectServer>
        get() = _loadedShips

    private val _loadedPhysicsEntities: MutableMap<ShipId, PhysicsEntityServer> = HashMap()

    val loadedPhysicsEntities: Map<ShipId, PhysicsEntityServer>
        get() = _loadedPhysicsEntities

    private val dimensionToGroundBodyId: MutableMap<DimensionId, ShipId> = HashMap()

    // An immutable view of [dimensionToGroundBodyId]
    override val dimensionToGroundBodyIdImmutable: Map<DimensionId, ShipId>
        get() = dimensionToGroundBodyId

    private val dimensionsAddedThisTick = ArrayList<DimensionId>()
    private val dimensionsRemovedThisTick = ArrayList<DimensionId>()
    private val voxelShapeUpdatesList = ArrayList<LevelVoxelUpdates>()

    // Map constraint ids to constraints. The positions of constraints are stored in the local coordinate system of the
    // ship.
    private val constraints: MutableMap<VSConstraintId, VSConstraint> = HashMap()
    private val shipIdToConstraints: MutableMap<ShipId, MutableSet<VSConstraintId>> = HashMap()
    private var nextConstraintId: VSConstraintId = 0

    // These fields are used to generate [VSGameFrame]
    private val newShipObjects: MutableList<ShipObjectServer> = ArrayList()
    private val newPhysicsEntities: MutableList<PhysicsEntityServer> = ArrayList()
    private val updatedShipObjects: MutableList<ShipObjectServer> = ArrayList()
    private val updatedPhysicsEntities: MutableList<PhysicsEntityServer> = ArrayList()
    private val deletedShipObjects: MutableList<ShipData> = ArrayList()
    private val deletedPhysicsEntities: MutableList<ShipId> = ArrayList()
    private var lastTickDeletedShipObjects: List<ShipData> = listOf()
        get() = enforcer.stage(GET_LAST_TICK_CHANGES).run { field }
    private var constraintsCreatedThisTick: MutableList<VSConstraintAndId> = ArrayList()
    private var constraintsUpdatedThisTick: MutableList<VSConstraintAndId> = ArrayList()
    private var constraintsDeletedThisTick: MutableList<VSConstraintId> = ArrayList()

    private val udpServer = networking.tryUdpServer()

    /**
     * A map of voxel updates pending to be applied to ships.
     *
     * These updates will be sent to the physics engine, however they are not applied immediately. The physics engine
     * has full control of when the updates are applied.
     */
    private val shipToVoxelUpdates: MutableShipVoxelUpdates = HashMap()

    @Deprecated(
        message = "All events moved to VSEvents",
        replaceWith = ReplaceWith("ShipLoadEvent", "org.valkyrienskies.core.impl.hooks.VSEvents.ShipLoadEvent")
    )
    val shipLoadEvent by VSEvents::shipLoadEvent

    /**
     * A class containing the result of the current tick. **This object is only valid for the tick it was produced in!**
     * Many of the maps/sets will be reused for efficiency's sake.
     */
    inner class CurrentTickChanges(
        val newShipObjects: Collection<ShipObjectServer>,
        val newPhysicsEntities: Collection<PhysicsEntityServer>,
        val updatedShipObjects: Collection<ShipObjectServer>,
        val updatedPhysicsEntities: Collection<PhysicsEntityServer>,
        val deletedShipObjects: Collection<ShipData>,
        val deletedPhysicsEntities: Collection<ShipId>,
        val shipToVoxelUpdates: ShipVoxelUpdates,
        val dimensionsAddedThisTick: Collection<DimensionId>,
        val dimensionsRemovedThisTick: Collection<DimensionId>,
        val constraintsCreatedThisTick: Collection<VSConstraintAndId>,
        val constraintsUpdatedThisTick: Collection<VSConstraintAndId>,
        val constraintsDeletedThisTick: Collection<VSConstraintId>
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
            return deletedGroundShips + deletedShipObjects.map { it.id } + deletedPhysicsEntities
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
        oldBlockType: BlockType,
        newBlockType: BlockType,
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



            if (shipData != null && loadedShips.getById(shipId) != null) {
                // val serverShip : ServerShip  = allShips.getById(shipId) as ServerShip
                val serverShip : LoadedServerShip = loadedShips.getById(shipId) as LoadedServerShip
                if (serverShip.getAttachment(ConnectivityForest::class.java) != null) {
                    val forest: ConnectivityForestImpl = serverShip.getAttachment(ConnectivityForest::class.java)!! as ConnectivityForestImpl

                    val voxelType = (newBlockType as BlockTypeImpl)

                    if (voxelType == BlockTypeImpl.AIR) {
                        // ignore this, should never not be a BlockPosVertex anyway, if it is we've got bigger problems
                        val adjacentVertexes : Collection<BlockPosVertex> = forest.graph.adjacentVertices(forest.vertices.get(Vector3i(posX, posY, posZ))) as Collection<BlockPosVertex>
                        forest.delVertex(posX, posY, posZ)
                        if (!adjacentVertexes.isEmpty()) {
                            // check if the ship is still intact
                            var disconnectOne: Vector3ic? = null
                            var disconnectTwo: Vector3ic? = null
                            var disconnectThree: Vector3ic? = null
                            var disconnectFour: Vector3ic? = null
                            var disconnectFive: Vector3ic? = null
                            var disconnectSix: Vector3ic? = null

                            var foundTwo = false
                            var foundThree = false
                            var foundFour = false
                            var foundFive = false
                            var foundSix = false

                            var intact = true

                            for (it in adjacentVertexes) {
                                for (otherit in adjacentVertexes) {
                                    if (!forest.graph.connected(it, otherit)) {
                                        intact = false
                                        disconnectOne = Vector3i(it.posX, it.posY, it.posZ)
                                        if (!foundTwo) {
                                            disconnectTwo = Vector3i(otherit.posX, otherit.posY, otherit.posZ)
                                            foundTwo = true
                                        } else if (!foundThree) {
                                            disconnectThree = Vector3i(otherit.posX, otherit.posY, otherit.posZ)
                                            foundThree = true
                                        } else if (!foundFour) {
                                            disconnectFour = Vector3i(otherit.posX, otherit.posY, otherit.posZ)
                                            foundFour = true
                                        } else if (!foundFive) {
                                            disconnectFive = Vector3i(otherit.posX, otherit.posY, otherit.posZ)
                                            foundFive = true
                                        } else if (!foundSix) {
                                            disconnectSix = Vector3i(otherit.posX, otherit.posY, otherit.posZ)
                                            foundSix = true
                                            break
                                        }
                                        logger.info("Ship with ID '$shipId' is no longer intact! Breakage point: $posX, $posY, $posZ")

                                    }
                                }
                            }

                            if (!intact && disconnectOne != null && disconnectTwo != null) {
                                forest.addToBreakQueue(arrayListOf(disconnectOne, disconnectTwo, disconnectThree, disconnectFour, disconnectFive, disconnectSix))
                            }

                        }
                    } else {
                        forest.newVertex(posX, posY, posZ)
                        val adjacentVertexes : Collection<ConnVertex> = forest.graph.adjacentVertices(forest.vertices.get(Vector3i(posX, posY, posZ)))
                        if (adjacentVertexes.isEmpty()) {
                            // someone used setblock :3dsus: or some mod jank idk
                        }
                    }
                }

            }

            val voxelUpdates = shipToVoxelUpdates.getOrPut(shipId) { HashMap() }

            val voxelShapeUpdate =
                voxelUpdates.getOrPut(chunkPos) { SparseVoxelShapeUpdate.createSparseVoxelShapeUpdate(chunkPos) }

            val voxelType: Int = (newBlockType as BlockTypeImpl).state

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
                else -> {
                    TODO()
                }
            }
        }
    }

    override fun addTerrainUpdates(dimensionId: DimensionId, terrainUpdates: List<TerrainUpdate>) {
        enforcer.stage(UPDATE_CHUNKS)

        val voxelShapeUpdates = terrainUpdates.map { (it as TerrainUpdateImpl).update }
        voxelShapeUpdatesList.add(LevelVoxelUpdates(dimensionId, voxelShapeUpdates))

        voxelShapeUpdates.forEach {
            val ship = allShips.getByChunkPos(it.regionX, it.regionZ, dimensionId) ?: return@forEach

            // TODO: Can we be a bit more explicit about how this works? Maybe revamp addTerrainUpdates
            when (it) {
                is DenseVoxelShapeUpdate, is EmptyVoxelShapeUpdate -> ship.onLoadChunk(it.regionX, it.regionZ)
                is DeleteVoxelShapeUpdate -> ship.onUnloadChunk(it.regionX, it.regionZ)
                else -> {
                    TODO()
                }
            }

            if (it is DenseVoxelShapeUpdate) {
                it.forEachVoxel { x, y, z, voxelState ->
                    ship.updateShipAABBGenerator(
                        (it.regionX shl 4) + x, (it.regionY shl 4) + y, (it.regionZ shl 4) + z,
                        voxelState != blockTypes.air.toInt()
                    )
                }
            }

        }
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

        // Delete any ships with a mass less 1e-8
        _loadedShips.filter { it.shipData.inertiaData.mass < 1e-8 }.forEach { deleteShip(it) }

        // For now just update very ship object every tick
        shipObjects.forEach { (_, shipObjectServer) ->
            updatedShipObjects.add(shipObjectServer)
        }

        loadedPhysicsEntities.forEach { (_, physicsEntity) ->
            if (physicsEntity.needsUpdating) {
                updatedPhysicsEntities.add(physicsEntity)
                physicsEntity.needsUpdating = false
            }
        }

        // Remove constraints from deleted ships
        val allRemoved = deletedPhysicsEntities + deletedShipObjects.map { it.id }
        allRemoved.forEach { id ->
            val constraintIds = shipIdToConstraints[id] ?: return@forEach
            // Copy these constraint ids to avoid ConcurrentModificationException
            val constraintIdsCopy: List<VSConstraintId> = ArrayList(constraintIds)
            constraintIdsCopy.forEach { constraintId ->
                removeConstraint(constraintId)
            }
        }

        // For now just update every [VSForceConstraint] every tick
        // TODO: Only do this when the center of mass of the ship has changed
        constraints.forEach { (constraintId, constraint) ->
            if (constraint is VSForceConstraint) {
                constraintsUpdatedThisTick.add(VSConstraintAndId(constraintId, constraint))
            }
        }

        val shipsLoadedThisTick = mutableListOf<ShipObjectServer>()

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

    override fun setExecutedChunkWatchTasks(
        watchTasks: Iterable<ChunkWatchTask>, unwatchTasks: Iterable<ChunkUnwatchTask>
    ) {
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
        val blockPosInShipCoordinates: Vector3ic = chunkClaim.getCenterBlockCoordinates(getYRange(dimensionId))
        val shipCenterInShipCoordinates: Vector3dc = Vector3d(blockPosInShipCoordinates).add(0.5, 0.5, 0.5)
        val newShipData = ShipData.createEmpty(
            slug = shipName,
            shipId = chunkAllocator.allocateShipId(),
            chunkClaim = chunkClaim,
            chunkClaimDimension = dimensionId,
            shipCenterInWorldCoordinates = shipCenterInWorldCoordinates,
            shipCenterInShipCoordinates = shipCenterInShipCoordinates,
            scaling = scaling
        )

        allShips.add(newShipData)

        if (createShipObjectImmediately) {
            TODO("Not implemented")
        }

        return newShipData
    }

    override fun createPhysicsEntity(physicsEntityData: PhysicsEntityData, dimensionId: DimensionId): PhysicsEntityServer {
        if (_loadedPhysicsEntities.contains(physicsEntityData.shipId)) {
            throw IllegalArgumentException(
                "We already have a physics entity registered to id ${physicsEntityData.shipId}!"
            )
        }
        val physicsEntity = PhysicsEntityServer(
            id = physicsEntityData.shipId,
            dimensionId = dimensionId,
            linearVelocity = physicsEntityData.linearVelocity,
            angularVelocity = physicsEntityData.angularVelocity,
            inertiaData = physicsEntityData.inertiaData,
            shipTransform = physicsEntityData.transform,
            prevTickShipTransform = physicsEntityData.transform,
            shipTeleportId = 0,
            collisionShapeData = physicsEntityData.collisionShapeData,
            collisionMask = physicsEntityData.collisionMask,
            staticFrictionCoefficient = physicsEntityData.staticFrictionCoefficient,
            dynamicFrictionCoefficient = physicsEntityData.dynamicFrictionCoefficient,
            restitutionCoefficient = physicsEntityData.restitutionCoefficient,
            isStatic = physicsEntityData.isStatic
        )
        _loadedPhysicsEntities[physicsEntityData.shipId] = physicsEntity
        newPhysicsEntities.add(physicsEntity)
        return physicsEntity
    }

    override fun deletePhysicsEntity(id: ShipId) {
        if (!_loadedPhysicsEntities.contains(id)) {
            throw IllegalArgumentException(
                "Trying to remove a physics entity that doesn't exist with id ${id}!"
            )
        }
        _loadedPhysicsEntities.remove(id)
        deletedPhysicsEntities.add(id)
    }

    override fun allocateShipId(dimensionId: DimensionId): ShipId {
        val chunkAllocator = chunkAllocators.forDimension(dimensionId)
        return chunkAllocator.allocateShipId()
    }

    // region [rigidBodyIdToConstraints] helpers
    private fun addConstraintIdToRigidBodyIdToConstraintsHelper(
        shipId: ShipId, newConstraintId: VSConstraintId
    ) = shipIdToConstraints.getOrPut(shipId) { HashSet() }.add(newConstraintId)

    private fun removeConstraintIdFromRigidBodyIdToConstraintsHelper(
        rigidBodyId: ShipId, oldConstraintId: VSConstraintId
    ) {
        val constraintIds = shipIdToConstraints[rigidBodyId] ?: return
        constraintIds.remove(oldConstraintId)
        if (constraintIds.isEmpty()) shipIdToConstraints.remove(rigidBodyId)
    }
    // endregion

    private fun isShipLoaded(shipId: ShipId): Boolean {
        return allShips.contains(shipId) || dimensionToGroundBodyIdImmutable.containsValue(shipId)
    }

    override fun createNewConstraint(vsConstraint: VSConstraint): VSConstraintId? {
        if (!isShipLoaded(vsConstraint.shipId0) || !isShipLoaded(vsConstraint.shipId1))
            return null

        val constraintId = nextConstraintId++
        constraints[constraintId] = vsConstraint

        addConstraintIdToRigidBodyIdToConstraintsHelper(vsConstraint.shipId0, constraintId)
        addConstraintIdToRigidBodyIdToConstraintsHelper(vsConstraint.shipId1, constraintId)

        // Send a constraint CREATE to the physics engine
        constraintsCreatedThisTick.add(VSConstraintAndId(constraintId, vsConstraint))

        return constraintId
    }

    override fun updateConstraint(constraintId: VSConstraintId, updatedVSConstraint: VSConstraint): Boolean {
        val oldConstraint = constraints[constraintId] ?: return false

        if (!isShipLoaded(updatedVSConstraint.shipId0)
            || !isShipLoaded(updatedVSConstraint.shipId1)
        ) {
            return false
        }

        // Update [rigidBodyIdToConstraints] if necessary
        if (oldConstraint.shipId0 != updatedVSConstraint.shipId0
            || oldConstraint.shipId1 != updatedVSConstraint.shipId1
        ) {
            removeConstraintIdFromRigidBodyIdToConstraintsHelper(oldConstraint.shipId0, constraintId)
            removeConstraintIdFromRigidBodyIdToConstraintsHelper(oldConstraint.shipId1, constraintId)
            addConstraintIdToRigidBodyIdToConstraintsHelper(updatedVSConstraint.shipId0, constraintId)
            addConstraintIdToRigidBodyIdToConstraintsHelper(updatedVSConstraint.shipId1, constraintId)
        }

        // Send a constraint UPDATE to the physics engine
        constraintsUpdatedThisTick.add(VSConstraintAndId(constraintId, updatedVSConstraint))

        return true
    }

    override fun removeConstraint(constraintId: VSConstraintId): Boolean {
        val oldConstraint = constraints[constraintId] ?: return false

        removeConstraintIdFromRigidBodyIdToConstraintsHelper(oldConstraint.shipId0, constraintId)
        removeConstraintIdFromRigidBodyIdToConstraintsHelper(oldConstraint.shipId1, constraintId)

        // Send a constraint DELETE to the physics engine
        constraintsDeletedThisTick.add(constraintId)

        // Actually remove the constraint
        constraints.remove(constraintId)

        return true
    }

    fun getYRange(dimensionId: DimensionId) = dimensionInfo.getValue(dimensionId).yRange

    override fun destroyWorld() {
    }

    fun getCurrentTickChanges(): CurrentTickChanges {
        enforcer.stage(GET_CURRENT_TICK_CHANGES)

        return CurrentTickChanges(
            newShipObjects,
            newPhysicsEntities,
            updatedShipObjects,
            updatedPhysicsEntities,
            deletedShipObjects,
            deletedPhysicsEntities,
            shipToVoxelUpdates,
            dimensionsAddedThisTick,
            dimensionsRemovedThisTick,
            constraintsCreatedThisTick,
            constraintsUpdatedThisTick,
            constraintsDeletedThisTick
        )
    }

    fun clearNewUpdatedDeletedShipObjectsAndVoxelUpdates() {
        enforcer.stage(CLEAR_FOR_RESET)
        newShipObjects.clear()
        newPhysicsEntities.clear()
        updatedShipObjects.clear()
        updatedPhysicsEntities.clear()
        lastTickDeletedShipObjects = deletedShipObjects.toList()
        deletedShipObjects.clear()
        deletedPhysicsEntities.clear()
        shipToVoxelUpdates.clear()
        voxelShapeUpdatesList.clear()
        dimensionsAddedThisTick.clear()
        dimensionsRemovedThisTick.forEach { dimensionRemovedThisTick: DimensionId ->
            val removedSuccessfully = dimensionToGroundBodyId.remove(dimensionRemovedThisTick) != null
            check(removedSuccessfully)
        }
        dimensionsRemovedThisTick.clear()
        constraintsCreatedThisTick = ArrayList()
        constraintsUpdatedThisTick = ArrayList()
        constraintsDeletedThisTick = ArrayList()
    }

    override fun addDimension(dimensionId: DimensionId, yRange: LevelYRange) {
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

    override fun deleteShip(ship: ServerShip) {
        if (_loadedShips.contains(ship.id)) {
            // TODO Fix this (ruby u know how?)
            val shipData = if (ship is ShipObjectServer) {
                ship.shipData
            } else {
                ship as ShipData
            }

            // Only try to unload the ship if it was loaded
            deletedShipObjects.add(shipData)
            _loadedShips.remove(ship.id)
        }

        allShips.remove(ship.id)
        shipToVoxelUpdates.remove(ship.id)
    }

    override fun teleportShip(ship: ServerShip, teleportData: ShipTeleportData) {
        if (ship is LoadedServerShipInternal) {
            ship.teleportShip(teleportData)
        } else {
            val shipAsLoaded = loadedShips.getById(ship.id)
            if (shipAsLoaded != null) {
                shipAsLoaded.teleportShip(teleportData)
            } else {
                // TODO: Do we want to change this?
                // (ship as ShipData).prevTickTransform = newTransform
                (ship as ShipData).transform = teleportData.createNewShipTransform(ship.transform)
            }
        }
    }

    override fun teleportPhysicsEntity(physicsEntityServer: PhysicsEntityServer, teleportData: ShipTeleportData) {
        physicsEntityServer.shipTeleportId++
        physicsEntityServer.shipTransform = teleportData.createNewShipTransform(physicsEntityServer.shipTransform)
        physicsEntityServer.linearVelocity = teleportData.newVel
        physicsEntityServer.angularVelocity = teleportData.newOmega
        physicsEntityServer.needsUpdating = true
    }

    data class LevelVoxelUpdates(
        val dimensionId: DimensionId,
        val updates: List<IVoxelShapeUpdate>
    )
}
