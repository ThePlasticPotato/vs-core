package org.valkyrienskies.core.networking.simple

import org.valkyrienskies.core.api.world.IPlayer

interface SimplePacket {
    fun receivedByClient() {}

    fun receivedByServer(player: IPlayer) {}
}
