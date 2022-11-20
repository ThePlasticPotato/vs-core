package org.valkyrienskies.core.api.ships

import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.api.ships.properties.ShipId

interface QueryableShipData<out ShipType : Ship> : Iterable<ShipType> {
    @Deprecated(message = "Use the specific functions instead, such as #getById or #iterator")
    val idToShipData: Map<ShipId, ShipType>

    override fun iterator(): Iterator<ShipType>
    fun getById(shipId: ShipId): ShipType?
    fun getShipDataFromChunkPos(chunkX: Int, chunkZ: Int, dimensionId: DimensionId): ShipType?
    fun getShipDataIntersecting(aabb: AABBdc): Iterable<ShipType>

    fun contains(shipId: ShipId): Boolean = getById(shipId) != null
}

