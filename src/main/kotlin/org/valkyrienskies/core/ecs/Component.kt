package org.valkyrienskies.core.ecs

import kotlin.reflect.KClass

interface Component

private val defaults = mutableMapOf<KClass<out Component>, () -> Any>()
private val listeners =
    mutableMapOf<KClass<out Component>, Pair<MutableList<(Component) -> Unit>, MutableList<(Component) -> Unit>>>()

fun <T : Component> KClass<T>.default(producer: () -> T): () -> T {
    if (defaults.put(this, producer) != null) throw IllegalStateException("Default for $this already set")
    return producer
}

val <T : Component> KClass<T>.hasDefault: Boolean get() = defaults.contains(this)

// val <T : Component> KClass<T>.default: T? get() = defaults[this]?.let { it() as T? }
val KClass<*>.default: Any? get() = defaults[this]?.let { it() }

fun <T : Component> KClass<T>.listenOnAdd(listener: (Ventity, T) -> Unit) =
    listeners.getOrPut(this) { Pair(mutableListOf(), mutableListOf()) }.first.add(listener as (Component) -> Unit)

fun <T : Component> KClass<T>.listenOnRemove(listener: (Ventity, T) -> Unit) =
    listeners.getOrPut(this) { Pair(mutableListOf(), mutableListOf()) }.second.add(listener as (Component) -> Unit)
