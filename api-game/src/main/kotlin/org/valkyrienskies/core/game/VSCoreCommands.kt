package org.valkyrienskies.core.game

import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.world.ServerShipWorld

object VSCoreCommands {

    fun deleteShips(world: ServerShipWorld, ships: List<ServerShip>) {
        ships.forEach { ship ->
            world.deleteShip(ship)
        }
    }

    fun renameShip(ship: ServerShip, newName: String) {
        ship.slug = newName
    }

    fun scaleShip(serverShip: ServerShip, float: Float) {
        TODO("Not yet implemented")
    }
}
