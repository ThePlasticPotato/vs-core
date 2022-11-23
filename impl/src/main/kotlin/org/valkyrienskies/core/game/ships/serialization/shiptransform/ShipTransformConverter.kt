package org.valkyrienskies.core.game.ships.serialization.shiptransform

import org.mapstruct.Mapper
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.game.ships.ShipTransformImpl
import org.valkyrienskies.core.game.ships.serialization.VSMapStructConfig
import org.valkyrienskies.core.game.ships.serialization.shiptransform.dto.ShipTransformDataV0

@Mapper(config = VSMapStructConfig::class)
interface ShipTransformConverter {
    fun convertToModel(data: ShipTransformDataV0): ShipTransformImpl

    fun convertToDto(model: ShipTransform): ShipTransformDataV0
}