package org.valkyrienskies.core.game

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.valkyrienskies.core.game.ships.ShipData
import org.valkyrienskies.core.util.serialization.VSJacksonUtil
import org.valkyrienskies.test_utils.generators.shipData

class ShipDataTest : StringSpec({

    /**
     * Tests the correctness of ShipData serialization and deserialization.
     */
    "test serialization and deserialization" {
        checkAll(Arb.shipData()) { shipData ->
            // Now serialize and deserialize and verify that they are the same
            val blockPosSetSerialized = VSJacksonUtil.defaultMapper.writeValueAsBytes(shipData)
            val blockPosSetDeserialized = VSJacksonUtil.defaultMapper.readValue(
                blockPosSetSerialized,
                ShipData::class.java
            )

            // Verify that both are equal
            shipData shouldBe blockPosSetDeserialized
        }
    }
})
