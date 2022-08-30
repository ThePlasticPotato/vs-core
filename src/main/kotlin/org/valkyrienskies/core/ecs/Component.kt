package org.valkyrienskies.core.ecs

import kotlin.reflect.KClass

interface Component

fun <T : Component> KClass<T>.default(world: VSWorld, producer: () -> T): () -> T {
    if (world.defaults.put(this, producer) != null) throw IllegalStateException("Default for $this already set")
    return producer
}

fun <T : Component> KClass<T>.hasDefault(world: VSWorld): Boolean = world.defaults.contains(this)

// val <T : Component> KClass<T>.default: T? get() = defaults[this]?.let { it() as T? }
fun KClass<*>.getDefault(world: VSWorld): Any? = world.defaults[this]?.let { it() }

fun <T : Component> KClass<T>.listenOnAdd(world: VSWorld, listener: (Ventity, T) -> Unit) =
    world.listeners.getOrPut(this) { Pair(mutableListOf(), mutableListOf()) }.first.add(
        listener as (Ventity, Component) -> Unit
    )

fun <T : Component> KClass<T>.listenOnRemove(world: VSWorld, listener: (Ventity, T) -> Unit) =
    world.listeners.getOrPut(this) { Pair(mutableListOf(), mutableListOf()) }.second.add(
        listener as (Ventity, Component) -> Unit
    )

internal fun <T : Component> KClass<T>.invokeAdd(world: VSWorld, entity: Ventity, component: T) {
    world.listeners[this]?.first?.forEach { it(entity, component) }
}

internal fun <T : Component> KClass<T>.invokeRemove(world: VSWorld, entity: Ventity, component: T) {
    world.listeners[this]?.second?.forEach { it(entity, component) }
}
