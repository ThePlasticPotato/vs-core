package org.valkyrienskies.core.api

import org.joml.primitives.AABBdc
import org.valkyrienskies.core.game.ships.ShipTransform

interface ClientShip : Ship, LoadedShip {
    /**
     * The transform used when rendering the ship
     */
    val renderTransform: ShipTransform
    val renderAABB: AABBdc
}
