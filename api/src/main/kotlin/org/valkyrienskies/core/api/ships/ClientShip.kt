package org.valkyrienskies.core.api.ships

import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.properties.ShipTransform

interface ClientShip : LoadedShip {
    /**
     * The transform used when rendering the ship
     */
    val renderTransform: ShipTransform
    val renderAABB: AABBdc
}
