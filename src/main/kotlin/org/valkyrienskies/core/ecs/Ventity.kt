package org.valkyrienskies.core.ecs

import dev.dominion.ecs.api.Entity

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
