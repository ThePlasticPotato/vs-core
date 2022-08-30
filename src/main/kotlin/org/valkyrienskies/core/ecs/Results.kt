package org.valkyrienskies.core.ecs

import dev.dominion.ecs.engine.CompositionRepository
import dev.dominion.ecs.engine.CompositionRepository.Node
import dev.dominion.ecs.engine.DataComposition
import dev.dominion.ecs.engine.DataComposition.StateIterator
import dev.dominion.ecs.engine.IntEntity
import dev.dominion.ecs.engine.system.IndexKey
import org.valkyrienskies.core.util.ConcatMutableIterator
import java.util.Spliterator
import java.util.Spliterators
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.reflect.KClass

interface Results<T> : MutableIterable<T>, dev.dominion.ecs.api.Results<T> {
    fun without(vararg componentTypes: KClass<*>): Results<T> =
        IResults(without(*componentTypes.map { it.java }.toTypedArray()))

    fun withAlso(vararg componentTypes: KClass<*>): Results<T> =
        IResults(withAlso(*componentTypes.map { it.java }.toTypedArray()))

    override fun <S : Enum<S>?> withState(state: S): Results<T>

    override fun without(vararg componentTypes: Class<*>): Results<T>

    override fun withAlso(vararg componentTypes: Class<*>): Results<T>
}

internal fun <T> dev.dominion.ecs.api.Results<T>.i(): Results<T> = IResults(this)

@JvmInline
private value class IResults<T>(val results: dev.dominion.ecs.api.Results<T>) : Results<T>,
    dev.dominion.ecs.api.Results<T> by results {

    override fun <S : Enum<S>?> withState(state: S): Results<T> = IResults(results.withState(state))

    override fun without(vararg componentTypes: Class<*>): Results<T> = IResults(results.without(*componentTypes))

    override fun withAlso(vararg componentTypes: Class<*>): Results<T> = IResults(results.withAlso(*componentTypes))
}

class ResultsMany(val indices: Map<Class<*>, Int>, val owner: IntEntity) {
    operator fun <T : Any> get(index: KClass<T>) =
        indices[index.java]?.let { owner.components[it] as T? }
}

internal class ResultsSetMany(
    val compositionRepository: CompositionRepository,
    val nodeMap: Map<IndexKey, Node>,
    val types: List<Class<*>>
) : Results<ResultsMany> {

    private var stateKey: IndexKey? = null

    fun compositionIterator(composition: DataComposition): MutableIterable<ResultsMany> {

        return object : MutableIterable<ResultsMany> {
            val indices = types.associateWith { composition.fetchComponentIndex(it) }

            override fun iterator(): MutableIterator<ResultsMany> =
                object : MutableIterator<ResultsMany> {
                    val iterator = if (stateKey == null)
                        composition.tenant.iterator()
                    else
                        StateIterator(composition.getStateRootEntity(stateKey))

                    override fun hasNext(): Boolean = iterator.hasNext()

                    override fun next(): ResultsMany = ResultsMany(indices, iterator.next())

                    override fun remove() = iterator.remove()
                }
        }
    }

    override fun iterator(): MutableIterator<ResultsMany> {
        return if (nodeMap != null)
            if (nodeMap.size > 1)
                ConcatMutableIterator(nodeMap.values.map { compositionIterator(it.composition) }.iterator())
            else
                compositionIterator(nodeMap.values.iterator().next().composition).iterator()
        else object : MutableIterator<ResultsMany> {
            override fun hasNext(): Boolean = false
            override fun next(): ResultsMany = throw UnsupportedOperationException()
            override fun remove() = throw UnsupportedOperationException()
        }
    }

    override fun <S : Enum<S>?> withState(state: S): Results<ResultsMany> {
        stateKey = DataComposition.calcIndexKey(state, compositionRepository.classIndex)
        return this
    }

    override fun stream(): Stream<ResultsMany> {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false
        )
    }

    override fun without(vararg componentTypes: Class<*>): Results<ResultsMany> {
        compositionRepository.without(nodeMap, *componentTypes)
        return this
    }

    override fun withAlso(vararg componentTypes: Class<*>): Results<ResultsMany> {
        compositionRepository.withAlso(nodeMap, *componentTypes)
        return this
    }
}

class ConcatResults<T>(private val internal: List<Results<T>>) : Results<T> {
    override fun iterator(): MutableIterator<T> = ConcatMutableIterator(internal.iterator())

    override fun stream(): Stream<T> = internal.stream().flatMap { it.stream() }

    override fun without(vararg componentTypes: Class<*>): Results<T> =
        ConcatResults(internal.map { it.without(*componentTypes) })

    override fun withAlso(vararg componentTypes: Class<*>): Results<T> =
        ConcatResults(internal.map { it.withAlso(*componentTypes) })

    override fun <S : Enum<S>?> withState(state: S): Results<T> =
        ConcatResults(internal.map { it.withState(state) })
}
