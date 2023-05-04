package org.valkyrienskies.core.impl.game

import org.joml.Quaterniond
import org.joml.Quaterniondc
import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.api.world.ServerShipWorld
import org.valkyrienskies.core.apigame.ShipTeleportData
import org.valkyrienskies.core.apigame.VSCoreCommands
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl

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

    override fun teleportShip(world: ServerShipWorld, ship: ServerShip, teleportData: ShipTeleportData) {
        world as ServerShipWorldCore
        world.teleportShip(ship, teleportData)
    }
}

data class ShipTeleportDataImpl(
    override val newPos: Vector3dc = Vector3d(),
    override val newRot: Quaterniondc = Quaterniond(),
    override val newVel: Vector3dc = Vector3d(),
    override val newOmega: Vector3dc = Vector3d(),
) : ShipTeleportData {
    override fun createNewShipTransform(oldShipTransform: ShipTransform): ShipTransform = ShipTransformImpl(
        positionInWorld = newPos,
        positionInShip = oldShipTransform.positionInShip,
        shipToWorldRotation = newRot,
        shipToWorldScaling = oldShipTransform.shipToWorldScaling,
    )
}
