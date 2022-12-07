package org.valkyrienskies.core.impl.networking

fun interface RegisteredHandler {
    fun unregister()
}

fun Iterable<RegisteredHandler>.unregisterAll() = forEach { it.unregister() }
