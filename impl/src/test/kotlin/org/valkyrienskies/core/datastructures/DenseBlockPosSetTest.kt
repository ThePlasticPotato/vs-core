package org.valkyrienskies.core.datastructures

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.set
import io.kotest.property.checkAll
import org.joml.Vector3i
import org.valkyrienskies.test_utils.generators.vector3i
import kotlin.random.Random

class DenseBlockPosSetTest : StringSpec({

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
                set.remove(x, y, z)
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
