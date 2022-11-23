package org.valkyrienskies.core.game.ships.serialization.shipserver.dto

import org.mapstruct.Mapper
import org.valkyrienskies.core.game.ships.serialization.DtoUpdater
import org.valkyrienskies.core.game.ships.serialization.VSMapStructConfig

@Mapper(config = VSMapStructConfig::class)
interface ServerShipDataV1Updater : DtoUpdater<ServerShipDataV1, ServerShipDataV2>
