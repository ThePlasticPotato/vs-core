package org.valkyrienskies.core.game.ships.serialization.shipinertia

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.valkyrienskies.core.api.ships.properties.ShipInertiaData
import org.valkyrienskies.core.game.ships.ShipInertiaDataImpl
import org.valkyrienskies.core.game.ships.serialization.VSMapStructConfig
import org.valkyrienskies.core.game.ships.serialization.shipinertia.dto.ShipInertiaDataV0

@Mapper(config = VSMapStructConfig::class)
interface ShipInertiaConverter {
    @Mappings(
        Mapping(target = "_momentOfInertiaTensor", source = "momentOfInertiaTensor"),
        Mapping(target = "_mass", source = "shipMass"),
        Mapping(target = "_centerOfMassInShip", source = "centerOfMassInShipSpace")
    )
    fun convertToModel(data: ShipInertiaDataV0): ShipInertiaDataImpl

    fun convertToDto(model: ShipInertiaData): ShipInertiaDataV0
}