package org.valkyrienskies.core.api.attachment

fun interface AttachmentUpdater<in OLD : Any, out NEW : Any> {
    fun update(data: OLD): NEW
}