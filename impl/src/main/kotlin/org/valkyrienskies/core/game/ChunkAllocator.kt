package org.valkyrienskies.core.game

import com.fasterxml.jackson.annotation.JsonAutoDetect
import org.valkyrienskies.core.api.ships.properties.ChunkClaim

/**
 * Allocates [ChunkClaim]s to be used by [ShipData].
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY) // serialize our private constructor vars
data class ChunkAllocator internal constructor(
    private var nextClaimX: Int,
    private var nextClaimZ: Int,
    private var nextShipId: Long,
) {
    companion object {
        /**
         * [ChunkAllocator]s will allocate [ChunkClaim]s within the rectangle of positions between these coordinates.
         *
         * Remember that [ChunkClaim] coordinates aren't the same as block or chunk coordinates.
         *
         * The following block positions are calculated assuming that [ChunkClaim.DIAMETER]=256. See [ChunkClaim] for more information.
         */
        const val X_INDEX_START = -7000 // Start at X=-28672000 block coordinates
        const val X_INDEX_END = 7000 // End at X=28672000 block coordinates
        const val Z_INDEX_START = 3000 // Start at Z=12288000 block coordinates
        const val Z_INDEX_END = 7000 // End at Z=28672000 block coordinates

        const val SHIP_ID_START = 0L

        fun create(): ChunkAllocator {
            return ChunkAllocator(X_INDEX_START, Z_INDEX_START, SHIP_ID_START)
        }
    }

    internal fun allocateShipId(): Long {
        return nextShipId++
    }

    /**
     * This finds the next empty chunkSet for use, currently only increases the xPos to get new
     * positions
     */
    internal fun allocateNewChunkClaim(): ChunkClaim {
        val nextClaim = ChunkClaimImpl(nextClaimX, nextClaimZ)
        // Setup coordinates for the next claim
        nextClaimX++
        if (nextClaimX > X_INDEX_END) {
            nextClaimX = X_INDEX_START
            nextClaimZ++
        }

        // Sanity check
        if (nextClaimZ !in Z_INDEX_START..Z_INDEX_END) {
            throw IllegalStateException("We ran out of chunk claims to allocate!")
        }

        return nextClaim
    }
}
