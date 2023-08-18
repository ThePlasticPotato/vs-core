package org.valkyrienskies.core.impl.chunk_tracking

import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.longs.LongSet
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet
import org.valkyrienskies.core.apigame.world.IPlayer
import org.valkyrienskies.core.apigame.world.chunks.ChunkUnwatchTask
import org.valkyrienskies.core.apigame.world.chunks.ChunkWatchTask
import org.valkyrienskies.core.apigame.world.chunks.ChunkWatchTasks
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.impl.api.ServerShipInternal
import org.valkyrienskies.core.impl.config.VSCoreConfig
import org.valkyrienskies.core.impl.game.DimensionInfo
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld
import org.valkyrienskies.core.impl.util.set
import org.valkyrienskies.core.impl.util.signedDistanceTo
import java.util.*
import java.util.function.LongFunction
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.min

class ShipObjectServerWorldChunkTracker @Inject constructor(
    val config: VSCoreConfig.Server,
    @Named("dimensionInfo") val dimensionInfoMap: Map<DimensionId, DimensionInfo>
) {

    private val chunkToPlayersWatchingMap: Long2ObjectMap<MutableSet<IPlayer>> = Long2ObjectOpenHashMap()

    /**
     * Player -> Ship Id -> chunks on that ship the player is tracking
     */
    private val playersToShipsWatchingMap =
        HashMap<IPlayer, MutableMap<ServerShipInternal, LongSet>>()

    /**
     * Ship Id -> Players watching
     */
    private val shipsToPlayersWatchingMap = Long2ObjectOpenHashMap<MutableSet<IPlayer>>()

    /**
     * Players -> ships that they weren't watching before
     *
     * This gets cleared by [ShipObjectServerWorld] every tick
     */
    private val playersToShipsNewlyWatchingMap =
        HashMap<IPlayer, MutableSet<ServerShipInternal>>()

    /**
     * Players -> ships that they are no longer watching
     *
     * This gets cleared by [ShipObjectServerWorld] every tick
     */
    private val playersToShipsNoLongerWatchingMap =
        HashMap<IPlayer, MutableSet<ServerShipInternal>>()

    private val shipsToLoad = HashSet<ServerShipInternal>()

    private val shipsToUnload = HashSet<ServerShipInternal>()

    private var newPlayers: Set<IPlayer> = setOf()

    private fun cleanDeletedShips(deletedShips: Iterable<ServerShipInternal>) {
        for (ship in deletedShips) {
            playersToShipsWatchingMap.values.forEach { it.remove(ship) }
            shipsToPlayersWatchingMap.remove(ship.id)
            shipsToUnload.add(ship)
        }
    }

    private fun resetForNewTick() {
        shipsToLoad.clear()
        shipsToUnload.clear()
        playersToShipsNewlyWatchingMap.clear()
        playersToShipsNoLongerWatchingMap.clear()
    }

    /**
     * Analyzes player and ship positions according to the load distances specified in config and generates a list
     * of *suggestions* for chunks to be watched. The game may or may not actually watch all these chunks.
     *
     * This untracks ships from all removed players, however the chunk tracker must also be updated by
     * [applyTasksAndGenerateTrackingInfo]
     */
    fun generateChunkWatchTasksAndUpdatePlayers(
        players: Set<IPlayer>, lastTickPlayers: Set<IPlayer>,
        ships: Iterable<ServerShipInternal>,
        deletedShips: Iterable<ServerShipInternal>
    ): ChunkWatchTasks {
        resetForNewTick()
        cleanDeletedShips(deletedShips)
        // Remove players that left the world
        removePlayers(lastTickPlayers - players)
        newPlayers = players - lastTickPlayers

        val newChunkWatchTasks = TreeSet<ChunkWatchTask>()
        val newChunkUnwatchTasks = TreeSet<ChunkUnwatchTask>()

        val chunkWatchDistance = config.shipLoadDistance
        val chunkUnwatchDistance = config.shipUnloadDistance

        // Reuse these vector objects across iterations
        val tempVector = Vector3d()
        val tempAABB = AABBd()

        ships.forEach { shipData ->
            val shipTransform = shipData.transform
            val voxelAABB = shipData.shipAABB
            val worldHeight = dimensionInfoMap.getValue(shipData.chunkClaimDimension).yRange
            shipData.activeChunksSet.forEach { chunkX, chunkZ ->
                val chunkAABBInWorld = tempAABB
                    .set(
                        (chunkX shl 4).toDouble(),
                        // start at the minY of the ship if available
                        voxelAABB?.minY()?.toDouble() ?: worldHeight.minY.toDouble(),
                        (chunkZ shl 4).toDouble(),
                        (chunkX shl 4).toDouble() + 16.0,
                        // end at the maxY of the ship if available
                        (voxelAABB?.maxY()?.toDouble() ?: worldHeight.maxY.toDouble()) + 1.0,
                        (chunkZ shl 4).toDouble() + 16.0
                    )
                    .transform(shipTransform.shipToWorld)

                val newPlayersWatching: MutableList<IPlayer> = ArrayList()
                val newPlayersUnwatching: MutableList<IPlayer> = ArrayList()

                var minWatchingDistance = Double.MAX_VALUE
                var minUnwatchingDistance = Double.MAX_VALUE

                val playersWatchingChunk = getPlayersWatchingChunk(chunkX, chunkZ, shipData.chunkClaimDimension)
                val chunkPosAsLong = IShipActiveChunksSet.chunkPosToLong(chunkX, chunkZ)

                for (player in players) {
                    val playerPositionInWorldCoordinates: Vector3dc = player.getPosition(tempVector)
                    val displacementDistance =
                        chunkAABBInWorld.signedDistanceTo(playerPositionInWorldCoordinates)

                    val isPlayerWatchingThisChunk = playersWatchingChunk.contains(player)

                    if (shipData.chunkClaimDimension != player.dimension) {
                        if (isPlayerWatchingThisChunk) {
                            newPlayersUnwatching.add(player)
                        }
                    } else {
                        if (displacementDistance < chunkWatchDistance) {
                            if (!isPlayerWatchingThisChunk) {
                                // Watch this chunk
                                newPlayersWatching.add(player)
                                // Update [minWatchingDistanceSq]
                                minWatchingDistance = min(minWatchingDistance, displacementDistance)
                            }
                        } else if (displacementDistance > chunkUnwatchDistance) {
                            if (isPlayerWatchingThisChunk) {
                                // Unwatch this chunk
                                newPlayersUnwatching.add(player)
                                // Update [minUnwatchingDistanceSq]
                                minUnwatchingDistance = min(minUnwatchingDistance, displacementDistance)
                            }
                        }
                    }
                }

                // TODO distanceSqToClosestPlayer is for the watch and unwatch tasks incorrect
                // ( doesn't matter as long as we still do all of the watching in one tick though )
                if (newPlayersWatching.isNotEmpty()) {
                    val newChunkWatchTask = ChunkWatchTask(
                        chunkPosAsLong, shipData.chunkClaimDimension, newPlayersWatching, minWatchingDistance,
                        shipData
                    )
                    newChunkWatchTasks.add(newChunkWatchTask)
                }
                if (newPlayersUnwatching.isNotEmpty()) {
                    // If the all the currently watching players have unwatched, we should unload this chunk
                    val shouldUnloadChunk = playersWatchingChunk.size == newPlayersUnwatching.size
                    val newChunkUnwatchTask = ChunkUnwatchTask(
                        chunkPosAsLong, shipData.chunkClaimDimension,
                        newPlayersUnwatching, shouldUnloadChunk, minUnwatchingDistance, shipData
                    )
                    newChunkUnwatchTasks.add(newChunkUnwatchTask)
                }
            }
        }

        return ChunkWatchTasks(newChunkWatchTasks, newChunkUnwatchTasks)
    }

    /**
     * Updates the chunk trackers state and generates [ChunkTrackingInfo].
     *
     * [ChunkTrackingInfo] is only safe to be used for this tick
     */
    fun applyTasksAndGenerateTrackingInfo(
        executedWatchTasks: Iterable<ChunkWatchTask>,
        executedUnwatchTasks: Iterable<ChunkUnwatchTask>
    ): ChunkTrackingInfo {
        for (task in executedWatchTasks) {
            addWatchersToChunk(
                task.ship as ServerShipInternal, task.chunkPos,
                task.playersNeedWatching
            )
        }

        for (task in executedUnwatchTasks) {
            removeWatchersFromChunk(
                task.ship as ServerShipInternal, task.chunkPos,
                task.playersNeedUnwatching
            )
        }

        return ChunkTrackingInfo(
            playersToShipsWatchingMap,
            shipsToPlayersWatchingMap,
            playersToShipsNewlyWatchingMap,
            playersToShipsNoLongerWatchingMap,
            shipsToLoad,
            shipsToUnload,
            newPlayers
        )
    }

    // note dimensionId intentionally ignored for now
    @Suppress("UNUSED_PARAMETER")
    fun getPlayersWatchingChunk(chunkX: Int, chunkZ: Int, dimensionId: DimensionId): Collection<IPlayer> {
        val chunkPosAsLong = IShipActiveChunksSet.chunkPosToLong(chunkX, chunkZ)
        return chunkToPlayersWatchingMap[chunkPosAsLong] ?: listOf()
    }

    private fun addWatchersToChunk(
        shipData: ServerShipInternal, chunkPos: Long,
        newWatchingPlayers: Iterable<IPlayer>
    ) {
        chunkToPlayersWatchingMap
            .computeIfAbsent(chunkPos) { HashSet() }
            .addAll(newWatchingPlayers)

        newWatchingPlayers.forEach { player ->
            playersToShipsWatchingMap
                .computeIfAbsent(player) { HashMap() }
                .compute(shipData) { _, chunks ->
                    if (chunks == null) {
                        // This ship was not already tracked by this [player]
                        playersToShipsNewlyWatchingMap
                            .computeIfAbsent(player) { HashSet() }
                            .add(shipData)

                        val playersWatchingShip = shipsToPlayersWatchingMap
                            .computeIfAbsent(shipData.id, LongFunction { HashSet() })

                        // If no players were watching this ship before, load it
                        if (playersWatchingShip.isEmpty()) {
                            shipsToLoad.add(shipData)
                        }

                        playersWatchingShip.add(player)

                        // create a new set with this chunk in it that the player is now watching
                        LongOpenHashSet().apply { add(chunkPos) }
                    } else {
                        // other chunks on this ship were already tracked by this [player], add the chunk to the set
                        chunks.add(chunkPos)
                        chunks
                    }
                }
        }
    }

    private fun removeWatchersFromChunk(
        shipData: ServerShipInternal, chunkPos: Long,
        removedWatchingPlayers: Iterable<IPlayer>
    ) {
        // If there are players watching this chunk, remove the removedWatchingPlayers
        chunkToPlayersWatchingMap.computeIfPresent(chunkPos) { _, playersWatchingChunk ->
            playersWatchingChunk.removeAll(removedWatchingPlayers)
            // delete the entry in the chunkToPlayersWatchingMap if playersWatchingChunk is now empty
            playersWatchingChunk.ifEmpty { null }
        }

        removedWatchingPlayers.forEach { player ->
            playersToShipsWatchingMap.computeIfPresent(player) { _, shipsWatchingMap ->
                shipsWatchingMap.computeIfPresent(shipData) { _, chunks ->
                    if (chunks.size == 1) {
                        check(chunks.contains(chunkPos)) { "Last chunk on ship was not what we expected it to be" }
                        // This was the last chunk on the ship that [player] was tracking
                        playersToShipsNoLongerWatchingMap
                            .computeIfAbsent(player) { HashSet() }
                            .add(shipData)

                        val playersWatchingShip = shipsToPlayersWatchingMap.get(shipData.id)!!
                        playersWatchingShip.remove(player)

                        // If no players are watching this ship anymore, unload it
                        if (playersWatchingShip.isEmpty()) {
                            shipsToUnload.add(shipData)
                        }

                        chunks.clear()
                        null // return null to remove the hashmap entry for this [shipId]
                    } else {
                        // There are remaining chunks on the ship that [player] is tracking, just remove this one
                        check(chunks.remove(chunkPos)) { "Player not watching chunk that we thought they were" }
                        chunks
                    }
                }

                // if the player is no longer watching any ships, remove the hashmap entry for this [player]
                if (shipsWatchingMap.isEmpty()) null else shipsWatchingMap
            }
        }
    }

    /**
     * Remove players that are no longer in the [ShipObjectServerWorld]
     */
    private fun removePlayers(removedPlayers: Set<IPlayer>) {
        if (removedPlayers.isEmpty()) return
        removedPlayers.forEach { player ->
            playersToShipsWatchingMap.remove(player)
        }
        shipsToPlayersWatchingMap.forEach { (_, playersWatching) ->
            playersWatching.removeAll(removedPlayers)
        }
        chunkToPlayersWatchingMap.forEach { (_, playersWatching) ->
            playersWatching.removeAll(removedPlayers)
        }
    }
}
