package org.valkyrienskies.core.game.ships.serialization.shipserver

import dagger.Binds
import dagger.Module
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV0Updater
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV0UpdaterImpl
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV1Updater
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV1UpdaterImpl
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV2Updater
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV2UpdaterImpl

@Module
internal interface ServerShipDataUpdatersModule {

    @Binds
    fun converter(impl: ServerShipDataConverterImpl): ServerShipDataConverter

    @Binds
    fun v0(impl: ServerShipDataV0UpdaterImpl): ServerShipDataV0Updater

    @Binds
    fun v1(impl: ServerShipDataV1UpdaterImpl): ServerShipDataV1Updater

    @Binds
    fun v2(impl: ServerShipDataV2UpdaterImpl): ServerShipDataV2Updater
}

