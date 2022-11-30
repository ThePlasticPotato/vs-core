package org.valkyrienskies.core.game.ships.serialization.shiptransform

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.game.ships.ShipTransformImpl
import org.valkyrienskies.core.game.ships.serialization.VSMapStructConfig
import org.valkyrienskies.core.game.ships.serialization.shiptransform.dto.ShipTransformDataV0

@Mapper(config = VSMapStructConfig::class)
interface ShipTransformConverter {

    @Mappings(
        Mapping(target = "positionInShip", source = "shipPositionInShipCoordinates"),
        Mapping(target = "positionInWorld", source = "shipPositionInWorldCoordinates"),
        Mapping(target = "shipToWorldRotation", source = "shipCoordinatesToWorldCoordinatesRotation"),
        Mapping(target = "shipToWorldScaling", source = "shipCoordinatesToWorldCoordinatesScaling")
    )
    fun convertToModel(data: ShipTransformDataV0): ShipTransformImpl

    @Mappings(
        Mapping(source = "positionInShip", target = "shipPositionInShipCoordinates"),
        Mapping(source = "positionInWorld", target = "shipPositionInWorldCoordinates"),
        Mapping(source = "shipToWorldRotation", target = "shipCoordinatesToWorldCoordinatesRotation"),
        Mapping(source = "shipToWorldScaling", target = "shipCoordinatesToWorldCoordinatesScaling")
    )
    fun convertToDto(model: ShipTransform): ShipTransformDataV0
}