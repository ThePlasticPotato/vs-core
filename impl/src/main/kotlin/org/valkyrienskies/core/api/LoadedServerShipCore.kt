package org.valkyrienskies.core.api

interface LoadedServerShipCore : LoadedShipCore, ServerShipCore {

    /**
     * Sets data in the non-persistent storage
     *
     * @param T
     * @param clazz of T
     * @param value the data that will be stored, if null will be removed
     */
    fun <T> setAttachment(clazz: Class<T>, value: T?)
}

inline fun <reified T> LoadedServerShipCore.setAttachment(value: T?) = setAttachment(T::class.java, value)
