package org.valkyrienskies.core.apigame.world.chunks

import org.valkyrienskies.core.api.bodies.shape.VoxelUpdate

@Deprecated("moved to api", ReplaceWith("VoxelUpdate", "org.valkyrienskies.core.api.bodies.shape.VoxelUpdate"))
interface TerrainUpdate : VoxelUpdate {

    @Deprecated("moved to api", ReplaceWith("VoxelUpdater.Builder", "org.valkyrienskies.core.api.bodies.shape.VoxelUpdate"))
    interface Builder : VoxelUpdate.Builder
}