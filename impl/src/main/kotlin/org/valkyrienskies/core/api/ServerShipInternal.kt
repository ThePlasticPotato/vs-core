package org.valkyrienskies.core.api

import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.game.ships.ShipDataCommon

interface ServerShipInternal : ServerShip, ShipInternal {
    fun asShipDataCommon(): ShipDataCommon
}
