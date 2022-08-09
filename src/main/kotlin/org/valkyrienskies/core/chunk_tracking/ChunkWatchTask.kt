package org.valkyrienskies.core.chunk_tracking

import org.valkyrienskies.core.game.DimensionId
import org.valkyrienskies.core.game.IPlayer
import org.valkyrienskies.core.game.ships.ShipData

/**
 * This task says that [playersNeedWatching] should be watching the chunk at [chunkPos].
 */
class ChunkWatchTask(
    val chunkPos: Long,
    val dimensionId: DimensionId,
    val playersNeedWatching: Iterable<IPlayer>,
    private val distanceToClosestPlayer: Double,
    val ship: ShipData
) : Comparable<ChunkWatchTask> {

    fun getChunkX(): Int = IShipActiveChunksSet.longToChunkX(chunkPos)
    fun getChunkZ(): Int = IShipActiveChunksSet.longToChunkZ(chunkPos)

    override fun compareTo(other: ChunkWatchTask): Int {
        return distanceToClosestPlayer.compareTo(other.distanceToClosestPlayer)
    }
}
