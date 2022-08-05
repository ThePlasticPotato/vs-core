package org.valkyrienskies.core.util.assertions.stages

import org.valkyrienskies.core.util.assertions.stages.constraints.StageConstraint
import org.valkyrienskies.core.util.assertions.stages.constraints.StageConstraint.Companion.requireExact
import org.valkyrienskies.core.util.assertions.stages.constraints.StageConstraint.Companion.requireOrder
import org.valkyrienskies.core.util.assertions.stages.constraints.StageConstraint.Companion.requireStages
import org.valkyrienskies.core.util.assertions.stages.predicates.StagePredicate
import java.util.function.Predicate

class StageConstraintsBuilder<S> {
    private val constraints = ArrayList<StageConstraint<S>>()

    fun constraint(constraint: StageConstraint<S>) {
        constraints.add(constraint)
    }

    /**
     * Requires [stages] to be the final stages before the reset stage
     */
    fun requireFinal(vararg stages: S) {
        constraints.add(StageConstraint.requireFinal(*stages))
    }

    /**
     * Requires that the following [stages] are executed
     */
    fun requireStages(vararg stages: S) {
        constraints.add(StageConstraint.requireStages(*stages))
    }

    /**
     * Requires that if the specified [stages] are executed, they are executed in the provided order
     */
    fun requireOrder(vararg stages: S) {
        constraints.add(StageConstraint.requireOrder(*stages))
    }

    /**
     * Requires that if the specified [stages] are executed, they are executed in the provided order
     */
    fun requireOrder(vararg stages: StagePredicate<S>) {
        constraints.add(StageConstraint.requireOrder(*stages))
    }

    /**
     * Requires that if a stage matches any of the specified predicates, subsequent stages must
     * match only the same or subsequent predicates
     */
    fun requireOrder(block: StagePredicatesBuilder<S>.() -> Unit) {
        constraints.add(StageConstraint.requireOrder(block))
    }

    /**
     * Requires that exactly [stages] must be executed before reset (in any order)
     */
    fun requireExact(vararg stages: S) {
        constraints.add(StageConstraint.requireExact(*stages))
    }

    /**
     * Requires that if any of [stages] are executed, they are executed only once
     */
    fun requireNoDuplicates(vararg stages: S) {
        constraints.add(StageConstraint.requireNoDuplicates(*stages))
    }

    /**
     * A combination of [requireOrder] and [requireStages]
     */
    fun requireStagesAndOrder(vararg stages: S) {
        constraints.add(StageConstraint.requireStagesAndOrder(*stages))
    }

    /**
     * A combination of [requireOrder] and [requireExact]
     */
    fun requireExactOrder(vararg stages: S) {
        constraints.add(StageConstraint.requireExactOrder(*stages))
    }

    /**
     * Requires that [stages] be executed on a thread that matches the [thread] predicate
     */
    fun requireThread(thread: Predicate<Thread>, vararg stages: S) {
        constraints.add(StageConstraint.requireThread(thread, *stages))
    }

    fun build(): List<StageConstraint<S>> {
        return constraints
    }
}
