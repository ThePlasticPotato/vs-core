package org.valkyrienskies.core.api.ships

import org.valkyrienskies.core.api.ships.properties.ShipInertiaData

interface ServerShip : Ship {

    val inertiaData: ShipInertiaData

    /**
     * Sets data in the persistent storage
     *
     * @param T
     * @param clazz of T
     * @param value the data that will be stored, if null will be removed
     */
    fun <T> saveAttachment(clazz: Class<T>, value: T?)

    /**
     * Gets from the ship storage the specified class
     *  it tries it first from the non-persistent storage
     *  and afterwards from the persistent storage
     * @param T
     * @param clazz of T
     * @return the data stored inside the ship
     */
    fun <T> getAttachment(clazz: Class<T>): T?
}

inline fun <reified T> ServerShip.saveAttachment(value: T?) = saveAttachment(T::class.java, value)
inline fun <reified T> ServerShip.getAttachment() = getAttachment(T::class.java)
