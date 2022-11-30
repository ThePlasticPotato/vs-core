package org.valkyrienskies.core.api.world

import org.valkyrienskies.core.api.world.chunks.BlockType
import org.valkyrienskies.core.api.world.properties.DimensionId

interface ShipWorldCore : ShipWorld {
    fun onSetBlock(
        posX: Int,
        posY: Int,
        posZ: Int,
        dimensionId: DimensionId,
        oldBlockType: BlockType,
        newBlockType: BlockType,
        oldBlockMass: Double,
        newBlockMass: Double
    )
}
