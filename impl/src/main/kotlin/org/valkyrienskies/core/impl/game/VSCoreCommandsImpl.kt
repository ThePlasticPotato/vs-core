package org.valkyrienskies.core.impl.game

import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.world.ServerShipWorld
import org.valkyrienskies.core.apigame.VSCoreCommands
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore

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

    override fun teleportShip(ship: ServerShip, x: Double, y: Double, z: Double) {
        TODO("Not yet implemented")
    }
}
