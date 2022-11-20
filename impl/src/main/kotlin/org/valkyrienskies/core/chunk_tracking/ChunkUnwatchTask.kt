package org.valkyrienskies.core.chunk_tracking

import org.valkyrienskies.core.api.world.chunks.ChunkUnwatchTask
import org.valkyrienskies.core.api.world.IPlayer
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.game.ships.ShipData

fun ChunkUnwatchTask(
    chunkPos: Long,
    dimensionId: DimensionId,
    playersNeedUnwatching: Iterable<IPlayer>,
    shouldUnload: Boolean,
    distanceToClosestPlayer: Double,
    ship: ShipData
): ChunkUnwatchTask = ChunkUnwatchTaskImpl(chunkPos, dimensionId, playersNeedUnwatching, shouldUnload, distanceToClosestPlayer, ship)

/**
 * This task says that the chunk at [chunkPos] should no longer be watched by [playersNeedUnwatching].
 */
class ChunkUnwatchTaskImpl(
    override val chunkPos: Long,
    override val dimensionId: DimensionId,
    override val playersNeedUnwatching: Iterable<IPlayer>,
    override val shouldUnload: Boolean,
    private val distanceToClosestPlayer: Double,
    override val ship: ShipData
) : Comparable<ChunkUnwatchTask>, ChunkUnwatchTask {

    override val chunkX: Int get() = IShipActiveChunksSet.longToChunkX(chunkPos)
    override val chunkZ: Int get() = IShipActiveChunksSet.longToChunkZ(chunkPos)

    override fun compareTo(other: ChunkUnwatchTask): Int {
        other as ChunkUnwatchTaskImpl
        return distanceToClosestPlayer.compareTo(other.distanceToClosestPlayer)
    }
}
