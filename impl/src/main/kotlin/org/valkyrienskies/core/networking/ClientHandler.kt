package org.valkyrienskies.core.networking

fun interface ClientHandler {
    fun handlePacket(packet: Packet)
}
