package org.valkyrienskies.core.api.ships

import org.valkyrienskies.core.api.ships.properties.ShipId

interface MutableQueryableShipData<ShipType : Ship> : QueryableShipData<ShipType>, MutableIterable<ShipType> {
    fun addShipData(shipData: ShipType)
    fun removeShipData(shipData: ShipType)
    fun removeShipData(id: ShipId)
}
