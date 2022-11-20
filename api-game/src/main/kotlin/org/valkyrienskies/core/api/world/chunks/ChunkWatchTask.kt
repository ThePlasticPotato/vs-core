package org.valkyrienskies.core.api.world.chunks

import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.world.IPlayer

interface ChunkWatchTask : Comparable<ChunkWatchTask> {
    val chunkPos: Long
    val dimensionId: DimensionId
    val playersNeedWatching: Iterable<IPlayer>
    val ship: ServerShip
    val chunkX: Int
    val chunkZ: Int
}
