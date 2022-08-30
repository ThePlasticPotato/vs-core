package org.valkyrienskies.core.datastructures

import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import org.valkyrienskies.core.ecs.components.ChunkClaimComponent
import org.valkyrienskies.core.game.ChunkClaim
import org.valkyrienskies.core.game.DimensionId

/**
 * Maps [ChunkClaim]s to [T].
 *
 * The [get] function allows accessing the [T] that claims that chunk position (if there is one) in
 * O(1) time. It makes no objects so its very efficient.
 */
class ChunkClaimMap2<T> {

    private val backingMap: Long2ObjectMap<Pair<T, ChunkClaimComponent>> = Long2ObjectOpenHashMap()

    operator fun set(chunkClaim: ChunkClaimComponent, data: T) {
        val claimAsLong = chunkClaim.toLong()
        if (backingMap.containsKey(claimAsLong)) {
            // There is already data at this claim, throw exception
            throw IllegalArgumentException(
                "Tried adding $data at $chunkClaim, but a value already exists at $chunkClaim"
            )
        }
        backingMap.put(claimAsLong, Pair(data, chunkClaim))
    }

    fun remove(chunkClaim: ChunkClaimComponent) {
        val claimAsLong = chunkClaim.toLong()
        if (backingMap.remove(claimAsLong) == null) {
            // Throw exception if we didn't remove anything
            throw IllegalArgumentException(
                "Tried to remove data at $chunkClaim, but that claim wasn't in the chunk claim map!"
            )
        }
    }

    operator fun get(chunkClaim: ChunkClaimComponent): T? {
        return backingMap[chunkClaim.toLong()].first
    }

    operator fun get(chunkX: Int, chunkZ: Int, dimensionId: DimensionId): T? {
        val chunkPosToClaimAsLong = ChunkClaimComponent.getClaimThenToLong(chunkX, chunkZ)
        return backingMap[chunkPosToClaimAsLong]?.let { (data, claim) ->
            if (claim.dimension == dimensionId) data else null
        }
    }
}
