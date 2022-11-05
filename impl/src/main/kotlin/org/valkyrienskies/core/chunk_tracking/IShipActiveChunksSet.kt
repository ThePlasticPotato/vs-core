package org.valkyrienskies.core.chunk_tracking

import org.joml.Vector3i
import kotlin.math.max
import kotlin.math.min

interface IShipActiveChunksSet {
    fun addChunkPos(chunkX: Int, chunkZ: Int): Boolean
    fun removeChunkPos(chunkX: Int, chunkZ: Int): Boolean
    fun containsChunkPos(chunkX: Int, chunkZ: Int): Boolean
    fun iterateChunkPos(func: (Int, Int) -> Unit)
    fun getTotalChunks(): Int
    fun getMinMaxWorldPos(minWorldPos: Vector3i, maxWorldPos: Vector3i) {
        if (getTotalChunks() == 0) {
            // Just set the ship to be undefined everywhere
            minWorldPos.set(Int.MAX_VALUE)
            maxWorldPos.set(Int.MIN_VALUE)
        }

        var minChunkX = Int.MAX_VALUE
        var minChunkZ = Int.MAX_VALUE
        var maxChunkX = Int.MIN_VALUE
        var maxChunkZ = Int.MIN_VALUE
        iterateChunkPos { chunkX, chunkZ ->
            minChunkX = min(minChunkX, chunkX)
            minChunkZ = min(minChunkZ, chunkZ)
            maxChunkX = max(maxChunkX, chunkX)
            maxChunkZ = max(maxChunkZ, chunkZ)
        }
        minWorldPos.set(minChunkX shl 4, 0, minChunkZ shl 4)
        maxWorldPos.set((maxChunkX shl 4) + 15, 255, (maxChunkZ shl 4) + 15)
    }

    companion object {
        fun chunkPosToLong(chunkX: Int, chunkZ: Int): Long {
            return (chunkX.toLong() shl 32) or chunkZ.toLong()
        }

        fun longToChunkX(chunkLong: Long): Int {
            return (chunkLong shr 32).toInt()
        }

        fun longToChunkZ(chunkLong: Long): Int {
            return (chunkLong and 0xFFFFFFFF).toInt()
        }
    }
}
