package org.valkyrienskies.core.program

import dagger.Module
import org.valkyrienskies.core.game.ships.ShipObjectClientWorldComponent

@Module(
    subcomponents = [ShipObjectClientWorldComponent::class],
    includes = [VSCoreModule::class]
)
class VSCoreClientModule
