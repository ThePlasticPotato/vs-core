package org.valkyrienskies.core.apigame.world

import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.ClientShip
import org.valkyrienskies.core.api.ships.QueryableShipData

interface ClientShipWorld : ShipWorld {

    override val allShips: QueryableShipData<ClientShip>
    override val loadedShips: QueryableShipData<ClientShip>

    @Deprecated("redundant", ReplaceWith("loadedShips.getShipDataIntersecting(aabb)"))
    override fun getShipObjectsIntersecting(aabb: AABBdc): List<ClientShip>

    @Deprecated("renamed", ReplaceWith("allShips"))
    override val queryableShipData: QueryableShipData<ClientShip> get() = allShips
}
