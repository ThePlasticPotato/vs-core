package org.valkyrienskies.core.game.ships.serialization

import dagger.Module
import org.valkyrienskies.core.game.ships.serialization.shipinertia.ShipInertiaModule
import org.valkyrienskies.core.game.ships.serialization.shipserver.ServerShipDataModule
import org.valkyrienskies.core.game.ships.serialization.shiptransform.ShipTransformModule
import org.valkyrienskies.core.game.ships.serialization.vspipeline.VSPipelineChainUpdaterModule
import org.valkyrienskies.core.game.ships.serialization.vspipeline.VSPipelineDataModule

@Module(
    includes = [
        VSPipelineDataModule::class,
        VSPipelineChainUpdaterModule::class,
        ServerShipDataModule::class,
        ShipInertiaModule::class,
        ShipTransformModule::class
    ]
)
class ShipSerializationModule
