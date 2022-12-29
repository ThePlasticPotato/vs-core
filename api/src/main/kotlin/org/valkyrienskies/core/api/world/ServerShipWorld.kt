package org.valkyrienskies.core.api.world

import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.VSBeta
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

    /**
     * Executes the lambda on the physics thread every physics tick.
     * If the lambda returns true, it will be executed again next tick.
     * Otherwise it will be removed.
     */ // TODO maybe change that to a parameter passed wich u call .cancel on?
    @VSBeta
    fun onPhysicsTick(lambda: (PhysicsWorld) -> Boolean)
}
