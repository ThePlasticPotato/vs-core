package org.valkyrienskies.core.networking

import org.valkyrienskies.core.game.IPlayer

fun interface ServerHandler {
    fun handlePacket(packet: Packet, player: IPlayer)
}
