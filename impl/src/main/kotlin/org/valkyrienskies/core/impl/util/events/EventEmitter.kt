package org.valkyrienskies.core.impl.util.events

import org.valkyrienskies.core.impl.networking.RegisteredHandler
import java.util.function.Consumer
import java.util.function.Predicate

interface EventEmitter<T> {

    fun on(cb: EventConsumer<T>): RegisteredHandler

    fun on(cb: Consumer<T>): RegisteredHandler {
        return on { value, _ -> cb.accept(value) }
    }

    fun once(cb: Consumer<T>): RegisteredHandler {
        return once({ true }, cb)
    }

    fun once(predicate: Predicate<T>, cb: Consumer<T>): RegisteredHandler {
        return on { value, handler ->
            if (predicate.test(value)) {
                cb.accept(value)
                handler.unregister()
            }
        }
    }
}
