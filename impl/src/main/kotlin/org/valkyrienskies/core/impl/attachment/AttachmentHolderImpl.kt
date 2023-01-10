package org.valkyrienskies.core.impl.attachment

import org.valkyrienskies.core.api.attachment.AttachmentHolder
import java.util.function.Supplier

@Suppress("UNCHECKED_CAST")
open class AttachmentHolderImpl : AttachmentHolder {

    protected val attachments = HashMap<Class<*>, Any>()

    override fun <T : Any> get(clazz: Class<T>): T? {
        return attachments[clazz] as T?
    }

    override fun <T : Any> getOrPut(clazz: Class<T>, supplier: Supplier<T>): T {
        return attachments.computeIfAbsent(clazz) { supplier.get() } as T
    }

    override fun <T : Any> set(value: T, clazz: Class<T>): T? {
        return attachments.put(clazz, value) as T?
    }

    override fun <T : Any> remove(clazz: Class<T>): T? {
        return attachments.remove(clazz) as T?
    }
}