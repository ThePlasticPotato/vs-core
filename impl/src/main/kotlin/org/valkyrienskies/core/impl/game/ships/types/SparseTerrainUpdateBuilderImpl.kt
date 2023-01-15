package org.valkyrienskies.core.impl.game.ships.types

import org.valkyrienskies.core.apigame.world.chunks.BlockType
import org.valkyrienskies.core.apigame.world.chunks.TerrainUpdate
import org.valkyrienskies.core.impl.game.BlockTypeImpl
import org.valkyrienskies.physics_api.voxel.updates.SparseVoxelShapeUpdate

class SparseTerrainUpdateBuilderImpl(
    chunkX: Int,
    chunkY: Int,
    chunkZ: Int
) : TerrainUpdate.Builder {
    private val update = SparseVoxelShapeUpdate(chunkX, chunkY, chunkZ)

    private var isBuilt = false

    override fun addBlock(x: Int, y: Int, z: Int, block: BlockType) {
        block as BlockTypeImpl

        update.addUpdate(x, y, z, block.state)
    }

    override fun build(): TerrainUpdate {
        require(!isBuilt) { "This builder has already been used!" }
        isBuilt = true

        return VoxelUpdateImpl(update)
    }
}
