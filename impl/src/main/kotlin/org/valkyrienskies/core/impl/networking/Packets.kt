package org.valkyrienskies.core.impl.networking

import org.valkyrienskies.core.impl.networking.VSNetworking.NetworkingModule.TCP
import org.valkyrienskies.core.impl.networking.VSNetworking.NetworkingModule.UDP
import org.valkyrienskies.core.impl.networking.impl.*
import org.valkyrienskies.core.impl.networking.simple.SimplePacketNetworking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Contains packets used by vs-core.
 */
@Singleton
class Packets @Inject constructor(
    @TCP tcp: NetworkChannel,
    @UDP udp: NetworkChannel,
    val simplePackets: SimplePacketNetworking
) {

    val TCP_SHIP_DATA_DELTA = tcp.registerPacket("Ship data delta update")

    val UDP_SHIP_TRANSFORM = udp.registerPacket("Ship transform update")

    companion object {
        @Deprecated(message = "global state")
        lateinit var INSTANCE: Packets
    }

    init {
        INSTANCE = this

        with(simplePackets) {
            PacketRequestUdp::class.register()
            PacketUdpState::class.register()
            PacketShipDataCreate::class.register()
            PacketShipRemove::class.register()
            PacketCommonConfigUpdate::class.register()
            PacketServerConfigUpdate::class.register()
        }
    }
}
