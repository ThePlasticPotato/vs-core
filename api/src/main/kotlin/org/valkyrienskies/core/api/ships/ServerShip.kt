package org.valkyrienskies.core.api.ships

import org.jetbrains.annotations.ApiStatus
import org.joml.Matrix4dc
import org.valkyrienskies.core.api.ships.properties.ShipInertiaData

interface ServerShip : Ship {

    val inertiaData: ShipInertiaData


    /**
     * Immediately sets the position of the ship such that the specified position in the ship, [shipX], [shipY], [shipZ]
     * corresponds with the specified position in the world, [worldX], [worldY], [worldZ]
     *
     * Transform updates from the physics thread will be ignored until it reads the updated transform.
     */
    fun setPosition(shipX: Double, shipY: Double, shipZ: Double, worldX: Double, worldY: Double, worldZ: Double) {
        TODO()
    }

    /**
     * Immediately sets the [shipToWorld] transform of the ship.
     *
     * Transform updates from the physics thread will be ignored until it reads the updated transform.
     */
    fun setTransform(shipToWorld: Matrix4dc) {

    }

    /**
     * Sets data in the persistent storage
     *
     * @param T
     * @param clazz of T
     * @param value the data that will be stored, if null will be removed
     */
    @ApiStatus.Experimental
    fun <T> saveAttachment(clazz: Class<T>, value: T?)

    /**
     * Gets from the ship storage the specified class
     *  it tries it first from the non-persistent storage
     *  and afterwards from the persistent storage
     * @param T
     * @param clazz of T
     * @return the data stored inside the ship
     */
    @ApiStatus.Experimental
    fun <T> getAttachment(clazz: Class<T>): T?
}

@ApiStatus.Experimental
inline fun <reified T> ServerShip.saveAttachment(value: T?) = saveAttachment(T::class.java, value)

@ApiStatus.Experimental
inline fun <reified T> ServerShip.getAttachment() = getAttachment(T::class.java)
