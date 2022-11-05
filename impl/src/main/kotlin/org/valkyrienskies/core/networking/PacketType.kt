package org.valkyrienskies.core.networking

import io.netty.buffer.ByteBuf
import org.valkyrienskies.core.game.IPlayer

data class PacketType(val channel: NetworkChannel, val id: Int, val name: String) {
    private fun createPacket(data: ByteBuf) = Packet(this, data)

    fun sendToServer(data: ByteBuf) =
        channel.sendToServer(createPacket(data))

    fun sendToClient(data: ByteBuf, player: IPlayer) =
        channel.sendToClient(createPacket(data), player)

    fun sendToClients(data: ByteBuf, vararg players: IPlayer) =
        channel.sendToClients(createPacket(data), *players)

    fun sendToAllClients(data: ByteBuf) =
        channel.sendToAllClients(createPacket(data))

    fun registerServerHandler(handler: ServerHandler) =
        channel.registerServerHandler(this, handler)

    fun registerClientHandler(handler: ClientHandler) =
        channel.registerClientHandler(this, handler)
}
