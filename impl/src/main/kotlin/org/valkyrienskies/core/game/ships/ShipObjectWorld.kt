package org.valkyrienskies.core.game.ships

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.plus
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.core.game.DimensionId
import org.valkyrienskies.core.game.VSBlockType
import org.valkyrienskies.core.util.coroutines.TickableCoroutineDispatcher
import org.valkyrienskies.core.util.intersectsAABB
import org.valkyrienskies.core.util.logger

/**
 * Manages all the [ShipObject]s in a world.
 */
abstract class ShipObjectWorld<ShipObjectType : ShipObject> {

    abstract val queryableShipData: QueryableShipData<Ship>
    // abstract val loadedShips: QueryableShipData<LoadedShip>

    abstract val loadedShips: QueryableShipData<ShipObjectType>

    @Deprecated(message = "use loadedShips", replaceWith = ReplaceWith("loadedShips"))
    val shipObjects: Map<ShipId, ShipObjectType> get() = loadedShips.idToShipData

    private val _dispatcher = TickableCoroutineDispatcher()
    val dispatcher: CoroutineDispatcher = _dispatcher
    val coroutineScope = MainScope() + _dispatcher

    var tickNumber = 0
        private set

    protected open fun preTick() {
        try {
            _dispatcher.tick()
        } catch (ex: Exception) {
            logger.error("Error while ticking ships", ex)
        }
        tickNumber++
    }

    protected abstract fun postTick()

    open fun onSetBlock(
        posX: Int,
        posY: Int,
        posZ: Int,
        dimensionId: DimensionId,
        oldBlockType: VSBlockType,
        newBlockType: VSBlockType,
        oldBlockMass: Double,
        newBlockMass: Double
    ) {
        // If there is a ShipData at this position and dimension, then tell it about the block update
        queryableShipData.getShipDataFromChunkPos(posX shr 4, posZ shr 4, dimensionId)
            ?.onSetBlock(posX, posY, posZ, oldBlockType, newBlockType, oldBlockMass, newBlockMass)
    }

    open fun getShipObjectsIntersecting(aabb: AABBdc): List<ShipObjectType> =
        shipObjects.values.filter { it.shipData.shipAABB.intersectsAABB(aabb) }.toCollection(ArrayList())

    abstract fun destroyWorld()

    companion object {
        private val logger by logger()
    }
}
