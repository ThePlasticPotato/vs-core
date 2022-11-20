package org.valkyrienskies.core.game

import org.valkyrienskies.core.api.ships.properties.VSBlockType
import org.valkyrienskies.physics_api.voxel_updates.KrunchVoxelStates

data class VSBlockTypeImpl(val state: Byte) : VSBlockType {
    @Deprecated("", ReplaceWith("state"))
    override fun toByte(): Byte = state

    companion object {
        val AIR: VSBlockType = VSBlockTypeImpl(KrunchVoxelStates.AIR_STATE)
        val SOLID: VSBlockType = VSBlockTypeImpl(KrunchVoxelStates.SOLID_STATE)
        val WATER: VSBlockType = VSBlockTypeImpl(KrunchVoxelStates.WATER_STATE)
        val LAVA: VSBlockType = VSBlockTypeImpl(KrunchVoxelStates.LAVA_STATE)
    }
}

