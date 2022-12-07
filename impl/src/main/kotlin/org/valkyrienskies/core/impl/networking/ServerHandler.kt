package org.valkyrienskies.core.impl.networking

import org.valkyrienskies.core.apigame.world.IPlayer

fun interface ServerHandler {
    fun handlePacket(packet: Packet, player: IPlayer)
}
