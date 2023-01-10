package org.valkyrienskies.core.impl.datastructures.queryable.chunkclaim

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.api.world.properties.DimensionId

/**
 * Maps [ChunkClaim]s to [T].
 *
 * The [get] function allows accessing the [T] that claims that chunk position (if there is one) in
 * O(1) time. It makes no objects so its very efficient.
 */
class HashChunkClaimIndex<T> : MutableChunkClaimIndexed<T> {

    private val map = HashMap<DimensionId, Long2ObjectOpenHashMap<T>>()

    override fun add(claimX: Int, claimZ: Int, dimensionId: DimensionId, value: T) {
        val claimMap = map.computeIfAbsent(dimensionId) { Long2ObjectOpenHashMap() }
        val claim = ChunkClaim.claimToLong(claimX, claimZ)

        require(claimMap.put(claim, value) == null) {
            "Tried to add object already in index (claimX: $claimX, claimZ: $claimZ)"
        }
    }
    override fun remove(claimX: Int, claimZ: Int, dimensionId: DimensionId): T {
        val claimMap = map.computeIfAbsent(dimensionId) { Long2ObjectOpenHashMap() }
        val claim = ChunkClaim.claimToLong(claimX, claimZ)

        return requireNotNull(claimMap.remove(claim)) {
            "Tried to remove object not in index (claimX: $claimX, claimZ: $claimZ)"
        }
    }

    override fun update(
        oldClaimX: Int,
        oldClaimZ: Int,
        oldDimensionId: DimensionId,
        newClaimX: Int,
        newClaimZ: Int,
        newDimensionId: DimensionId
    ) {
        require(contains(oldClaimX, oldClaimZ, oldDimensionId)) {
            "Tried to update object not index (" +
                "oldClaimX: $oldClaimX, oldClaimZ: $oldClaimZ, oldDimensionId: $oldDimensionId, " +
                "newClaimX: $newClaimX, newClaimZ: $newClaimZ, newDimensionId: $newDimensionId)"
        }
        require(!contains(newClaimX, newClaimZ, newDimensionId)) {
            "Tried to update object but new claim already in index (" +
                "oldClaimX: $oldClaimX, oldClaimZ: $oldClaimZ, oldDimensionId: $oldDimensionId, " +
                "newClaimX: $newClaimX, newClaimZ: $newClaimZ, newDimensionId: $newDimensionId)"
        }

        add(newClaimX, newClaimZ, newDimensionId, remove(oldClaimX, oldClaimZ, oldDimensionId))
    }

    private fun contains(claimX: Int, claimZ: Int, dimensionId: DimensionId) =
        getByChunkClaim(claimX, claimZ, dimensionId) != null

    override fun getByChunkPos(chunkX: Int, chunkZ: Int, dimensionId: DimensionId): T? {
        return getByChunkClaim(ChunkClaim.getClaimXIndex(chunkX), ChunkClaim.getClaimZIndex(chunkZ), dimensionId)
    }

    override fun getByChunkClaim(claimX: Int, claimZ: Int, dimensionId: DimensionId): T? {
        val claimMap = map.computeIfAbsent(dimensionId) { Long2ObjectOpenHashMap() }
        val claim = ChunkClaim.claimToLong(claimX, claimZ)

        return claimMap[claim]
    }
}
