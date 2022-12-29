package org.valkyrienskies.core.api.world

import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.ships.PhysShip
import org.valkyrienskies.core.api.ships.properties.ShipId

@VSBeta
interface PhysicsWorld {

    operator fun get(shipId: ShipId): PhysShip?

    // TODO
}
