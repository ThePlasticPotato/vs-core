package org.valkyrienskies.core.impl.util.events

import org.valkyrienskies.core.impl.networking.RegisteredHandler

fun interface EventConsumer<T> {
    fun accept(event: T, handler: RegisteredHandler)
}
