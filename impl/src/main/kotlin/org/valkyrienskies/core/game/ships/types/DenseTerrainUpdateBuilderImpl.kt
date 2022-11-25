package org.valkyrienskies.core.game.ships.types

import org.valkyrienskies.core.api.world.chunks.BlockType
import org.valkyrienskies.core.api.world.chunks.TerrainUpdate
import org.valkyrienskies.core.game.BlockTypeImpl
import org.valkyrienskies.physics_api.voxel_updates.DenseVoxelShapeUpdate

class DenseTerrainUpdateBuilderImpl(
    chunkX: Int,
    chunkY: Int,
    chunkZ: Int
) : TerrainUpdate.Builder {
    private val update = DenseVoxelShapeUpdate(chunkX, chunkY, chunkZ)

    private var isBuilt = false

    override fun addBlock(x: Int, y: Int, z: Int, block: BlockType) {
        block as BlockTypeImpl

        update.setVoxel(x, y, z, block.state)
    }

    override fun build(): TerrainUpdate {
        require(!isBuilt) { "This builder has already been used!" }
        isBuilt = true

        return TerrainUpdateImpl(update)
    }
}
