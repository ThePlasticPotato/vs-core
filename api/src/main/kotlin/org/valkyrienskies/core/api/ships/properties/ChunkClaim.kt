package org.valkyrienskies.core.api.ships.properties

import org.joml.Vector3i
import org.joml.primitives.AABBi
import org.joml.primitives.AABBic

interface ChunkClaim {
    val xIndex: Int
    val zIndex: Int

    /**
     * x start (inclusive)
     */
    val xStart: Int

    /**
     * x end (inclusive)
     */
    val xEnd: Int

    /**
     * z start (inclusive)
     */
    val zStart: Int

    /**
     * z end (inclusive)
     */
    val zEnd: Int
    val xMiddle: Int
    val zMiddle: Int

    /**
     * The number of chunks owned by this claim
     */
    val size: Int
    fun toLong(): Long
    fun contains(x: Int, z: Int): Boolean
    fun getCenterBlockCoordinates(destination: Vector3i): Vector3i
    fun getBlockSize(destination: Vector3i): Vector3i

    /**
     * The region of all blocks contained in this [ChunkClaim].
     */
    fun getTotalVoxelRegion(destination: AABBi): AABBic

    companion object {
        /**
         * Every ship is given [DIAMETER] x [DIAMETER] chunks, hard-coded.
         */
        const val DIAMETER: Int = 256

        private const val BOTTOM_32_BITS_MASK: Long = 0xFFFFFFFFL

        fun getClaimXIndex(chunkX: Int) = Math.floorDiv(chunkX, DIAMETER)
        fun getClaimZIndex(chunkZ: Int) = Math.floorDiv(chunkZ, DIAMETER)

        fun claimToLong(claimXIndex: Int, claimZIndex: Int): Long {
            return ((claimXIndex.toLong() shl 32) or (claimZIndex.toLong() and BOTTOM_32_BITS_MASK))
        }

        fun getClaimThenToLong(chunkX: Int, chunkZ: Int): Long {
            // Compute the coordinates of the claim this chunk is in (not the same as chunk coordinates)
            val claimXIndex = getClaimXIndex(chunkX)
            val claimZIndex = getClaimZIndex(chunkZ)
            // Then convert
            return claimToLong(claimXIndex, claimZIndex)
        }
    }
}
