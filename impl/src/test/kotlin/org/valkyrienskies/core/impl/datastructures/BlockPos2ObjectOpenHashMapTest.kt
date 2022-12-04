package org.valkyrienskies.core.impl.datastructures

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll
import org.joml.Vector3i
import org.valkyrienskies.test_utils.generators.nullable
import org.valkyrienskies.test_utils.generators.vector3i

class BlockPos2ObjectOpenHashMapTest : StringSpec({

    "iterates set items correctly" {
        val blockArb = Arb.pair(Arb.vector3i(), Arb.nullable(Arb.int()))
        val blocksArb = Arb.map(blockArb)

        checkAll(blocksArb) { blocks: Map<Vector3i, Int?> ->
            val map = BlockPos2ObjectOpenHashMap<Int?>()

            blocks.forEach { (pos, value) ->
                map.put(pos.x, pos.y, pos.z, value)
            }

            var called = 0
            map.forEach { x, y, z, value ->
                called++
                value shouldBe blocks[Vector3i(x, y, z)]
            }

            called shouldBe blocks.size
        }
    }

    "iterates zero vector" {
        val map = BlockPos2ObjectOpenHashMap<Int>()
        map.put(0, 0, 0, 0)
        var called = 0
        map.forEach { x, y, z, value ->
            called++
            value shouldBe 0
        }
        called shouldBe 1
    }

})
