package org.valkyrienskies.core.api

import org.valkyrienskies.core.api.ships.ServerShip

/**
 * ServerShipProvider: A interface that just specifies the existence of a ship field
 */
@Deprecated("sus")
interface ServerShipProvider {
    val ship: ServerShip?
}

/**
 * Modifiable version of ServerShipProvider, will be automatically set when used as an attachment
 */
@Deprecated("sus")
interface ServerShipUser : ServerShipProvider {
    override var ship: ServerShip?
}
