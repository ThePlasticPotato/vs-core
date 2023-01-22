package org.valkyrienskies.core.impl.game.ships.serialization.shiptransform

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl
import org.valkyrienskies.core.impl.game.ships.serialization.VSMapStructConfig
import org.valkyrienskies.core.impl.game.ships.serialization.shiptransform.dto.ShipTransformDataV0

@Mapper(config = VSMapStructConfig::class)
interface ShipTransformConverter {

    @Mappings(
        Mapping(target = "positionInModel", source = "shipPositionInShipCoordinates"),
        Mapping(target = "position", source = "shipPositionInWorldCoordinates"),
        Mapping(target = "rotation", source = "shipCoordinatesToWorldCoordinatesRotation"),
        Mapping(target = "scaling", source = "shipCoordinatesToWorldCoordinatesScaling")
    )
    fun convertToModel(data: ShipTransformDataV0): ShipTransformImpl

    @Mappings(
        Mapping(source = "positionInModel", target = "shipPositionInShipCoordinates"),
        Mapping(source = "position", target = "shipPositionInWorldCoordinates"),
        Mapping(source = "rotation", target = "shipCoordinatesToWorldCoordinatesRotation"),
        Mapping(source = "scaling", target = "shipCoordinatesToWorldCoordinatesScaling")
    )
    fun convertToDto(model: ShipTransform): ShipTransformDataV0
}
