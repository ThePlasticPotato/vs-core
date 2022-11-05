package org.valkyrienskies.core.api

import org.valkyrienskies.core.api.ships.Ship

interface ServerShipCore : Ship, ServerShipProvider {
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

    override val ship: ServerShipCore
        get() = this
}

inline fun <reified T> ServerShipCore.saveAttachment(value: T?) = saveAttachment(T::class.java, value)
inline fun <reified T> ServerShipCore.getAttachment() = getAttachment(T::class.java)
