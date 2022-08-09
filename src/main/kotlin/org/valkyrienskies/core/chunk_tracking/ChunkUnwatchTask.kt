package org.valkyrienskies.core.chunk_tracking

import org.valkyrienskies.core.game.DimensionId
import org.valkyrienskies.core.game.IPlayer
import org.valkyrienskies.core.game.ships.ShipData

/**
 * This task says that the chunk at [chunkPos] should no longer be watched by [playersNeedUnwatching].
 */
class ChunkUnwatchTask(
    val chunkPos: Long,
    val dimensionId: DimensionId,
    val playersNeedUnwatching: Iterable<IPlayer>,
    val shouldUnload: Boolean,
    private val distanceToClosestPlayer: Double,
    val ship: ShipData
) : Comparable<ChunkUnwatchTask> {

    fun getChunkX(): Int = IShipActiveChunksSet.longToChunkX(chunkPos)
    fun getChunkZ(): Int = IShipActiveChunksSet.longToChunkZ(chunkPos)

    override fun compareTo(other: ChunkUnwatchTask): Int {
        return distanceToClosestPlayer.compareTo(other.distanceToClosestPlayer)
    }
}
