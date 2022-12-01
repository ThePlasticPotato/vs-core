package org.valkyrienskies.core.game

import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.world.ServerShipWorld

interface VSCoreCommands {

    fun deleteShips(world: ServerShipWorld, ships: List<ServerShip>)

    fun renameShip(ship: ServerShip, newName: String)

    fun scaleShip(ship: ServerShip, newScale: Float)
}
