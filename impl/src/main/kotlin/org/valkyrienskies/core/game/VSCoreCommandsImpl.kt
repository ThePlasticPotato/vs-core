package org.valkyrienskies.core.game

import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.world.ServerShipWorld
import org.valkyrienskies.core.api.world.ServerShipWorldCore

object VSCoreCommandsImpl : VSCoreCommands {

    override fun deleteShips(world: ServerShipWorld, ships: List<ServerShip>) {
        world as ServerShipWorldCore
        ships.forEach { ship ->
            world.deleteShip(ship)
        }
    }

    override fun renameShip(ship: ServerShip, newName: String) {
        ship.slug = newName
    }

    override fun scaleShip(ship: ServerShip, newScale: Float) {
        TODO("Not yet implemented")
    }
}
