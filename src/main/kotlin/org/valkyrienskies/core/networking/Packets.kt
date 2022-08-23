package org.valkyrienskies.core.networking

import org.valkyrienskies.core.networking.VSNetworking.NetworkingModule.TCP
import org.valkyrienskies.core.networking.VSNetworking.NetworkingModule.UDP
import org.valkyrienskies.core.networking.impl.PacketCommonConfigUpdate
import org.valkyrienskies.core.networking.impl.PacketRequestUdp
import org.valkyrienskies.core.networking.impl.PacketServerConfigUpdate
import org.valkyrienskies.core.networking.impl.PacketShipDataCreate
import org.valkyrienskies.core.networking.impl.PacketShipRemove
import org.valkyrienskies.core.networking.impl.PacketUdpState
import org.valkyrienskies.core.networking.simple.SimplePacketNetworking
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
