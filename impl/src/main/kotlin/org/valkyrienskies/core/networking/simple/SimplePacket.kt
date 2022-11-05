package org.valkyrienskies.core.networking.simple

import org.valkyrienskies.core.game.IPlayer

interface SimplePacket {
    fun receivedByClient() {}

    fun receivedByServer(player: IPlayer) {}
}
