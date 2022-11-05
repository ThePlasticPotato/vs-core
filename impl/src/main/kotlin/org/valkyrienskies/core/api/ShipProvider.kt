package org.valkyrienskies.core.api

/**
 * ServerShipProvider: A interface that just specifies the existence of a ship field
 */
interface ServerShipProvider {
    val ship: ServerShip?
}

/**
 * Modifiable version of ServerShipProvider, will be automatically set when used as an attachment
 */
interface ServerShipUser : ServerShipProvider {
    override var ship: ServerShip?
}
