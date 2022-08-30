package org.valkyrienskies.core.ecs.tracking

import org.valkyrienskies.core.datastructures.ChunkClaimMap2
import org.valkyrienskies.core.ecs.VSWorld
import org.valkyrienskies.core.ecs.Ventity
import org.valkyrienskies.core.ecs.components.ChunkClaimComponent
import org.valkyrienskies.core.ecs.listenOnAdd
import org.valkyrienskies.core.ecs.listenOnRemove
import org.valkyrienskies.core.game.DimensionId

// TODO DAGGER
class VentityChunkTracker(
    val world: VSWorld
) : Iterable<Ventity> {
    /**
     * Chunk claims are shared over all dimensions, this is so that we don't have to change the chunk claim when we move
     * a ship between dimensions.
     */
    private val chunkClaimToShipData: ChunkClaimMap2<Ventity> = ChunkClaimMap2()

    init {
        ChunkClaimComponent::class.listenOnAdd { ve, it ->
            chunkClaimToShipData[it] = ve
        }

        ChunkClaimComponent::class.listenOnRemove { ve, it ->
            chunkClaimToShipData.remove(it)
        }
    }

    override fun iterator(): MutableIterator<Ventity> =
        world.findOwnersOf(ChunkClaimComponent::class)

    fun getVentityFromPos(chunkX: Int, chunkZ: Int, dimensionId: DimensionId): Ventity? {
        return chunkClaimToShipData[chunkX, chunkZ, dimensionId] /*if (ship != null && ship.chunkClaimDimension == dimensionId) {
            // Only return [shipData] if [shipData.chunkClaimDimension] is the same as [dimensionId]
            ship
        } else {
            // [shipData] is null, or has a different dimension
            null
        }*/
    }

    /* // TODO see todo below and add method
    fun getShipDataIntersecting(aabb: AABBdc): Iterable<ShipType> {
        // TODO Use https://github.com/tzaeschke/phtree
        return _idToShipData.values.filter { it.shipAABB.intersectsAABB(aabb) }
    }
    */
}
