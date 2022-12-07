package org.valkyrienskies.core.impl.util.assertions.stages

import org.valkyrienskies.core.impl.util.assertions.stages.predicates.StagePredicate

class StagePredicatesBuilder<S> {
    private val predicates = ArrayList<StagePredicate<S>>()

    fun matches(predicate: StagePredicate<S>) {
        predicates.add(predicate)
    }

    fun single(stage: S) {
        predicates.add(StagePredicate.single(stage))
    }

    fun anyOf(vararg stages: S) {
        predicates.add(StagePredicate.anyOf(*stages))
    }

    fun build(): List<StagePredicate<S>> {
        return predicates.toList()
    }
}
