package org.valkyrienskies.core.util.datastructures.blockpos.set

import org.joml.Vector3i
import org.joml.Vector3ic

interface SingleChunkBlockPosSet {
    fun remove(x: Int, y: Int, z: Int): Boolean
    fun add(x: Int, y: Int, z: Int): Boolean
    fun contains(x: Int, y: Int, z: Int): Boolean

    companion object {
        val dimensions: Vector3ic = Vector3i(16, 16, 16)

        fun requireInBounds(x: Int, y: Int, z: Int) {
            require(isInBounds(x, y, z)) {
                "Block coordinates ($x, $y, $z) must be within the bounds of the chunk (0 <= x, y, z <= 15)"
            }
        }

        fun isInBounds(x: Int, y: Int, z: Int): Boolean {
            return x < dimensions.x() && x >= 0 && y < dimensions.y() && y >= 0 && z < dimensions.z() && z >= 0
        }
    }
}
