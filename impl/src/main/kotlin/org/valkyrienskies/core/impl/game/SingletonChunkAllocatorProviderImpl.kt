package org.valkyrienskies.core.impl.game

import org.valkyrienskies.core.apigame.world.properties.DimensionId
import javax.inject.Inject
import javax.inject.Named

class SingletonChunkAllocatorProviderImpl @Inject constructor(
    @Named("primary") val allocator: ChunkAllocator
) : ChunkAllocatorProvider {
    override fun forDimension(dimensionId: DimensionId): ChunkAllocator = allocator
}
