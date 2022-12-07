package org.valkyrienskies.core.apigame

import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.world.ServerShipWorld

interface VSCoreCommands {

    fun deleteShips(world: ServerShipWorld, ships: List<ServerShip>)

    fun renameShip(ship: ServerShip, newName: String)

    fun scaleShip(ship: ServerShip, newScale: Float)

    fun teleportShip(ship: ServerShip, x: Double, y: Double, z: Double)
}
