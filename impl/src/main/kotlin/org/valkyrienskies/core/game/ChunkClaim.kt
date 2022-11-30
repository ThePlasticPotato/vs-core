package org.valkyrienskies.core.game

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIncludeProperties
import org.joml.Vector3i
import org.joml.primitives.AABBi
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.api.ships.properties.ChunkClaim.Companion.DIAMETER
import org.valkyrienskies.core.api.ships.properties.ChunkClaim.Companion.claimToLong

/**
 * Each ChunkClaim claims all chunks between the coordinates
 * ([xIndex] * [DIAMETER], [zIndex] * [DIAMETER]) and ([xIndex] * [DIAMETER] + [DIAMETER] - 1, [zIndex] * [DIAMETER] + [DIAMETER] - 1)
 *
 * So for example, if [xIndex] is 5, [zIndex] is 10, and [DIAMETER] is 50, then this claim contains all chunks between
 * (250, 500) and (299, 549)
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY) // Don't use getters, they have a weird name
@JsonIncludeProperties("xIndex", "zIndex") // Serialize only the xIndex and zIndex fields
data class ChunkClaimImpl(override val xIndex: Int, override val zIndex: Int) : ChunkClaim {

    companion object {
        /**
         * Get the claim for a specific chunk
         */
        fun getClaim(chunkX: Int, chunkZ: Int): ChunkClaim =
            ChunkClaimImpl(ChunkClaim.getClaimXIndex(chunkX), ChunkClaim.getClaimZIndex(chunkZ))
    }

    /**
     * x start (inclusive)
     */
    override val xStart = xIndex * DIAMETER

    /**
     * x end (inclusive)
     */
    override val xEnd = (xIndex * DIAMETER) + DIAMETER - 1

    /**
     * z start (inclusive)
     */
    override val zStart = zIndex * DIAMETER

    /**
     * z end (inclusive)
     */
    override val zEnd = (zIndex * DIAMETER) + DIAMETER - 1

    override val xMiddle = xStart + DIAMETER / 2

    override val zMiddle = zStart + DIAMETER / 2

    /**
     * The number of chunks owned by this claim
     */
    override val size = (xEnd - xStart + 1) * (zEnd - zStart + 1)

    override fun toLong(): Long {
        return claimToLong(xIndex, zIndex)
    }

    override fun contains(x: Int, z: Int) =
        (x in xStart..xEnd) and (z in zStart..zEnd)

    override fun getCenterBlockCoordinates(yRange: IntRange, destination: Vector3i): Vector3i {
        val minBlockX = xStart shl 4
        val maxBlockX = (xEnd shl 4) + 15
        val minBlockZ = zStart shl 4
        val maxBlockZ = (zEnd shl 4) + 15

        val centerX = (minBlockX + maxBlockX) / 2
        val centerY = yRange.center
        val centerZ = (minBlockZ + maxBlockZ) / 2
        return destination.set(centerX, centerY, centerZ)
    }

    override fun getBlockSize(yRange: IntRange, destination: Vector3i): Vector3i {
        val xSize = (xEnd - xStart + 1) * 16
        val ySize = 256
        val zSize = (zEnd - zStart + 1) * 16
        return destination.set(xSize, ySize, zSize)
    }

    /**
     * The region of all blocks contained in this [ChunkClaim].
     */
    override fun getTotalVoxelRegion(yRange: IntRange, destination: AABBi): AABBi {
        destination.minX = xStart shl 4
        destination.minY = yRange.first
        destination.minZ = zStart shl 4
        destination.maxX = (xEnd shl 4) + 15
        destination.maxY = yRange.last
        destination.maxZ = (zEnd shl 4) + 15
        return destination
    }
}

private val IntRange.size get() = last - first + 1
private val IntRange.center get() = (last + first + 1) / 2
