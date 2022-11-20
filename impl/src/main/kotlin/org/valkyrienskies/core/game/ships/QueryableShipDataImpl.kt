package org.valkyrienskies.core.game.ships

import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.MutableQueryableShipData
import org.valkyrienskies.core.api.ships.QueryableShipData
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.datastructures.ChunkClaimMap
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.util.intersectsAABB

typealias QueryableShipDataServer = QueryableShipData<ShipData>
typealias MutableQueryableShipDataServer = MutableQueryableShipData<ShipData>

open class QueryableShipDataImpl<ShipType : Ship>(
    data: Iterable<ShipType> = emptyList()
) : MutableQueryableShipData<ShipType> {

    private val _idToShipData: HashMap<ShipId, ShipType> = HashMap()

    @Deprecated("Use the specific functions instead, such as #getById or #iterator")
    override val idToShipData: Map<ShipId, ShipType> = _idToShipData

    /**
     * Chunk claims are shared over all dimensions, this is so that we don't have to change the chunk claim when we move
     * a ship between dimensions.
     */
    private val chunkClaimToShipData: ChunkClaimMap<ShipType> = ChunkClaimMap()

    init {
        data.forEach(::addShipData)
    }

    override fun iterator(): MutableIterator<ShipType> {
        val iter = _idToShipData.values.iterator()

        return object : MutableIterator<ShipType> {
            var last: ShipType? = null
            override fun hasNext(): Boolean = iter.hasNext()
            override fun next(): ShipType {
                val next = iter.next()
                last = next
                return next
            }

            override fun remove() {
                val lastCopy: ShipType = last ?: throw IllegalStateException("remove() failed because last was null!")
                iter.remove()
                chunkClaimToShipData.remove(lastCopy.chunkClaim)
                last = null
            }
        }
    }

    override fun addShipData(shipData: ShipType) {
        if (getById(shipData.id) != null) {
            throw IllegalArgumentException("Adding shipData $shipData failed because of duplicated UUID.")
        }
        _idToShipData[shipData.id] = shipData
        chunkClaimToShipData[shipData.chunkClaim] = shipData
    }

    override fun removeShipData(shipData: ShipType) {
        removeShipData(shipData.id)
    }

    override fun getById(shipId: ShipId): ShipType? {
        return _idToShipData[shipId]
    }

    override fun getShipDataFromChunkPos(chunkX: Int, chunkZ: Int, dimensionId: DimensionId): ShipType? {
        val shipData: ShipType? = chunkClaimToShipData[chunkX, chunkZ]
        return if (shipData != null && shipData.chunkClaimDimension == dimensionId) {
            // Only return [shipData] if [shipData.chunkClaimDimension] is the same as [dimensionId]
            shipData
        } else {
            // [shipData] is null, or has a different dimension
            null
        }
    }

    override fun removeShipData(id: ShipId) {
        val shipData = getById(id)
            ?: throw IllegalArgumentException("Removing ship id:$id failed because it wasn't in the UUID map.")
        _idToShipData.remove(shipData.id)
        chunkClaimToShipData.remove(shipData.chunkClaim)
    }

    override fun getShipDataIntersecting(aabb: AABBdc): Iterable<ShipType> {
        // TODO Use https://github.com/tzaeschke/phtree
        return _idToShipData.values.filter { it.shipAABB.intersectsAABB(aabb) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QueryableShipDataImpl<*>

        if (idToShipData != other.idToShipData) return false

        return true
    }

    override fun hashCode(): Int {
        return idToShipData.hashCode()
    }
}
