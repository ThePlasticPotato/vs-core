package org.valkyrienskies.core.impl.game.ships.types

import org.valkyrienskies.core.apigame.world.chunks.TerrainUpdate
import org.valkyrienskies.physics_api.voxel.updates.IVoxelShapeUpdate

data class VoxelUpdateImpl(
    val update: IVoxelShapeUpdate,
) : TerrainUpdate {
    override val chunkX: Int
        get() = update.regionX

    override val chunkY: Int
        get() = update.regionY

    override val chunkZ: Int
        get() = update.regionZ
}
