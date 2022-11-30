package org.valkyrienskies.core.datastructures

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.zip
import io.kotest.property.checkAll
import org.joml.Vector3i

class SingleChunkDenseBlockPosSetTest : StringSpec({

    "add should return true if not already contained" {
        val set = SingleChunkDenseBlockPosSet()

        checkAll(Arb.int(0..15), Arb.int(0..15), Arb.int(0..15)) { x, y, z ->
            set.add(x, y, z) shouldBe true
            set.add(x, y, z) shouldBe false

            set.remove(x, y, z)
        }
    }

    "remove should return true if removed" {
        val set = SingleChunkDenseBlockPosSet()

        checkAll(Arb.int(0..15), Arb.int(0..15), Arb.int(0..15)) { x, y, z ->
            set.add(x, y, z)
            set.remove(x, y, z) shouldBe true
            set.remove(x, y, z) shouldBe false
        }
    }

    "remove should return false if not removed" {
        val set = SingleChunkDenseBlockPosSet()

        checkAll(Arb.int(0..15), Arb.int(0..15), Arb.int(0..15)) { x, y, z ->
            set.remove(x, y, z) shouldBe false
        }
    }

    "errors if you try to store a block outside the bounds" {
        checkAll(Arb.int().filterNot { it in 0..15 },
            Arb.int().filterNot { it in 0..15 },
            Arb.int().filterNot { it in 0..15 }) { x, y, z ->

            val set = SingleChunkDenseBlockPosSet()

            shouldThrow<IllegalArgumentException> {
                set.add(x, y, z)
            }

            shouldThrow<IllegalArgumentException> {
                set.remove(x, y, z)
            }
        }
    }

    "stores and iterates single block" {
        checkAll(Arb.int(0..15), Arb.int(0..15), Arb.int(0..15)) { testX, testY, testZ ->
            val set = SingleChunkDenseBlockPosSet()
            set.add(testX, testY, testZ)

            var called = 0
            set.forEach { x, y, z ->
                called++
                x shouldBe testX
                y shouldBe testY
                z shouldBe testZ
            }

            set.contains(testX, testY, testZ) shouldBe true

            called shouldBe 1
        }
    }

    "stores and iterates, and unsets a list of blocks" {
        val blockArb = Arb.zip(Arb.int(0..15), Arb.int(0..15), Arb.int(0..15), ::Vector3i)
        val blocksArb = Arb.set(blockArb)

        checkAll(blocksArb) { blocks: Set<Vector3i> ->
            val set = SingleChunkDenseBlockPosSet()

            blocks.forEach { set.add(it.x, it.y, it.z) }
            var called = 0
            set.forEach { x, y, z ->
                called++
                Vector3i(x, y, z) shouldBeIn blocks
            }

            blocks.forEach { v ->
                set.contains(v.x, v.y, v.z) shouldBe true
                set.remove(v.x, v.y, v.z)
                set.contains(v.x, v.y, v.z) shouldBe false
            }

            var called2 = 0
            set.forEach { _, _, _ ->
                called2++
            }

            called2 shouldBe 0
            called shouldBe blocks.size
        }
    }

    "stores and iterates single with x = 15" {
        val set = SingleChunkDenseBlockPosSet()
        set.add(15, 1, 1)

        var called = 0
        set.forEach { x, y, z ->
            called++
            x shouldBe 15
            y shouldBe 1
            z shouldBe 1
        }

        set.contains(15, 1, 1) shouldBe true

        called shouldBe 1
    }

})
