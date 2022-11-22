package org.valkyrienskies.core.datastructures

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.set
import io.kotest.property.checkAll
import org.joml.Vector3i
import org.valkyrienskies.test_utils.generators.vector3i
import kotlin.random.Random

class DenseBlockPosSetTest : StringSpec({

    "size is tracked correctly" {
        val set = DenseBlockPosSet()
        set shouldHaveSize 0

        set.add(0, 0, 0)
        set shouldHaveSize 1

        set.add(1, 1, 1)
        set shouldHaveSize 2

        set.add(1, 1, 1)
        set shouldHaveSize 2

        set.remove(0, 0, 0)
        set shouldHaveSize 1

        set.remove(0, 0, 0)
        set shouldHaveSize 1

        set.remove(1, 1, 1)
        set shouldHaveSize 0

        set.remove(0, 0, 0)
        set shouldHaveSize 0

        set.add(1, 1, 1)
        set.add(2, 2, 2)
        set shouldHaveSize 2

        set.clear()
        set shouldHaveSize 0
    }

    "should be able to store a lot of blocks" {
        val set = DenseBlockPosSet()
        // This should take up a max of 128*128*16*512 bytes = 128 MiB
        for (i in 0..10_000_000) {
            set.add(
                Random.nextInt(-64 * 16, 64 * 16),
                Random.nextInt(0, 256),
                Random.nextInt(-64 * 16, 64 * 16)
            )
        }
        set.size shouldBeInRange 9_000_000..10_000_000
    }

    "add should return true if not already contained" {
        val set = DenseBlockPosSet()

        checkAll { x: Int, y: Int, z: Int ->
            set.add(x, y, z) shouldBe true
            set.add(x, y, z) shouldBe false

            set.remove(x, y, z)
        }
    }

    "remove should return true if removed" {
        val set = DenseBlockPosSet()

        checkAll { x: Int, y: Int, z: Int ->
            set.add(x, y, z)
            set.remove(x, y, z) shouldBe true
            set.remove(x, y, z) shouldBe false
        }

        set shouldHaveSize 0
    }

    "remove should return false if empty" {
        val set = DenseBlockPosSet()
        checkAll { x: Int, y: Int, z: Int ->
            set.remove(x, y, z) shouldBe false
        }

        set shouldHaveSize 0
    }

    "should iterate blocks" {
        val blocksArb = Arb.set(Arb.vector3i())

        checkAll(blocksArb) { blocks: Set<Vector3i> ->
            val set = DenseBlockPosSet()

            blocks.forEach { set.add(it.x, it.y, it.z) }

            var called = 0
            set.forEach { x, y, z ->
                called++
                withClue(
                    "Block at ($x, $y, $z) should be in the set " + blocks.joinToString { "(${it.x}, ${it.y}, ${it.z})" }) {
                    Vector3i(x, y, z) shouldBeIn blocks
                }
            }

            set.forEach { x, y, z ->
                set.contains(x, y, z) shouldBe true
                set.remove(x, y, z) shouldBe true
                set.contains(x, y, z) shouldBe false
            }

            var called2 = 0
            set.forEach { _, _, _ ->
                called2++
            }

            called2 shouldBe 0
            called shouldBe blocks.size
        }
    }
})
