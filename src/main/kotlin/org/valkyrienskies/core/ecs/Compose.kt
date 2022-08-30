package org.valkyrienskies.core.ecs

import dev.dominion.ecs.engine.DataComposition
import kotlin.reflect.KClass

fun compose(builder: ComposeBuilder.() -> Unit): Composition =
    ComposeBuilder().apply(builder).build()

class Composition(val components: List<KClass<Component>>, val optionals: List<KClass<Component>>) {
    private lateinit var composed: DataComposition

    fun entity(world: VSWorld, name: String? = null, builder: CompositionFiller.() -> Unit): Ventity =
        CompositionFiller(name).apply(builder).build(world)

    inner class CompositionFiller(val name: String?) {
        val provided = mutableListOf<Component>()

        fun <T : Component> provide(component: T) {
            provided.add(component)
        }

        fun build(world: VSWorld): Ventity {
            val building = mutableListOf<Any>()

            components.forEach { clazz ->
                val iterator = provided.listIterator()
                var found = false
                while (iterator.hasNext()) {
                    val component = iterator.next()

                    if (component::class == clazz) {
                        building.add(component)
                        iterator.remove()
                        found = true
                        break
                    }
                }

                if (!found) {
                    if (clazz.hasDefault) {
                        building.add(clazz.default!!)
                    } else throw Exception("No component of type ${clazz.simpleName} found and there is no default!")
                }

            }

            val optionals = false

            if (!::composed.isInitialized && !optionals) {
                composed = world.compose(components)
            }



            return if (optionals)
                world.spawn(name, *building.toTypedArray())
            else
                world.spawn(name, composed)
        }
    }
}

class ComposeBuilder {
    private val needs = mutableListOf<KClass<Component>>()
    private val optional = mutableListOf<KClass<Component>>()

    operator fun <T : Component> KClass<T>.unaryPlus() = needs.add(this as KClass<Component>)
    operator fun <T : Component> KClass<T>.not() = optional.add(this as KClass<Component>)

    internal fun build(): Composition = Composition(needs, optional)
}

class BuiltComposition(val values: List<Any>, val owner: DataComposition)
