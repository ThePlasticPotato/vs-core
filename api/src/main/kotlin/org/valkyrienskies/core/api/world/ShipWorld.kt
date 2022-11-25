package org.valkyrienskies.core.api.world

import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.LoadedShip
import org.valkyrienskies.core.api.ships.QueryableShipData
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.core.api.world.properties.DimensionId

interface ShipWorld {

    val allShips: QueryableShipData<Ship>
    val loadedShips: QueryableShipData<LoadedShip>

    fun isChunkInShipyard(chunkX: Int, chunkZ: Int, dimensionId: DimensionId): Boolean

    fun isBlockInShipyard(blockX: Int, blockY: Int, blockZ: Int, dimensionId: DimensionId): Boolean

    @Deprecated("redundant", ReplaceWith("loadedShips.getIntersecting(aabb)"))
    fun getShipObjectsIntersecting(aabb: AABBdc): List<LoadedShip>

    @Deprecated("renamed", ReplaceWith("allShips"))
    val queryableShipData: QueryableShipData<Ship> get() = allShips
}
