package org.valkyrienskies.core.impl.game

import org.valkyrienskies.core.api.world.properties.DimensionId

interface ChunkAllocatorProvider {

    fun forDimension(dimensionId: DimensionId): ChunkAllocator

}
