package org.valkyrienskies.core.impl.datastructures

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.valkyrienskies.core.util.datastructures.blockpos.map.BlockPos2IntOpenHashMap

class BlockPos2IntOpenHashMapTest : StringSpec({
    "put and get items immediately afterwards" {
        val map = BlockPos2IntOpenHashMap()
        checkAll<Int, Int, Int, Int>(25_000) { x, y, z, v ->
            map.put(x, y, z, v)
            map.get(x, y, z) shouldBe v
        }

    }

    "remove items" {
        val map = BlockPos2IntOpenHashMap()
        checkAll<Int, Int, Int>(25_000) { x, y, z ->
            map.put(x, y, z, 50)
            map.get(x, y, z) shouldBe 50
            map.remove(x, y, z)
            map.get(x, y, z) shouldBe 0
        }
    }
})
