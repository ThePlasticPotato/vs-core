package org.valkyrienskies.core.api

import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.core.game.VSBlockType
import org.valkyrienskies.core.util.PrivateApi

/**
 * Abstraction of a ship, there are many types such as offline ships
 *  or loaded ships so this is the generic interface for all ships.
 */
interface ShipCore : Ship {
    @PrivateApi
    @JvmSynthetic
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
