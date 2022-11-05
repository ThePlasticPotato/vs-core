package org.valkyrienskies.core.util.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.longArray
import io.kotest.property.checkAll
import it.unimi.dsi.fastutil.longs.LongOpenHashSet

class FastUtilModuleTest : StringSpec({
    val mapper = ObjectMapper().registerModule(FastUtilModule()).registerKotlinModule()

    "serializes object with LongOpenHashSet" {
        data class TestClass(val a: Int, val set: LongOpenHashSet, val somethingElse: String)

        checkAll(Arb.longArray(Arb.int(0, 10), Arb.long())) { ints ->
            val testClass = TestClass(2, LongOpenHashSet(ints), "blah")
            val serialized = mapper.writeValueAsString(testClass)
            val deserialized = mapper.readValue(serialized, TestClass::class.java)

            testClass shouldBe deserialized
        }

    }

    "serializes object with LongOpenHashSet inside a nested object" {
        data class TestClass(val a: Int, val set: LongOpenHashSet, val somethingElse: String)
        data class TestClass2(val a: Int, val testClass: TestClass, val somethingElse: String)

        checkAll(Arb.longArray(Arb.int(0, 10), Arb.long())) { ints ->
            val testClass = TestClass(2, LongOpenHashSet(ints), "blah")
            val testClass2 = TestClass2(2, testClass, "blah")
            val serialized = mapper.writeValueAsString(testClass2)
            val deserialized = mapper.readValue(serialized, TestClass2::class.java)

            testClass2 shouldBe deserialized
        }
    }

})
