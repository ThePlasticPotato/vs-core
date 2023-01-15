package org.valkyrienskies.core.api.bodies.shape

interface VoxelUpdate  {

    val chunkX: Int
    val chunkY: Int
    val chunkZ: Int

    interface Builder {
        /**
         * Add a block to the terrain update.
         */
        fun addBlock(x: Int, y: Int, z: Int, block: VoxelType)
        fun build(): VoxelUpdate
    }
}
