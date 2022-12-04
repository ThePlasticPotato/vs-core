package org.valkyrienskies.core.apigame.world

import org.valkyrienskies.core.apigame.world.ShipWorld
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.apigame.world.chunks.BlockType

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
