package org.valkyrienskies.core.impl.chunk_tracking

import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.apigame.world.IPlayer
import org.valkyrienskies.core.apigame.world.chunks.ChunkWatchTask


fun ChunkWatchTask(
    chunkPos: Long,
    dimensionId: DimensionId,
    playersNeedWatching: Iterable<IPlayer>,
    distanceToClosestPlayer: Double,
    ship: ServerShip
) = ChunkWatchTaskImpl(chunkPos, dimensionId, playersNeedWatching, distanceToClosestPlayer, ship)

/**
 * This task says that [playersNeedWatching] should be watching the chunk at [chunkPos].
 */
class ChunkWatchTaskImpl(
    override val chunkPos: Long,
    override val dimensionId: DimensionId,
    override val playersNeedWatching: Iterable<IPlayer>,
    private val distanceToClosestPlayer: Double,
    override val ship: ServerShip
) : Comparable<ChunkWatchTask>, ChunkWatchTask {

    override val chunkX get() = IShipActiveChunksSet.longToChunkX(chunkPos)
    override val chunkZ get() = IShipActiveChunksSet.longToChunkZ(chunkPos)

    override fun compareTo(other: ChunkWatchTask): Int {
        other as ChunkWatchTaskImpl
        return distanceToClosestPlayer.compareTo(other.distanceToClosestPlayer)
    }
}
