package org.valkyrienskies.core.impl.game.serialization.ships.dto

import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.valkyrienskies.core.impl.game.ships.serialization.shipserver.dto.ServerShipDataV3
import org.valkyrienskies.core.impl.util.serialization.VSJacksonUtil
import org.valkyrienskies.test_utils.generators.serverShipDataV3

class ServerShipDataV3Test : StringSpec({

    "can serialize and deserialize" {
        checkAll(Arb.serverShipDataV3()) { data ->
            val serialized = VSJacksonUtil.dtoMapper.writeValueAsString(data)
            val deserialized = VSJacksonUtil.dtoMapper.readValue<ServerShipDataV3>(serialized)
            deserialized shouldBe data
        }
    }
})
