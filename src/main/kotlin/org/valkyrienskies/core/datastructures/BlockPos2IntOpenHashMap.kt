package org.valkyrienskies.core.datastructures

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.HashCommon.arraySize
import it.unimi.dsi.fastutil.HashCommon.maxFill
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap
import org.valkyrienskies.core.datastructures.MurmurHash3.fmix32
import org.valkyrienskies.core.datastructures.MurmurHash3.mix32

fun main() {
    val map = Int2ByteOpenHashMap()
    for (i in 1..100) {
        map.put(i, i.toByte())
        map.remove(i)
    }

    map.put(101, 0)

    map.remove(101)
}

class BlockPos2IntOpenHashMap {

    companion object {
        private const val NUM_KEYS = 3
    }

    private var n: Int = 0 // table capacity
    private var size: Int = 0

    private var keys: IntArray
    private var values: IntArray

    private var containsNullKey = false
    private var maxFill: Int

    private var defRetValue = 0

    private val minN: Int
    private val f: Float // load factor
    private val mask get() = n - 1
    private val realSize get() = if (containsNullKey) size - 1 else size

    constructor(expected: Int = 10, loadFactor: Float = 0.75f) {
        f = loadFactor
        n = arraySize(expected, f)
        minN = n
        maxFill = maxFill(n, loadFactor)

        keys = IntArray((n + 1) * NUM_KEYS)
        values = IntArray(n + 1)
    }

    fun get(x: Int, y: Int, z: Int): Int {
        val pos = find(x, y, z)
        return if (pos < 0) defRetValue else values[pos]
    }

    fun put(x: Int, y: Int, z: Int, v: Int): Int {
        val pos = find(x, y, z)
        if (pos < 0) {
            insert(-pos - 1, x, y, z, v)
            return defRetValue
        }
        val oldValue: Int = values[pos]
        values[pos] = v
        return oldValue
    }

    fun remove(x: Int, y: Int, z: Int): Int {
        if (x == 0 && y == 0 && z == 0) {
            return if (containsNullKey) removeNullEntry() else defRetValue
        }
        val pos = find(x, y, z)

        if (pos < 0) {
            return defRetValue
        }

        return removeEntry(pos)
    }

    fun contains(x: Int, y: Int, z: Int): Boolean {
        return find(x, y, z) >= 0
    }

    private fun removeNullEntry(): Int {
        containsNullKey = false
        val oldValue: Int = values[n]
        size--
        if (n > minN && size < maxFill / 4 && n > Hash.DEFAULT_INITIAL_SIZE) rehash(n / 2)
        return oldValue
    }

    private fun removeEntry(pos: Int): Int {
        val oldValue: Int = values[pos]
        size--
        shiftKeys(pos)
        if (n > minN && size < maxFill / 4 && n > Hash.DEFAULT_INITIAL_SIZE) rehash(n / 2)
        return oldValue
    }

    // slightly enigmatic function, just copied it from fastutil
    private fun shiftKeys(pos: Int) {
        // Shift entries with the same hash.
        var pos = pos
        var last: Int
        var slot: Int
        var curX: Int
        var curY: Int
        var curZ: Int

        val key: IntArray = this.keys

        while (true) {
            last = pos
            pos = (pos + 1) and mask

            while (true) {
                curX = key[pos * NUM_KEYS]
                curY = key[pos * NUM_KEYS + 1]
                curZ = key[pos * NUM_KEYS + 2]
                if (curX == 0 && curY == 0 && curZ == 0) {
                    key[last * NUM_KEYS] = 0
                    key[last * NUM_KEYS + 1] = 0
                    key[last * NUM_KEYS + 2] = 0

                    return
                }

                slot = hash(curX, curY, curZ) and mask

                if (last <= pos) {
                    if (last >= slot || slot > pos) break
                } else {
                    if (slot in (pos + 1)..last) break
                }

                pos = pos + 1 and mask
            }
            key[last * NUM_KEYS] = curX
            key[last * NUM_KEYS + 1] = curY
            key[last * NUM_KEYS + 2] = curZ

            values[last] = values[pos]
        }
    }

    /**
     * MurmurHash3, implementation modified from Apache Commons
     */
    private fun hash(x: Int, y: Int, z: Int): Int {
        var hash = 0
        hash = mix32(x, hash)
        hash = mix32(y, hash)
        hash = mix32(z, hash)
        hash = hash xor 12

        return fmix32(hash)
    }

    private fun insert(pos: Int, x: Int, y: Int, z: Int, v: Int) {
        if (pos == n) {
            containsNullKey = true
        }

        val keyPos = pos * NUM_KEYS

        keys[keyPos] = x
        keys[keyPos + 1] = y
        keys[keyPos + 2] = z
        values[pos] = v

        if (size++ >= maxFill) {
            rehash(arraySize(size + 1, f))
        }
    }

    private fun rehash(newN: Int) {
        val keys = keys
        val values = values

        val newMask = newN - 1 // Note that this is used by the hashing macro
        val newKey = IntArray((newN + 1) * NUM_KEYS)
        val newValue = IntArray(newN + 1)
        var pos: Int
        var j: Int = realSize

        var i = n * NUM_KEYS

        while (j-- != 0) {
            do {
                i -= NUM_KEYS
            } while (keys[i] == 0 && keys[i + 1] == 0 && keys[i + 2] == 0)

            val oldPos = i / NUM_KEYS

            pos = hash(keys[i], keys[i + 1], keys[i + 2]) and newMask
            var keyPos = pos * NUM_KEYS

            while (newKey[keyPos] != 0 || newKey[keyPos + 1] != 0 || newKey[keyPos + 2] != 0) {
                pos = (pos + 1) and newMask
                keyPos = pos * NUM_KEYS
            }

            newKey[keyPos] = keys[i]
            newKey[keyPos + 1] = keys[i + 1]
            newKey[keyPos + 2] = keys[i + 2]

            newValue[pos] = values[oldPos]
        }
        newValue[newN] = values[n]

        n = newN
        maxFill = maxFill(n, f)
        this.keys = newKey
        this.values = newValue
    }

    private fun find(x: Int, y: Int, z: Int): Int {
        if (x == 0 && y == 0 && z == 0) {
            return if (containsNullKey) n else -(n + 1)
        }

        val keys = keys
        val mask = mask

        var pos: Int = hash(x, y, z) and mask

        while (true) {
            val keyPos: Int = pos * NUM_KEYS
            val curX = keys[keyPos]
            val curY = keys[keyPos + 1]
            val curZ = keys[keyPos + 2]

            if (curX == 0 && curY == 0 && curZ == 0) {
                return -(pos + 1)
            }
            if (x == curX && y == curY && z == curZ) {
                return pos
            }
            pos = (pos + 1) and mask
        }
    }
}

