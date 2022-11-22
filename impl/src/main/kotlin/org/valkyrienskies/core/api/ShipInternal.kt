package org.valkyrienskies.core.api

import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.core.api.ships.properties.VSBlockType

/**
 * Abstraction of a ship, there are many types such as offline ships
 *  or loaded ships so this is the generic interface for all ships.
 */
interface ShipInternal : Ship {

    fun onSetBlock(
        posX: Int,
        posY: Int,
        posZ: Int,
        oldBlockType: VSBlockType,
        newBlockType: VSBlockType,
        oldBlockMass: Double,
        newBlockMass: Double
    )
}
