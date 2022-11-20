package org.valkyrienskies.core.game.ships.types

import org.valkyrienskies.core.api.ships.properties.VSBlockType
import org.valkyrienskies.core.api.world.chunks.TerrainUpdate
import org.valkyrienskies.core.game.VSBlockTypeImpl
import org.valkyrienskies.physics_api.voxel_updates.DenseVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel_updates.SparseVoxelShapeUpdate

class SparseTerrainUpdateBuilderImpl(
    chunkX: Int,
    chunkY: Int,
    chunkZ: Int
) : TerrainUpdate.Builder {
    private val update = SparseVoxelShapeUpdate(chunkX, chunkY, chunkZ)

    private var isBuilt = false

    override fun addBlock(x: Int, y: Int, z: Int, block: VSBlockType) {
        block as VSBlockTypeImpl

        update.addUpdate(x, y, z, block.state)
    }

    override fun build(): TerrainUpdate {
        require(!isBuilt) { "This builder has already been used!" }
        isBuilt = true

        return TerrainUpdateImpl(update)
    }
}
