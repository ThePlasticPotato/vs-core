package org.valkyrienskies.core.game.serialization

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.valkyrienskies.core.game.ChunkClaim
import org.valkyrienskies.core.util.serialization.VSJacksonUtil
import org.valkyrienskies.test_utils.generators.chunkClaim

class ChunkClaimSerializationTest : StringSpec({
    "can serialize and deserialize chunkclaim" {
        checkAll(Arb.chunkClaim()) { claim ->
            val serialized = VSJacksonUtil.dtoMapper.valueToTree<ObjectNode>(claim)
            serialized shouldHaveSize 2

            val deserialized = VSJacksonUtil.dtoMapper.readValue<ChunkClaim>(serialized.toString())
            deserialized shouldBe claim
        }
    }
})
