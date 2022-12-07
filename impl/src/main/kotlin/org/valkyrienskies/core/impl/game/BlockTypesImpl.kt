package org.valkyrienskies.core.impl.game


import org.valkyrienskies.core.apigame.world.chunks.BlockType
import org.valkyrienskies.core.apigame.world.chunks.BlockTypes
import javax.inject.Inject

class BlockTypesImpl @Inject constructor() : BlockTypes {

    override val air: BlockType
        get() = BlockTypeImpl.AIR
    override val solid: BlockType
        get() = BlockTypeImpl.SOLID
    override val lava: BlockType
        get() = BlockTypeImpl.LAVA
    override val water: BlockType
        get() = BlockTypeImpl.WATER
}
