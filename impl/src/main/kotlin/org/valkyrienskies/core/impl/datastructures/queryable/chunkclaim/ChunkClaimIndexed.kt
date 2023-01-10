package org.valkyrienskies.core.impl.datastructures.queryable.chunkclaim

import org.valkyrienskies.core.api.world.properties.DimensionId

interface ChunkClaimIndexed<T> {

    fun getByChunkPos(chunkX: Int, chunkZ: Int, dimensionId: DimensionId): T?

    fun getByChunkClaim(claimX: Int, claimZ: Int, dimensionId: DimensionId): T?

}