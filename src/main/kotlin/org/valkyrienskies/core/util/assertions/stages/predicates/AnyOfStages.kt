package org.valkyrienskies.core.util.assertions.stages.predicates

internal class AnyOfStages<S>(vararg stages: S) : StagePredicate<S> {
    private val stages = stages.toSet()

    override fun test(stage: S): Boolean = stages.contains(stage)

    override fun toString(): String = "oneOf$stages"
}
