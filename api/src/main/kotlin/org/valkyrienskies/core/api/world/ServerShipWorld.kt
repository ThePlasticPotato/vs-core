package org.valkyrienskies.core.apigame.world

import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.LoadedServerShip
import org.valkyrienskies.core.api.ships.QueryableShipData
import org.valkyrienskies.core.api.ships.ServerShip

interface ServerShipWorld : ShipWorld {

    override val allShips: QueryableShipData<ServerShip>
    override val loadedShips: QueryableShipData<LoadedServerShip>

    @Deprecated("redundant", ReplaceWith("loadedShips.getShipDataIntersecting(aabb)"))
    override fun getShipObjectsIntersecting(aabb: AABBdc): List<LoadedServerShip>

    @Deprecated("renamed", ReplaceWith("allShips"))
    override val queryableShipData: QueryableShipData<ServerShip> get() = allShips
}
