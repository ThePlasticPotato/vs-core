package org.valkyrienskies.core.impl.networking.impl

import org.valkyrienskies.core.impl.game.ships.ShipDataCommon
import org.valkyrienskies.core.impl.networking.simple.SimplePacket

data class PacketShipDataCreate(
    val toCreate: List<ShipDataCommon>
) : SimplePacket
