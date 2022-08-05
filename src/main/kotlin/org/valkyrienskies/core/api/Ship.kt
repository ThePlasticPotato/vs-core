package org.valkyrienskies.core.api

import org.joml.Matrix4dc
import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.game.ChunkClaim
import org.valkyrienskies.core.game.DimensionId
import org.valkyrienskies.core.game.VSBlockType
import org.valkyrienskies.core.game.ships.ShipId
import org.valkyrienskies.core.game.ships.ShipTransform
import org.valkyrienskies.core.util.PrivateApi

/**
 * Abstraction of a ship, there are many types such as offline ships
 *  or loaded ships so this is the generic interface for all ships.
 */
interface Ship : ShipProvider {

    val id: ShipId

    val shipTransform: ShipTransform
    val prevTickShipTransform: ShipTransform

    val chunkClaim: ChunkClaim
    val chunkClaimDimension: DimensionId
    val shipAABB: AABBdc
    val velocity: Vector3dc
    val omega: Vector3dc

    val shipToWorld: Matrix4dc get() = shipTransform.shipToWorldMatrix
    val worldToShip: Matrix4dc get() = shipTransform.worldToShipMatrix

    /**
     * Gets from the ship storage the specified class
     *  it tries it first from the non-persistent storage
     *  and afterwards from the persistent storage
     * @param T
     * @param clazz of T
     * @return the data stored inside the ship
     */
    fun <T> getAttachment(clazz: Class<T>): T?

    /**
     * Sets data in the non-persistent storage
     *  if you are using a ship that is not loaded in
     *  it will do nothing
     *
     * @param T
     * @param clazz of T
     * @param value the data that will be stored, if null will be removed
     */
    fun <T> setAttachment(clazz: Class<T>, value: T?)

    override val ship: Ship
        get() = this

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

inline fun <reified T> Ship.getAttachment() = getAttachment(T::class.java)
inline fun <reified T> Ship.setAttachment(value: T?) = setAttachment(T::class.java, value)
