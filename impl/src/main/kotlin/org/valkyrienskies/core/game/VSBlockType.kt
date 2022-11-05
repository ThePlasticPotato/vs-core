package org.valkyrienskies.core.game

import org.valkyrienskies.physics_api.voxel_updates.KrunchVoxelStates

enum class VSBlockType {
    AIR, SOLID, WATER, LAVA;

    fun toByte() = when (this) {
        AIR -> KrunchVoxelStates.AIR_STATE
        SOLID -> KrunchVoxelStates.SOLID_STATE
        WATER -> KrunchVoxelStates.WATER_STATE
        LAVA -> KrunchVoxelStates.LAVA_STATE
    }
}
