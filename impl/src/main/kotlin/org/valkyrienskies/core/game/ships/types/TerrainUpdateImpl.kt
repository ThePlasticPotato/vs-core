package org.valkyrienskies.core.game.ships.types

import org.valkyrienskies.core.api.world.chunks.TerrainUpdate
import org.valkyrienskies.physics_api.voxel_updates.IVoxelShapeUpdate

data class TerrainUpdateImpl(
    val update: IVoxelShapeUpdate,
): TerrainUpdate {
    override val chunkX: Int
        get() = update.regionX

    override val chunkY: Int
        get() = update.regionY

    override val chunkZ: Int
        get() = update.regionZ
}
