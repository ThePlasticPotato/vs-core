package org.valkyrienskies.core.ecs

import kotlin.reflect.KClass

interface Component

private val defaults = mutableMapOf<KClass<out Component>, () -> Any>()

fun <T : Component> KClass<T>.default(producer: () -> T): () -> T {
    if (defaults.put(this, producer) != null) throw IllegalStateException("Default for $this already set")
    return producer
}

val <T : Component> KClass<T>.hasDefault: Boolean get() = defaults.contains(this)

// val <T : Component> KClass<T>.default: T? get() = defaults[this]?.let { it() as T? }
val KClass<*>.default: Any? get() = defaults[this]?.let { it() }
