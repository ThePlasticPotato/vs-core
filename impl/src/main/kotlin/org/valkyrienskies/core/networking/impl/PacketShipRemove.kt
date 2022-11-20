package org.valkyrienskies.core.networking.impl

import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.networking.simple.SimplePacket

data class PacketShipRemove(
    val toRemove: List<ShipId>
) : SimplePacket
