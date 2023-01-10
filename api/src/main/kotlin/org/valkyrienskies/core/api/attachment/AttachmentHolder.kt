package org.valkyrienskies.core.api.attachment

import java.util.function.Supplier

interface AttachmentHolder {

    fun <T : Any> get(clazz: Class<T>): T?

    fun <T : Any> getOrPut(clazz: Class<T>, supplier: Supplier<T>): T

    fun <T : Any> set(value: T, clazz: Class<T>): T?

    fun <T : Any> remove(clazz: Class<T>): T?

}

inline fun <reified T : Any> AttachmentHolder.get() = this.get(T::class.java)
inline fun <reified T : Any> AttachmentHolder.getOrPut(supplier: Supplier<T>): T = this.getOrPut(T::class.java, supplier)
inline fun <reified T : Any > AttachmentHolder.set(value: T) = this.set(value, T::class.java)
inline fun <reified T : Any > AttachmentHolder.remove() = this.remove(T::class.java)
