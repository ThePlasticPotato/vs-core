package org.valkyrienskies.core.impl.util

import com.google.common.collect.ImmutableSet
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.checkAll
import io.netty.buffer.Unpooled
import org.valkyrienskies.test_utils.generators.quatd
import org.valkyrienskies.test_utils.generators.vector3d

class VSCoreUtilTest : StringSpec({

    "square ints" {
        4.squared() shouldBe 4 * 4

        checkAll<Int> {
            it.squared() shouldBe it * it
        }
    }

    "square doubles" {
        65.5.squared() shouldBe 65.5 * 65.5

        checkAll<Double> {
            it.squared() shouldBe it * it
        }
    }

    "splitCamelCaseAndCapitalize Strings" {
        "thisIsATestSentence".splitCamelCaseAndCapitalize() shouldBe "This Is A Test Sentence"
        "someCamelCaseThing".splitCamelCaseAndCapitalize() shouldBe "Some Camel Case Thing"
    }

    "convert sequence to immutable set" {
        val list = listOf("a", "b", "c")

        list.asSequence().toImmutableSet() shouldBe ImmutableSet.of("a", "b", "c")

        checkAll<List<String>> {
            it.asSequence().toImmutableSet() shouldBe ImmutableSet.copyOf(it)
        }
    }

    "read and write vec3d" {
        checkAll(Arb.vector3d()) {
            val buf = Unpooled.buffer(6)
            buf.writeVec3d(it)
            buf.readVec3d() shouldBe it
        }
    }

    "read and write quatd" {
        checkAll(Arb.quatd()) {
            val buf = Unpooled.buffer(8)
            buf.writeQuatd(it)
            buf.readQuatd() shouldBe it
        }
    }
})
