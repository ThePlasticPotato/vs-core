package org.valkyrienskies.core.chunk_tracking

import org.valkyrienskies.core.api.world.chunks.ChunkWatchTask
import org.valkyrienskies.core.api.world.IPlayer
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.game.ships.ShipData


fun ChunkWatchTask(
    chunkPos: Long,
    dimensionId: DimensionId,
    playersNeedWatching: Iterable<IPlayer>,
    distanceToClosestPlayer: Double,
    ship: ShipData
) = ChunkWatchTaskImpl(chunkPos, dimensionId, playersNeedWatching, distanceToClosestPlayer, ship)

/**
 * This task says that [playersNeedWatching] should be watching the chunk at [chunkPos].
 */
class ChunkWatchTaskImpl(
    override val chunkPos: Long,
    override val dimensionId: DimensionId,
    override val playersNeedWatching: Iterable<IPlayer>,
    private val distanceToClosestPlayer: Double,
    override val ship: ShipData
) : Comparable<ChunkWatchTask>, ChunkWatchTask {

    override val chunkX get() = IShipActiveChunksSet.longToChunkX(chunkPos)
    override val chunkZ get() = IShipActiveChunksSet.longToChunkZ(chunkPos)

    override fun compareTo(other: ChunkWatchTask): Int {
        other as ChunkWatchTaskImpl
        return distanceToClosestPlayer.compareTo(other.distanceToClosestPlayer)
    }
}
