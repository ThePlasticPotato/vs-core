package org.valkyrienskies.core.impl.networking

fun interface ClientHandler {
    fun handlePacket(packet: Packet)
}
