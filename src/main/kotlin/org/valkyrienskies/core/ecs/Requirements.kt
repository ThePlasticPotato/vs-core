package org.valkyrienskies.core.ecs

import kotlin.reflect.KClass

class Requirements private constructor(val requirements: List<RequirementFrame>) {
    internal fun fetch(find: (List<KClass<*>>) -> Results<ResultsMany>): Results<ResultsMany> =
        ConcatResults(requirements.map { find(it.list) })

    companion object {
        operator fun invoke(clazz: KClass<*>) =
            Requirements(listOf(RequirementFrame(listOf(clazz))))

        operator fun invoke(building: RequirementBuilder.() -> RequirementBuilder) =
            Requirements(building(EmptyRequirementBuilder).build().map { it.prepare() }.distinct().toList())
    }
}

class RequirementFrame(val list: List<KClass<*>>) {
    internal fun requires(ands: List<RequirementFrame>) =
        RequirementFrame(ands.foldRight(list) { frame, acc -> acc + frame.list })

    internal fun prepare() =
        RequirementFrame(list.distinct().sortedBy { it.simpleName })

    override fun toString(): String = list.toString()
    override fun hashCode(): Int = list.hashCode()
    override fun equals(other: Any?): Boolean = other is RequirementFrame && list == other.list
}

sealed interface RequirementBuilder {
    infix fun and(builder: RequirementBuilder): RequirementBuilder = AndContainer(mutableListOf(this, builder))
    infix fun or(builder: RequirementBuilder): RequirementBuilder = OrContainer(this, builder)

    infix fun and(clazz: KClass<*>) = and(ClazzContainer(clazz))
    infix fun KClass<*>.and(clazz: KClass<*>) = ClazzContainer(this).and(clazz)
    infix fun KClass<*>.and(builder: RequirementBuilder) = ClazzContainer(this).and(builder)

    infix fun or(clazz: KClass<*>) = or(ClazzContainer(clazz))
    infix fun KClass<*>.or(clazz: KClass<*>) = ClazzContainer(this).or(clazz)
    infix fun KClass<*>.or(builder: RequirementBuilder) = ClazzContainer(this).or(builder)

    fun build(): Sequence<RequirementFrame>
}

private class ClazzContainer(val clazz: KClass<*>) : RequirementBuilder {
    override fun build(): Sequence<RequirementFrame> = sequenceOf(RequirementFrame(listOf(clazz)))
}

private class AndContainer(val ands: MutableList<RequirementBuilder>) : RequirementBuilder {

    override fun and(builder: RequirementBuilder): RequirementBuilder {
        ands += builder; return this
    }

    override fun build(): Sequence<RequirementFrame> =
        ands.associateWith { it.build() }
            .flatMap { (key, values) ->
                ands.filter { key != it }
                    .flatMap { it.build() }
                    .flatMap { values.map { v -> v.requires(listOf(it)) } }
            }
            .asSequence()
}

private class OrContainer(val b1: RequirementBuilder, val b2: RequirementBuilder) : RequirementBuilder {
    override fun build(): Sequence<RequirementFrame> = b1.build().plus(b2.build())
}

object EmptyRequirementBuilder : RequirementBuilder {
    override fun build(): Sequence<RequirementFrame> = emptySequence()
}
