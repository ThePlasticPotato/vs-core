package org.valkyrienskies.core.impl.game.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Component
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.valkyrienskies.core.impl.game.ships.serialization.shipserver.ServerShipDataConverter
import org.valkyrienskies.core.impl.game.ships.serialization.shipserver.dto.ServerShipDataV0Updater
import org.valkyrienskies.core.impl.program.VSCoreModule
import org.valkyrienskies.test_utils.generators.shipData
import javax.inject.Named
import javax.inject.Singleton

class ServerShipDataConverterTest : StringSpec({
    val component = DaggerServerShipDataConverterComponent.create()

    "can deserialize from normal ShipData" {
        val converter = component.converter()

        checkAll(Arb.shipData()) { ship ->
            val asDto1 = converter.convertToDto(ship)
            val asModel2 = converter.convertToModel(asDto1)
            val asDto2 = converter.convertToDto(asModel2)

            asDto1 shouldBe asDto2
            asModel2 shouldBe ship
        }
    }
})

@Singleton
@Component(modules = [VSCoreModule::class])
interface ServerShipDataConverterComponent {
    fun converter(): ServerShipDataConverter
    fun updater(): ServerShipDataV0Updater

    @Named("dto")
    fun dtoMapper(): ObjectMapper
    fun mapper(): ObjectMapper
}

