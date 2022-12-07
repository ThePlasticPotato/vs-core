package org.valkyrienskies.core.impl.networking.impl

import org.valkyrienskies.core.impl.networking.simple.SimplePacket

data class PacketRequestUdp(val udpV: Int, val secretKeyBytes: ByteArray) : SimplePacket
data class PacketUdpState(
    val port: Int,
    val state: Boolean,
    val id: Long
) : SimplePacket
