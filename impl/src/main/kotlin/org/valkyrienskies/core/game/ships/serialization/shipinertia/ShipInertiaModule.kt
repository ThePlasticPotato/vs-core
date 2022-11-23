package org.valkyrienskies.core.game.ships.serialization.shipinertia

import dagger.Binds
import dagger.Module

@Module
interface ShipInertiaModule {

    @Binds
    fun converter(impl: ShipInertiaConverterImpl): ShipInertiaConverter

}

