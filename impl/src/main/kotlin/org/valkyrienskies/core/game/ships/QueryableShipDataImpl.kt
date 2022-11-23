package org.valkyrienskies.core.game.ships

import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.MutableQueryableShipData
import org.valkyrienskies.core.api.ships.QueryableShipData
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.datastructures.ChunkClaimMap
import org.valkyrienskies.core.util.intersectsAABB

typealias QueryableShipDataServer = QueryableShipData<ShipData>
typealias MutableQueryableShipDataServer = MutableQueryableShipData<ShipData>

open class QueryableShipDataImpl<ShipType : Ship>(
    data: Iterable<ShipType> = emptyList()
) : MutableQueryableShipData<ShipType> {

    private val _idToShipData: HashMap<ShipId, ShipType> = HashMap()

    @Deprecated("Use the specific functions instead, such as #getById or #iterator")
    override val idToShipData: Map<ShipId, ShipType> get() = _idToShipData

    /**
     * Chunk claims are shared over all dimensions, this is so that we don't have to change the chunk claim when we move
     * a ship between dimensions.
     */
    private val chunkClaimToShipData: ChunkClaimMap<ShipType> = ChunkClaimMap()

    init {
        data.forEach(::add)
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

    override fun add(ship: ShipType) {
        if (getById(ship.id) != null) {
            throw IllegalArgumentException("Adding ship id:${ship.id} failed because of duplicated ID.")
        }
        _idToShipData[ship.id] = ship
        chunkClaimToShipData[ship.chunkClaim] = ship
    }

    override fun remove(ship: ShipType) {
        remove(ship.id)
    }

    override fun getById(shipId: ShipId): ShipType? {
        return _idToShipData[shipId]
    }

    override fun getByChunkPos(chunkX: Int, chunkZ: Int, dimensionId: DimensionId): ShipType? {
        val shipData: ShipType? = chunkClaimToShipData[chunkX, chunkZ]
        return if (shipData != null && shipData.chunkClaimDimension == dimensionId) {
            // Only return [shipData] if [shipData.chunkClaimDimension] is the same as [dimensionId]
            shipData
        } else {
            // [shipData] is null, or has a different dimension
            null
        }
    }

    override fun remove(id: ShipId) {
        val shipData = getById(id)
            ?: throw IllegalArgumentException("Removing ship id:$id failed because it couldn't be found.")
        _idToShipData.remove(shipData.id)
        chunkClaimToShipData.remove(shipData.chunkClaim)
    }

    override val size: Int
        get() = _idToShipData.size

    override fun getIntersecting(aabb: AABBdc): Iterable<ShipType> {
        // TODO Use https://github.com/tzaeschke/phtree
        return _idToShipData.values.filter { it.shipAABB.intersectsAABB(aabb) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QueryableShipDataImpl<*>

        if (_idToShipData != other._idToShipData) return false

        return true
    }

    override fun hashCode(): Int {
        return _idToShipData.hashCode()
    }
}
