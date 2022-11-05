package org.valkyrienskies.core.game.ships.serialization

import dagger.Module
import org.valkyrienskies.core.game.ships.serialization.shipserver.ServerShipDataUpdatersModule
import org.valkyrienskies.core.game.ships.serialization.vspipeline.VSPipelineChainUpdaterModule
import org.valkyrienskies.core.game.ships.serialization.vspipeline.VSPipelineDataUpdatersModule

@Module(
    includes = [VSPipelineDataUpdatersModule::class, VSPipelineChainUpdaterModule::class, ServerShipDataUpdatersModule::class]
)
class ShipSerializationModule
