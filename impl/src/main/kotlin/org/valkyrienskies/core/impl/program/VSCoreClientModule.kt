package org.valkyrienskies.core.impl.program

import dagger.Module
import org.valkyrienskies.core.impl.game.ships.ShipObjectClientWorldComponent

@Module(
    subcomponents = [ShipObjectClientWorldComponent::class],
    includes = [VSCoreModule::class]
)
class VSCoreClientModule
