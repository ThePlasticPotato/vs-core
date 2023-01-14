package org.valkyrienskies.core.api.attachment

import java.util.function.Supplier

interface AttachmentHolder {

    fun <T : Any> getAttachment(clazz: Class<T>): T?

    fun <T : Any> getOrPutAttachment(clazz: Class<T>, supplier: Supplier<T>): T

    fun <T : Any> setAttachment(value: T, clazz: Class<T>): T?

    fun <T : Any> removeAttachment(clazz: Class<T>): T?

}

inline fun <reified T : Any> AttachmentHolder.getAttachment() = this.getAttachment(T::class.java)
inline fun <reified T : Any> AttachmentHolder.getOrPutAttachment(supplier: Supplier<T>): T = this.getOrPutAttachment(T::class.java, supplier)
inline fun <reified T : Any > AttachmentHolder.setAttachment(value: T) = this.setAttachment(value, T::class.java)
inline fun <reified T : Any > AttachmentHolder.removeAttachment() = this.removeAttachment(T::class.java)
