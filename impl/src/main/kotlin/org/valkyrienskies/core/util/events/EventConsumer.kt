package org.valkyrienskies.core.util.events

import org.valkyrienskies.core.networking.RegisteredHandler

fun interface EventConsumer<T> {
    fun accept(event: T, handler: RegisteredHandler)
}
