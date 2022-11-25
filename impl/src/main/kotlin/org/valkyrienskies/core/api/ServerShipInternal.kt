package org.valkyrienskies.core.api

import org.valkyrienskies.core.api.ships.ServerShipCore
import org.valkyrienskies.core.game.ships.ShipDataCommon

interface ServerShipInternal : ServerShipCore, ShipInternal {
    fun asShipDataCommon(): ShipDataCommon

    /**
     * Update the [shipAABB] to when a block is added/removed.
     */
    fun updateShipAABBGenerator(posX: Int, posY: Int, posZ: Int, set: Boolean)
    fun onLoadChunk(chunkX: Int, chunkZ: Int)
    fun onUnloadChunk(chunkX: Int, chunkZ: Int)
    fun areVoxelsFullyLoaded(): Boolean
}
