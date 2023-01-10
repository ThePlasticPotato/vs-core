package org.valkyrienskies.core.impl.datastructures.queryable.chunkclaim

import org.valkyrienskies.core.api.world.properties.DimensionId

interface MutableChunkClaimIndexed<T> : ChunkClaimIndexed<T> {

    fun add(claimX: Int, claimZ: Int, dimensionId: DimensionId, value: T)

    fun remove(claimX: Int, claimZ: Int, dimensionId: DimensionId): T

    fun update(
        oldClaimX: Int, oldClaimZ: Int, oldDimensionId: DimensionId,
        newClaimX: Int, newClaimZ: Int, newDimensionId: DimensionId
    )

}