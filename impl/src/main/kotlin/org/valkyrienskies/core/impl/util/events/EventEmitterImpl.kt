package org.valkyrienskies.core.impl.util.events

import org.valkyrienskies.core.impl.networking.RegisteredHandler
import java.util.concurrent.ConcurrentHashMap

class EventEmitterImpl<T> : EventEmitter<T> {

    private val listeners = ConcurrentHashMap.newKeySet<EventListener<T>>()

    fun emit(value: T) {
        listeners.forEach { it.accept(value) }
    }

    override fun on(cb: EventConsumer<T>): RegisteredHandler {
        val listener = EventListener(cb)
        listener.handler = RegisteredHandler { listeners.remove(listener) }
        listeners.add(listener)

        return listener.handler
    }

    private data class EventListener<T>(val cb: EventConsumer<T>) {
        lateinit var handler: RegisteredHandler

        fun accept(event: T) {
            cb.accept(event, handler)
        }
    }
}
