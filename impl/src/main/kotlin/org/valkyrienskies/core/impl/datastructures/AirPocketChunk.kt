package org.valkyrienskies.core.impl.datastructures

import java.util.BitSet

class AirPocketChunk {
    var airChunk: BitSet

    init {
        airChunk = BitSet(16 * 16 * 16)
    }

    fun setBitInChunk(index: Int, value: Boolean) {
        airChunk[index] = value
    }

    fun isSealed(index: Int): Boolean {
        return airChunk[index]
    }
}
