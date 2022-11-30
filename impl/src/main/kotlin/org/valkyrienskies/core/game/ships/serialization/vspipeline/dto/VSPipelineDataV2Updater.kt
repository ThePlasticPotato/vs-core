package org.valkyrienskies.core.game.ships.serialization.vspipeline.dto

import org.mapstruct.Mapper
import org.valkyrienskies.core.game.ships.serialization.DtoUpdater
import org.valkyrienskies.core.game.ships.serialization.VSMapStructConfig
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV2Updater

@Mapper(
    uses = [ServerShipDataV2Updater::class],
    config = VSMapStructConfig::class
)
interface VSPipelineDataV2Updater : DtoUpdater<VSPipelineDataV2, VSPipelineDataV3>
