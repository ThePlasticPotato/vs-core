package org.valkyrienskies.core.apigame.world.chunks

import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.apigame.world.IPlayer
import org.valkyrienskies.core.apigame.world.properties.DimensionId

interface ChunkWatchTask : Comparable<ChunkWatchTask> {
    val chunkPos: Long
    val dimensionId: DimensionId
    val playersNeedWatching: Iterable<IPlayer>
    val ship: ServerShip
    val chunkX: Int
    val chunkZ: Int
}
