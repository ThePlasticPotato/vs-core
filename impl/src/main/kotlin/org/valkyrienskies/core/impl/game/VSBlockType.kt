package org.valkyrienskies.core.impl.game

import org.valkyrienskies.core.apigame.world.chunks.BlockType
import org.valkyrienskies.physics_api.voxel_updates.KrunchVoxelStates

data class BlockTypeImpl(val state: Byte) : BlockType {
    @Deprecated("", ReplaceWith("state"))
    override fun toByte(): Byte = state

    companion object {
        val AIR: BlockType = BlockTypeImpl(KrunchVoxelStates.AIR_STATE)
        val SOLID: BlockType = BlockTypeImpl(KrunchVoxelStates.SOLID_STATE)
        val WATER: BlockType = BlockTypeImpl(KrunchVoxelStates.WATER_STATE)
        val LAVA: BlockType = BlockTypeImpl(KrunchVoxelStates.LAVA_STATE)
    }
}

