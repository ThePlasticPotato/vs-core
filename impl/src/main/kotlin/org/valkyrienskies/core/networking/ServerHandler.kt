package org.valkyrienskies.core.networking

import org.valkyrienskies.core.api.world.IPlayer

fun interface ServerHandler {
    fun handlePacket(packet: Packet, player: IPlayer)
}
