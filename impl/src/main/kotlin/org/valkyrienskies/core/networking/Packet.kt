package org.valkyrienskies.core.networking

import io.netty.buffer.ByteBuf
import org.valkyrienskies.core.api.world.IPlayer

data class Packet(val type: PacketType, val data: ByteBuf) {
    fun sendToServer() = type.sendToServer(data)
    fun sendToClient(player: IPlayer) = type.sendToClient(data, player)
}
