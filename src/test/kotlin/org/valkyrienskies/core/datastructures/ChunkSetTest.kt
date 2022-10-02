package org.valkyrienskies.core.datastructures

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

class ChunkSetTest : StringSpec({

    "stores and iterates single block" {
        checkAll(Arb.int(0..15), Arb.int(0..15), Arb.int(0..15)) { testX, testY, testZ ->
            val set = ChunkSet()
            set.setBlock(testX, testY, testZ)

            var called = 0
            set.iterateSetBlocks { x, y, z ->
                called++
                x shouldBe testX
                y shouldBe testY
                z shouldBe testZ
            }

            set.getBlock(testX, testY, testZ) shouldBe true

            called shouldBe 1
        }
    }

    "stores and iterates single with x = 15" {
        val set = ChunkSet()
        set.setBlock(15, 1, 1)

        var called = 0
        set.iterateSetBlocks { x, y, z ->
            called++
            x shouldBe 15
            y shouldBe 1
            z shouldBe 1
        }

        set.getBlock(15, 1, 1) shouldBe true

        called shouldBe 1
    }

})
