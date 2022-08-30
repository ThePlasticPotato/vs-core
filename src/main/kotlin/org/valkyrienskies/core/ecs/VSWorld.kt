package org.valkyrienskies.core.ecs

import dev.dominion.ecs.engine.CompositionRepository
import dev.dominion.ecs.engine.DataComposition
import dev.dominion.ecs.engine.ResultSet
import dev.dominion.ecs.engine.system.LoggingSystem.Context
import org.valkyrienskies.core.util.ConcatMutableIterator
import org.valkyrienskies.core.util.mutMap
import java.util.Collections.emptyIterator
import kotlin.reflect.KClass

// TODO remove
internal lateinit var latestWorld: VSWorld

class VSWorld(context: Context = Context("VSWorld", 0)) {
    private val compositions = CompositionRepository(context)
    internal val defaults = mutableMapOf<KClass<out Component>, () -> Any>()
    internal val listeners =
        mutableMapOf<KClass<out Component>, Pair<MutableList<(Ventity, Component) -> Unit>, MutableList<(Ventity, Component) -> Unit>>>()

    init {
        latestWorld = this
    }

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

    fun spawn(name: String? = null, vararg components: Component): Ventity {
        val r = Ventity(
            compositions.getOrCreate(components)
                .createEntity(name, false, *components)
        )

        fun <T : Component> add(clazz: KClass<T>, component: Component) = clazz.invokeAdd(this, r, component as T)

        components.forEach { add(it::class, it) }
        return r
    }

    fun spawn(name: String? = null, prepared: BuiltComposition) =
        Ventity(prepared.owner.createEntity(name, true, prepared.values))

    internal fun compose(components: List<KClass<out Component>>): DataComposition =
        compositions.getOrCreate(components.toTypedArray())

    fun findOwnersOf(vararg classes: KClass<out Component>): MutableIterator<Ventity> =
        compositions.findWith(*classes.map { it.java }.toTypedArray())
            ?.map { (_, it) ->
                object : MutableIterable<Ventity> {
                    override fun iterator(): MutableIterator<Ventity> {
                        return it.composition.tenant.iterator().mutMap { Ventity(it) }
                    }
                }
            }
            ?.iterator()?.let { ConcatMutableIterator(it) } ?: emptyIterator()

    fun delete(ventity: Ventity) = ventity.delete()
}
