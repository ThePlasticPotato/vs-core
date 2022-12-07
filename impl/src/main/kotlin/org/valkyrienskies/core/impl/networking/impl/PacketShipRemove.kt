package org.valkyrienskies.core.impl.networking.impl

import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.impl.networking.simple.SimplePacket

data class PacketShipRemove(
    val toRemove: List<ShipId>
) : SimplePacket
