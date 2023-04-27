package org.valkyrienskies.core.api.ships

import org.jetbrains.annotations.ApiStatus
import org.valkyrienskies.core.api.ships.properties.ShipInertiaData

interface ServerShip : Ship {
    // Slug is modifiable on server-side
    override var slug: String?

    val inertiaData: ShipInertiaData

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
