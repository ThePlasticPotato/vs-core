package org.valkyrienskies.core.impl.datastructures

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class BlockPos2ByteOpenHashMapTest : StringSpec({

    "put and get items immediately afterwards" {
        val map = BlockPos2ByteOpenHashMap()
        checkAll<Int, Int, Int, Byte>(25_000) { x, y, z, v ->
            map.put(x, y, z, v)
            map.get(x, y, z) shouldBe v
        }

    }

    "remove items" {
        val map = BlockPos2ByteOpenHashMap()
        checkAll<Int, Int, Int>(25_000) { x, y, z ->
            map.put(x, y, z, 50)
            map.get(x, y, z) shouldBe 50
            map.remove(x, y, z)
            map.get(x, y, z) shouldBe 0
        }
    }
})
