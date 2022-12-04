package org.valkyrienskies.core.impl.game.serialization

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.valkyrienskies.core.impl.game.ships.serialization.shipinertia.ShipInertiaConverter
import org.valkyrienskies.core.impl.util.serialization.VSJacksonUtil
import org.valkyrienskies.test_utils.generators.inertiaData

class InertiaDataSerializationTest : StringSpec({

    "serialize and deserialize inertia data" {
        val converter = ShipInertiaConverter()
        checkAll(Arb.inertiaData()) { inertiaData ->
            val serialized: ObjectNode = VSJacksonUtil.dtoMapper.valueToTree(converter.convertToDto(inertiaData))
            val deserialized = converter.convertToModel(VSJacksonUtil.dtoMapper.treeToValue(serialized))
            deserialized shouldBe inertiaData
        }

    }

})
