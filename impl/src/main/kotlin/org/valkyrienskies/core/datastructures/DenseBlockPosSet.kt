package org.valkyrienskies.core.datastructures

import org.joml.Vector3ic

class DenseBlockPosSet : IBlockPosSet {

    val chunks = BlockPos2ObjectOpenHashMap<SingleChunkDenseBlockPosSet>()
    override var size = 0
        private set

    fun getChunk(x: Int, y: Int, z: Int): SingleChunkDenseBlockPosSet? {
        return chunks.get(x shr 4, y shr 4, z shr 4)
    }

    override fun add(x: Int, y: Int, z: Int): Boolean {
        val prev = chunks.getOrPut(x shr 4, y shr 4, z shr 4) { SingleChunkDenseBlockPosSet() }
            .add(x and 15, y and 15, z and 15)

        if (!prev) {
            size++
        }

        return prev
    }

    override fun clear() {
        chunks.clear()
    }

    override fun iterator(): MutableIterator<Vector3ic> = throw UnsupportedOperationException()

    override fun remove(x: Int, y: Int, z: Int): Boolean {
        val prev = chunks.get(x shr 4, y shr 4, z shr 4)?.remove(x and 15, y and 15, z and 15) ?: false

        if (prev) {
            size--
        }

        return prev
    }

    override fun contains(x: Int, y: Int, z: Int): Boolean {
        return chunks.get(x shr 4, y shr 4, z shr 4)?.contains(x and 15, y and 15, z and 15) ?: false
    }

    override fun canStore(x: Int, y: Int, z: Int): Boolean {
        return true
    }

    inline fun forEachChunk(fn: (Int, Int, Int, SingleChunkDenseBlockPosSet) -> Unit) {
        chunks.forEach { x, y, z, chunk ->
            fn(x, y, z, chunk)
        }
    }

    inline fun forEach(fn: (Int, Int, Int) -> Unit) {
        chunks.forEach { chunkX, chunkY, chunkZ, chunk ->
            chunk.forEach { x, y, z ->
                fn(x + (chunkX shl 4), y + (chunkY shl 4), z + (chunkZ shl 4))
            }
        }
    }
}
