package org.valkyrienskies.core.impl.game.ships.serialization.vspipeline.dto

import org.mapstruct.Mapper
import org.valkyrienskies.core.impl.game.ships.serialization.DtoUpdater
import org.valkyrienskies.core.impl.game.ships.serialization.VSMapStructConfig
import org.valkyrienskies.core.impl.game.ships.serialization.shipserver.dto.ServerShipDataV1Updater

@Mapper(
    uses = [ServerShipDataV1Updater::class],
    config = VSMapStructConfig::class
)
interface VSPipelineDataV1Updater : DtoUpdater<VSPipelineDataV1, VSPipelineDataV2>
