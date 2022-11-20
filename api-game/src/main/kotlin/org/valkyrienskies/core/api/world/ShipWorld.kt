package org.valkyrienskies.core.api.world

import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.LoadedShip
import org.valkyrienskies.core.api.ships.QueryableShipData
import org.valkyrienskies.core.api.ships.Ship

interface ShipWorld {

    val allShips: QueryableShipData<Ship>
    val loadedShips: QueryableShipData<LoadedShip>

    @Deprecated("redundant", ReplaceWith("loadedShips.getShipDataIntersecting(aabb)"))
    fun getShipObjectsIntersecting(aabb: AABBdc): List<LoadedShip>

    @Deprecated("renamed", ReplaceWith("allShips"))
    val queryableShipData: QueryableShipData<Ship> get() = allShips
}
