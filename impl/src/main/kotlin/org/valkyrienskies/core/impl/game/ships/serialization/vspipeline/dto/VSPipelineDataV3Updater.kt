package org.valkyrienskies.core.impl.game.ships.serialization.vspipeline.dto

import org.mapstruct.Mapper
import org.valkyrienskies.core.impl.game.ships.serialization.DtoUpdater
import org.valkyrienskies.core.impl.game.ships.serialization.VSMapStructConfig
import org.valkyrienskies.core.impl.game.ships.serialization.shipserver.dto.ServerShipDataV3Updater

@Mapper(
    uses = [ServerShipDataV3Updater::class],
    config = VSMapStructConfig::class
)
interface VSPipelineDataV3Updater : DtoUpdater<VSPipelineDataV3, VSPipelineDataV4>
