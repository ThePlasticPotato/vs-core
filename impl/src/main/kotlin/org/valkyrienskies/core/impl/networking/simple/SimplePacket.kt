package org.valkyrienskies.core.impl.networking.simple

import org.valkyrienskies.core.apigame.world.IPlayer

interface SimplePacket {
    fun receivedByClient() {}

    fun receivedByServer(player: IPlayer) {}
}
