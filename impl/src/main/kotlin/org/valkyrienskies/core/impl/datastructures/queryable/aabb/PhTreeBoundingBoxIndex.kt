package org.valkyrienskies.core.impl.datastructures.queryable.aabb

import ch.ethz.globis.phtree.PhTreeSolidF
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import org.joml.primitives.AABBdc

class PhTreeBoundingBoxIndex<T> : MutableBoundingBoxIndexed<T> {
    // Either V or Long2ObjectMap<V>
    private val pht: PhTreeSolidF<Any> = PhTreeSolidF.create(3)

    override fun add(bb: AABBdc, id: Long, value: T) =
        add(bb.minX(), bb.minY(), bb.minZ(), bb.maxX(), bb.maxY(), bb.maxZ(), id, value)

    fun add(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double, id: Long, value: T) {
        val min = d3(minX, minY, minZ)
        val max = d3(maxX, maxY, maxZ)

        add(min, max, id, value)
    }

    @Suppress("UNCHECKED_CAST")
    fun add(min: DoubleArray, max: DoubleArray, id: Long, value: T) {
        pht.compute(min, max) { _, _, prev ->
            when (prev) {
                // previous value is a map, add to it
                is Long2ObjectMap<*> -> {
                    prev as Long2ObjectMap<T>
                    prev.put(id, value)
                    prev
                }
                // previous value is single value, make a map
                is Any -> {
                    prev as T

                    val map = Long2ObjectOpenHashMap<T>()
                    map.put(id, value)
                    map.put(id, prev)

                    map
                }
                // previous value is null, return new value
                else -> value
            }
        }
    }

    fun remove(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double, id: Long): T {
        val min = d3(minX, minY, minZ)
        val max = d3(maxX, maxY, maxZ)

        return remove(min, max, id)
    }

    @Suppress("UNCHECKED_CAST")
    fun remove(min: DoubleArray, max: DoubleArray, id: Long): T {
        val value: T = when (val prev = pht.remove(min, max)) {
            is Long2ObjectMap<*> -> {
                prev as Long2ObjectMap<T>
                val value = prev.remove(id)
                if (prev.isNotEmpty()) {
                    pht.put(min, max, prev) // re-add the removed map
                }

                requireNotNull(value) {
                    "Failed to remove, id:$id not found in list for bb"
                }
            }

            is Any -> prev as T
            else -> throw IllegalArgumentException("bb not found")
        }

        return value
    }

    @Suppress("UNCHECKED_CAST")
    fun update(
        minXOld: Double, minYOld: Double, minZOld: Double,
        maxXOld: Double, maxYOld: Double, maxZOld: Double,
        minXNew: Double, minYNew: Double, minZNew: Double,
        maxXNew: Double, maxYNew: Double, maxZNew: Double,
        id: Long
    ) {
        val minOld = d3(minXOld, minYOld, minZOld)
        val maxOld = d3(maxXOld, maxYOld, maxZOld)

        val minNew = d3(minXNew, minYNew, minZNew)
        val maxNew = d3(maxXNew, maxYNew, maxZNew)

        add(minNew, maxNew, id, remove(minOld, maxOld, id))
    }

    override fun remove(bb: AABBdc, id: Long): T =
        remove(bb.minX(), bb.minY(), bb.minZ(), bb.maxX(), bb.maxY(), bb.maxZ(), id)

    override fun update(oldBB: AABBdc, newBB: AABBdc, id: Long) {
        update(
            oldBB.minX(), oldBB.minY(), oldBB.minZ(),
            oldBB.maxX(), oldBB.maxY(), oldBB.maxZ(),
            newBB.minX(), newBB.minY(), newBB.minZ(),
            newBB.maxX(), newBB.maxY(), newBB.maxZ(),
            id
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun getIntersecting(bb: AABBdc): Iterable<T> {
        val min = d3(bb.minX(), bb.minY(), bb.minZ())
        val max = d3(bb.maxX(), bb.maxY(), bb.maxZ())

        val results = mutableListOf<T>()
        val iter = pht.queryIntersect(min, max)
        iter.forEachRemaining { value ->
            when (value) {
                is Long2ObjectMap<*> -> {
                    value as Long2ObjectMap<T>
                    results.addAll(value.values)
                }
                is Any -> {
                    value as T
                    results.add(value)
                }
                else -> throw NullPointerException()
            }
        }

        return results
    }
}

private fun d3(x: Double, y: Double, z: Double): DoubleArray {
    return doubleArrayOf(x, y, z)
}