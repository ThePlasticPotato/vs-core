package org.valkyrienskies.core.impl.game.ships.serialization.vspipeline

import org.mapstruct.Mapper
import org.valkyrienskies.core.impl.game.ships.QueryableShipDataImpl
import org.valkyrienskies.core.impl.game.ships.ShipData
import org.valkyrienskies.core.impl.game.ships.modules.ShipWorldModule
import org.valkyrienskies.core.impl.game.ships.serialization.VSMapStructConfig
import org.valkyrienskies.core.impl.game.ships.serialization.shipserver.ServerShipDataConverter
import org.valkyrienskies.core.impl.game.ships.serialization.shipserver.dto.ServerShipDataV4
import org.valkyrienskies.core.impl.game.ships.serialization.vspipeline.dto.VSPipelineDataV4

@Mapper(config = VSMapStructConfig::class, uses = [ServerShipDataConverter::class])
interface VSPipelineDataConverter {

    fun convertToModel(data: VSPipelineDataV4): ShipWorldModule {
        return ShipWorldModule(QueryableShipDataImpl(convertShipData(data.ships)), data.chunkAllocator)
    }

    fun convertShipData(data: List<ServerShipDataV4>): List<ShipData>
}
