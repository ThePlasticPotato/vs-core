package org.valkyrienskies.core.api.ships

import org.jetbrains.annotations.ApiStatus

interface LoadedServerShip : LoadedShip, ServerShip {

    /**
     * Sets data in the non-persistent storage
     *
     * @param T
     * @param clazz of T
     * @param value the data that will be stored, if null will be removed
     */
    @ApiStatus.Experimental
    fun <T> setAttachment(clazz: Class<T>, value: T?)
}

@ApiStatus.Experimental
inline fun <reified T> LoadedServerShip.setAttachment(value: T?) = setAttachment(T::class.java, value)
