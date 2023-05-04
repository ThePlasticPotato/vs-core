package org.valkyrienskies.core.apigame

import org.joml.Quaterniondc
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.api.world.ServerShipWorld

interface VSCoreCommands {

    fun deleteShips(world: ServerShipWorld, ships: List<ServerShip>)

    fun renameShip(ship: ServerShip, newName: String)

    fun scaleShip(ship: ServerShip, newScale: Float)

    fun teleportShip(world: ServerShipWorld, ship: ServerShip, teleportData: ShipTeleportData)
}

interface ShipTeleportData {
    val newPos: Vector3dc
    val newRot: Quaterniondc
    val newVel: Vector3dc
    val newOmega: Vector3dc

    fun createNewShipTransform(oldShipTransform: ShipTransform): ShipTransform
}
