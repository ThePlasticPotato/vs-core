package org.valkyrienskies.core.apigame.ships

import org.valkyrienskies.core.api.ships.QueryableShipData
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.core.api.ships.properties.ShipId

interface MutableQueryableShipData<ShipType : Ship> : QueryableShipData<ShipType>, MutableIterable<ShipType> {

    fun add(ship: ShipType)
    fun remove(ship: ShipType)
    fun remove(id: ShipId)

    // region Deprecated

    @Deprecated("renamed", ReplaceWith("add(ship)"))
    fun addShipData(ship: ShipType) = add(ship)

    @Deprecated("renamed", ReplaceWith("remove(ship)"))
    fun removeShipData(ship: ShipType) = remove(ship)

    @Deprecated("renamed", ReplaceWith("remove(id)"))
    fun removeShipData(id: ShipId) = remove(id)

    // endregion
}
