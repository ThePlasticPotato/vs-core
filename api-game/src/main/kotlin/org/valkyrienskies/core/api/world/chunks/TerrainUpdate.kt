package org.valkyrienskies.core.api.world.chunks

interface TerrainUpdate {

    val chunkX: Int
    val chunkY: Int
    val chunkZ: Int

    interface Builder {
        /**
         * Add a block to the terrain update.
         */
        fun addBlock(x: Int, y: Int, z: Int, block: BlockType)
        fun build(): TerrainUpdate
    }
}
