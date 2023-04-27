package org.valkyrienskies.core.impl.api

import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.apigame.ships.LoadedServerShipCore

interface LoadedServerShipInternal : LoadedServerShipCore, LoadedShipInternal,
    ServerShipInternal {
    // The id of the last ship teleport request, used to ignore position updates from the physics engine that would undo
    // the teleport.
    val shipTeleportId: Int

    fun teleportShip(newTransform: ShipTransform)
}
