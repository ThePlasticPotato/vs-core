package org.valkyrienskies.core.impl.networking

import io.netty.buffer.ByteBuf
import org.valkyrienskies.core.apigame.world.IPlayer

data class Packet(val type: PacketType, val data: ByteBuf) {
    fun sendToServer() = type.sendToServer(data)
    fun sendToClient(player: IPlayer) = type.sendToClient(data, player)
}
