package org.valkyrienskies.core.game.ships.serialization.shiptransform

import dagger.Binds
import dagger.Module

@Module
interface ShipTransformModule {

    @Binds
    fun converter(impl: ShipTransformConverterImpl): ShipTransformConverter

}

