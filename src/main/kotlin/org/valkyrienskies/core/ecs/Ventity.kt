package org.valkyrienskies.core.ecs

import dev.dominion.ecs.api.Entity
import dev.dominion.ecs.engine.IntEntity
import dev.dominion.ecs.engine.delete
import kotlin.reflect.KClass

class Ventity(private val internal: Entity) {

    inline operator fun <reified T : Component> plusAssign(component: T) = add(T::class, component)

    inline operator fun <reified T : Component> plus(component: T): Ventity {
        add(T::class, component); return this
    }

    operator fun <T : Component> minusAssign(component: KClass<T>) = remove(component, get(component))

    operator fun <T : Component> minus(component: KClass<T>): Ventity {
        remove(component, get(component)); return this
    }

    inline operator fun <reified T : Component> minusAssign(component: T) = remove(T::class, component)

    inline operator fun <reified T : Component> minus(component: T): Ventity {
        remove(T::class, component); return this
    }

    operator fun <T> get(component: Class<T>): T? {
        internal as IntEntity

        return if (internal.composition.isMultiComponent)
            internal.data.components[internal.composition.fetchComponentIndex(component)] as T
        else if (internal.composition.componentTypes[0] == component)
            internal.data.components[0] as T
        else
            null
    }

    operator fun <T : Any> get(component: KClass<T>): T? = this[component.java]

    inline fun <reified T> component(): T? = this[T::class.java]

    fun <T : Component> add(clazz: KClass<T>, component: T) {
        clazz.invokeAdd(latestWorld, this, component)
        internal.add(component)
    }

    fun <T : Component> remove(clazz: KClass<T>, component: Any?) {
        if (component == null) return
        clazz.invokeRemove(latestWorld, this, component as T)
        internal.remove(component)
    }

    fun delete(): Boolean {
        internal as IntEntity
        internal.data.components.iterator().forEach { remove(it::class as KClass<out Component>, it) }

        return internal.delete()
    }
}
