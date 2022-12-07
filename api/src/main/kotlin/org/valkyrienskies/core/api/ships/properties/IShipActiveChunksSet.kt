package org.valkyrienskies.core.api.ships.properties

import org.joml.Vector3i
import org.valkyrienskies.core.api.util.functions.IntBinaryConsumer
import kotlin.math.max
import kotlin.math.min

interface IShipActiveChunksSet {
    val size: Int

    fun add(chunkX: Int, chunkZ: Int): Boolean

    fun remove(chunkX: Int, chunkZ: Int): Boolean

    fun contains(chunkX: Int, chunkZ: Int): Boolean

    fun forEach(func: IntBinaryConsumer)

    fun getMinMaxWorldPos(minWorldPos: Vector3i, maxWorldPos: Vector3i) {
        if (size == 0) {
            // Just set the ship to be undefined everywhere
            minWorldPos.set(Int.MAX_VALUE)
            maxWorldPos.set(Int.MIN_VALUE)
        }

        var minChunkX = Int.MAX_VALUE
        var minChunkZ = Int.MAX_VALUE
        var maxChunkX = Int.MIN_VALUE
        var maxChunkZ = Int.MIN_VALUE
        forEach { chunkX, chunkZ ->
            minChunkX = min(minChunkX, chunkX)
            minChunkZ = min(minChunkZ, chunkZ)
            maxChunkX = max(maxChunkX, chunkX)
            maxChunkZ = max(maxChunkZ, chunkZ)
        }
        minWorldPos.set(minChunkX shl 4, 0, minChunkZ shl 4)
        maxWorldPos.set((maxChunkX shl 4) + 15, 255, (maxChunkZ shl 4) + 15)
    }

    @Deprecated("renamed", ReplaceWith("remove(chunkX, chunkZ)"))
    fun removeChunkPos(chunkX: Int, chunkZ: Int): Boolean = remove(chunkX, chunkZ)

    @Deprecated("renamed", ReplaceWith("size"))
    fun getTotalChunks(): Int = size

    @Deprecated(
        "Uses boxed integers as parameters and requires Unit return type, bad for performance and Java interop",
        ReplaceWith("forEach(func)")
    )
    fun iterateChunkPos(func: (Int, Int) -> Unit)

    @Deprecated("renamed", ReplaceWith("contains(chunkX, chunkZ)"))
    fun containsChunkPos(chunkX: Int, chunkZ: Int): Boolean = contains(chunkX, chunkZ)

    @Deprecated("renamed", ReplaceWith("add(chunkX, chunkZ)"))
    fun addChunkPos(chunkX: Int, chunkZ: Int): Boolean = add(chunkX, chunkZ)

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
