package org.valkyrienskies.core.util.datastructures.blockpos.map

/**
 * Implementation of MurmurHash3 directly yanked from Apache Commons
 *
 * @see https://commons.apache.org/proper/commons-codec/jacoco/org.apache.commons.codec.digest/MurmurHash3.java.html
 */
object MurmurHash3 {

    private const val C1_32 = -0x3361d2af
    private const val C2_32 = 0x1b873593
    private const val R1_32 = 15
    private const val R2_32 = 13
    private const val M_32 = 5
    private const val N_32 = -0x19ab949c

    /**
     * Performs the intermediate mix step of the 32-bit hash function `MurmurHash3_x86_32`.
     *
     * @param k The data to add to the hash
     * @param hash The current hash
     * @return The new hash
     */
    fun mix32(k: Int, hash: Int): Int {
        var k = k
        var hash = hash
        k *= C1_32
        k = Integer.rotateLeft(k, R1_32)
        k *= C2_32
        hash = hash xor k
        return Integer.rotateLeft(hash, R2_32) * M_32 + N_32
    }

    /**
     * Performs the final avalanche mix step of the 32-bit hash function `MurmurHash3_x86_32`.
     *
     * @param hash The current hash
     * @return The final hash
     */
    fun fmix32(hash: Int): Int {
        var hash = hash
        hash = hash xor (hash ushr 16)
        hash *= -0x7a143595
        hash = hash xor (hash ushr 13)
        hash *= -0x3d4d51cb
        hash = hash xor (hash ushr 16)
        return hash
    }
}
