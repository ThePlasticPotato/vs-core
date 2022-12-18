package org.valkyrienskies.core.util.functions

import org.joml.Vector3i
import org.joml.Vector3ic
import org.valkyrienskies.core.util.prelude.*
import java.nio.ByteBuffer

/**
 * Take (x, y, z) and produce index (i)
 */
fun unwrapIndex(index: Int, dimensions: Vector3ic, v: Vector3i): Vector3i {
    return unwrapIndex(index, dimensions) { x, y, z ->
        v.set(x, y, z)
    }
}

inline fun <R> unwrapIndex(index: Int, dimensions: Vector3ic, out: (Int, Int, Int) -> R): R {
    val z = index / (dimensions.x * dimensions.y)
    val y = (index - (z * dimensions.x * dimensions.y)) / dimensions.x
    val x = (index - (z * dimensions.x * dimensions.y)) % dimensions.x

    return out(x, y, z)
}

fun wrapIndex(x: Int, y: Int, z: Int, dimensions: Vector3ic): Int =
    x + (y * dimensions.x) + (z * dimensions.x * dimensions.y)

fun wrapIndex(point: Vector3ic, dimensions: Vector3ic): Int =
    wrapIndex(point.x, point.y, point.z, dimensions)

inline fun Byte.iterateBits(func: (Boolean, Int) -> Unit) {
    for (i in 7 downTo 0) {
        val masked = (this.toInt() and (1 shl i))
        func(masked != 0, i)
    }
}

inline fun Int.iterateBits(func: (Boolean, Int) -> Unit) {
    for (i in 31 downTo 0) {
        val masked = this and (1 shl i)
        func(masked != 0, i)
    }
}

inline fun Long.iterateBits(func: (Boolean, Int) -> Unit) {
    for (i in 63 downTo 0) {
        val masked = this and (1L shl i)
        func(masked != 0L, i)
    }
}

/**
 * For example ByteBuffer is 01110 then this is called with
 * (false, 0), (true, 1), (true, 2), (true, 3), (false, 4)
 */
inline fun ByteBuffer.iterateBits(func: (Boolean, Int) -> Unit) {
    for (i in 0..this.capacity()) {
        val byte = this.get(i)
        byte.iterateBits { bit, j -> func(bit, i * 8 + j) }
    }
}
