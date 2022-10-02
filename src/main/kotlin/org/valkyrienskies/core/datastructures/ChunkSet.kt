package org.valkyrienskies.core.datastructures

import org.joml.Vector3i
import org.joml.Vector3ic
import org.valkyrienskies.core.util.iterateBits
import org.valkyrienskies.core.util.unwrapIndex
import org.valkyrienskies.core.util.wrapIndex
import kotlin.experimental.and
import kotlin.experimental.or

class ChunkSet(
    val data: ByteArray = ByteArray(512)
) {
    companion object {
        @PublishedApi
        internal val dimensions: Vector3ic = Vector3i(16, 16, 16)
    }

    inline fun iterateSetBlocks(fn: (Int, Int, Int) -> Unit) {
        data.forEachIndexed { index, byte ->
            byte.iterateBits { isSet, bitIndex ->
                unwrapIndex(index * 8 + bitIndex, dimensions) { x, y, z ->
                    if (isSet) {
                        fn(x, y, z)
                    }
                }
            }
        }
    }

    fun unsetBlock(x: Int, y: Int, z: Int) {
        val index = wrapIndex(x, y, z, dimensions)
        val realIndex = index / 8
        val offset = index % 8

        data[realIndex] = data[realIndex] and ((1 shl offset).inv().toByte())
    }

    fun setBlock(x: Int, y: Int, z: Int) {
        val index = wrapIndex(x, y, z, dimensions)
        val realIndex = index / 8
        val offset = index % 8

        data[realIndex] = data[realIndex] or ((1 shl offset).toByte())
    }

    fun getBlock(x: Int, y: Int, z: Int): Boolean {
        val index = wrapIndex(x, y, z, dimensions)
        val realIndex = index / 8
        val offset = index % 8

        return (data[realIndex] and ((1 shl offset).toByte())) != 0.toByte()
    }
}

