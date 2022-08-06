package org.valkyrienskies.core.networking

import org.valkyrienskies.core.networking.impl.PacketCommonConfigUpdate
import org.valkyrienskies.core.networking.impl.PacketRequestUdp
import org.valkyrienskies.core.networking.impl.PacketServerConfigUpdate
import org.valkyrienskies.core.networking.impl.PacketShipDataCreate
import org.valkyrienskies.core.networking.impl.PacketShipRemove
import org.valkyrienskies.core.networking.impl.PacketUdpState
import org.valkyrienskies.core.networking.simple.register

/**
 * Contains packets used by vs-core.
 */
object Packets {
    val TCP_SHIP_DATA_DELTA = VSNetworking.TCP.registerPacket("Ship data delta update")

    val UDP_SHIP_TRANSFORM = VSNetworking.UDP.registerPacket("Ship transform update")

    init {
        PacketRequestUdp::class.register()
        PacketUdpState::class.register()
        PacketShipDataCreate::class.register()
        PacketShipRemove::class.register()
        PacketCommonConfigUpdate::class.register()
        PacketServerConfigUpdate::class.register()
    }

    // no-op to force the class to load
    internal fun init() {}
}
