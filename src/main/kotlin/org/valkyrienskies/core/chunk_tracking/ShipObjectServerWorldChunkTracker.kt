package org.valkyrienskies.core.chunk_tracking

import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.valkyrienskies.core.config.VSCoreConfig
import org.valkyrienskies.core.game.DimensionId
import org.valkyrienskies.core.game.IPlayer
import org.valkyrienskies.core.game.ships.ShipData
import org.valkyrienskies.core.game.ships.ShipObjectServerWorld
import org.valkyrienskies.core.util.set
import org.valkyrienskies.core.util.signedDistanceTo
import java.util.TreeSet
import java.util.function.LongFunction
import javax.inject.Inject
import kotlin.math.min

internal class ShipObjectServerWorldChunkTracker @Inject constructor(
    val config: VSCoreConfig.Server
) {

    private val chunkToPlayersWatchingMap: Long2ObjectMap<MutableSet<IPlayer>> = Long2ObjectOpenHashMap()

    /**
     * Player -> Ship Id -> Number of chunks on that ship the player is tracking
     */
    private val playersToShipsWatchingMap = HashMap<IPlayer, Object2IntMap<ShipData>>()

    /**
     * Ship Id -> Players watching
     */
    private val shipsToPlayersWatchingMap = Long2ObjectOpenHashMap<MutableSet<IPlayer>>()

    /**
     * Players -> ships that they weren't watching before
     *
     * This gets cleared by [ShipObjectServerWorld] every tick
     */
    private val playersToShipsNewlyWatchingMap = HashMap<IPlayer, MutableSet<ShipData>>()

    /**
     * Players -> ships that they are no longer watching
     *
     * This gets cleared by [ShipObjectServerWorld] every tick
     */
    private val playersToShipsNoLongerWatchingMap = HashMap<IPlayer, MutableSet<ShipData>>()

    private val shipsToLoad = HashSet<ShipData>()

    private val shipsToUnload = HashSet<ShipData>()

    private fun cleanDeletedShips(deletedShips: Iterable<ShipData>) {
        for (ship in deletedShips) {
            playersToShipsWatchingMap.values.forEach { it.removeInt(ship) }
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
        players: Set<IPlayer>, lastTickPlayers: Set<IPlayer>, ships: Iterable<ShipData>,
        deletedShips: Iterable<ShipData>
    ): ChunkWatchTasks {
        resetForNewTick()
        cleanDeletedShips(deletedShips)
        // Remove players that left the world
        removePlayers(lastTickPlayers - players)

        val newChunkWatchTasks = TreeSet<ChunkWatchTask>()
        val newChunkUnwatchTasks = TreeSet<ChunkUnwatchTask>()

        val chunkWatchDistance = config.shipLoadDistance
        val chunkUnwatchDistance = config.shipUnloadDistance

        // Reuse these vector objects across iterations
        val tempVector = Vector3d()
        val tempAABB = AABBd()

        ships.forEach { shipData ->
            val shipTransform = shipData.shipTransform
            val voxelAABB = shipData.shipVoxelAABB
            shipData.shipActiveChunksSet.iterateChunkPos { chunkX, chunkZ ->
                val chunkAABBInWorld = tempAABB
                    .set(
                        (chunkX shl 4).toDouble(),
                        voxelAABB?.minY()?.toDouble() ?: 0.0, // start at the minY of the ship if available
                        (chunkZ shl 4).toDouble(),
                        (chunkX shl 4).toDouble() + 16.0,
                        (voxelAABB?.maxY()?.toDouble() ?: 255.0) + 1.0, // end at the maxY of the ship if available
                        (chunkZ shl 4).toDouble() + 16.0
                    )
                    .transform(shipTransform.shipToWorldMatrix)

                val newPlayersWatching: MutableList<IPlayer> = ArrayList()
                val newPlayersUnwatching: MutableList<IPlayer> = ArrayList()

                var minWatchingDistance = Double.MAX_VALUE
                var minUnwatchingDistance = Double.MAX_VALUE

                val playersWatchingChunk = getPlayersWatchingChunk(chunkX, chunkZ, shipData.chunkClaimDimension)

                for (player in players) {
                    val playerPositionInWorldCoordinates: Vector3dc = player.getPosition(tempVector)
                    val displacementDistance =
                        chunkAABBInWorld.signedDistanceTo(playerPositionInWorldCoordinates)

                    val isPlayerWatchingThisChunk = playersWatchingChunk.contains(player)

                    if (shipData.chunkClaimDimension != player.dimension) {
                        if (isPlayerWatchingThisChunk) {
                            // if the chunk dimension is different from the player dimension,
                            // lets just unwatch it for now
                            newPlayersUnwatching.add(player)
                        }

                        continue
                    }

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

                val chunkPosAsLong = IShipActiveChunksSet.chunkPosToLong(chunkX, chunkZ)
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
     * Updates the chunk trackers internal state and generates [ChunkTrackingInfo].
     *
     * [ChunkTrackingInfo] is only safe to be used for this tick
     */
    fun applyTasksAndGenerateTrackingInfo(
        executedWatchTasks: Iterable<ChunkWatchTask>,
        executedUnwatchTasks: Iterable<ChunkUnwatchTask>
    ): ChunkTrackingInfo {
        for (task in executedWatchTasks) {
            addWatchersToChunk(task.ship, task.chunkPos, task.playersNeedWatching)
        }

        for (task in executedUnwatchTasks) {
            removeWatchersFromChunk(task.ship, task.chunkPos, task.playersNeedUnwatching)
        }

        return ChunkTrackingInfo(
            playersToShipsWatchingMap,
            shipsToPlayersWatchingMap,
            playersToShipsNewlyWatchingMap,
            playersToShipsNoLongerWatchingMap,
            shipsToLoad,
            shipsToUnload
        )
    }

    // note dimensionId intentionally ignored for now
    @Suppress("UNUSED_PARAMETER")
    fun getPlayersWatchingChunk(chunkX: Int, chunkZ: Int, dimensionId: DimensionId): Collection<IPlayer> {
        val chunkPosAsLong = IShipActiveChunksSet.chunkPosToLong(chunkX, chunkZ)
        return chunkToPlayersWatchingMap[chunkPosAsLong] ?: listOf()
    }

    private fun addWatchersToChunk(shipData: ShipData, chunkPos: Long, newWatchingPlayers: Iterable<IPlayer>) {
        chunkToPlayersWatchingMap
            .computeIfAbsent(chunkPos) { HashSet() }
            .addAll(newWatchingPlayers)

        newWatchingPlayers.forEach { player ->
            playersToShipsWatchingMap
                .computeIfAbsent(player) { Object2IntOpenHashMap() }
                .compute(shipData) { _, count ->
                    if (count == null) {
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

                        // return 1 (the player is now watching one chunk on this ship)
                        1
                    } else {
                        // other chunks on this ship were already tracked by this [player], increase the count by one
                        count + 1
                    }
                }
        }
    }

    private fun removeWatchersFromChunk(
        shipData: ShipData, chunkPos: Long, removedWatchingPlayers: Iterable<IPlayer>
    ) {
        // If there are players watching this chunk, remove the removedWatchingPlayers
        chunkToPlayersWatchingMap.computeIfPresent(chunkPos) { _, playersWatchingChunk ->
            playersWatchingChunk.removeAll(removedWatchingPlayers)
            // delete the entry in the chunkToPlayersWatchingMap if playersWatchingChunk is now empty
            playersWatchingChunk.ifEmpty { null }
        }

        removedWatchingPlayers.forEach { player ->
            playersToShipsWatchingMap.computeIfPresent(player) { _, shipsWatchingMap ->
                shipsWatchingMap.computeIfPresent(shipData) { _, count ->
                    if (count == 1) {
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

                        null // return null to remove the hashmap entry for this [shipId]
                    } else {
                        // There are remaining chunks on the ship that [player] is tracking
                        count - 1
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
