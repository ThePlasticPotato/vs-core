package org.valkyrienskies.core.ecs

import dev.dominion.ecs.api.Entity
import dev.dominion.ecs.engine.IntEntity
import kotlin.reflect.KClass

typealias Ventity = Entity

operator fun Ventity.plusAssign(component: Any) {
    this.add(component)
}

operator fun Ventity.plus(component: Any): Ventity {
    this.add(component); return this
}

operator fun Ventity.minusAssign(component: Class<*>) {
    this.removeType(component)
}

operator fun Ventity.minus(component: Class<*>): Ventity {
    this.removeType(component); return this
}

operator fun Ventity.minusAssign(component: Any) {
    this.remove(component)
}

operator fun Ventity.minus(component: Any): Ventity {
    this.remove(component); return this
}

operator fun <T> Ventity.get(component: Class<T>): T {
    return (this as IntEntity).data.components[this.composition.fetchComponentIndex(component)] as T
}

operator fun <T : Any> Ventity.get(component: KClass<T>): T = this[component.java]

inline fun <reified T> Ventity.component(): T = this[T::class.java]
