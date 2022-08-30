package org.valkyrienskies.core.ecs

import dev.dominion.ecs.api.Composition
import dev.dominion.ecs.engine.CompositionRepository
import dev.dominion.ecs.engine.DataComposition
import dev.dominion.ecs.engine.ResultSet
import dev.dominion.ecs.engine.system.LoggingSystem.Context
import kotlin.reflect.KClass

class VSWorld {
    private val compositions = CompositionRepository(Context("Skeld", 0))

    fun find(types: List<KClass<*>>): Results<ResultsMany> {
        val mapped = types.map { it.java }
        return ResultsSetMany(
            this.compositions,
            this.compositions.findWith(*mapped.toTypedArray()),
            mapped
        )
    }

    fun find(requirements: Requirements): Results<ResultsMany> =
        requirements.fetch(this::find)

    fun find(vararg types: KClass<*>): Results<ResultsMany> = find(types.toList())

    fun <T : Any> find(type: KClass<T>): Results<dev.dominion.ecs.api.Results.With1<T>> =
        ResultSet.With1(
            this.compositions,
            this.compositions.findWith(type.java),
            type.java
        ).i()

    fun <A : Any, B : Any> find(type1: KClass<A>, type2: KClass<B>): Results<dev.dominion.ecs.api.Results.With2<A, B>> =
        ResultSet.With2(
            this.compositions,
            this.compositions.findWith(type1.java, type2.java),
            type1.java, type2.java
        ).i()

    fun spawn(name: String? = null, vararg components: Any) =
        compositions.getOrCreate(components)
            .createEntity(name, false, (if (components.isEmpty()) null else components) as Array<Any>?)

    fun spawn(name: String? = null, prepared: Composition.OfTypes) =
        (prepared.context as DataComposition).createEntity(name, true, prepared.components)

    // TODO kotlinfy this
    fun compose(): Composition = compositions.preparedComposition
}
