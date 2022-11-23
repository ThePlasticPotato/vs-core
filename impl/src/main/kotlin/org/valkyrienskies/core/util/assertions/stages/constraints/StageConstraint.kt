package org.valkyrienskies.core.util.assertions.stages.constraints

import org.valkyrienskies.core.util.assertions.stages.StagePredicatesBuilder
import org.valkyrienskies.core.util.assertions.stages.TickStageEnforcer
import org.valkyrienskies.core.util.assertions.stages.constraints.StageConstraint.Companion.requireExact
import org.valkyrienskies.core.util.assertions.stages.constraints.StageConstraint.Companion.requireExactOrder
import org.valkyrienskies.core.util.assertions.stages.constraints.StageConstraint.Companion.requireFinal
import org.valkyrienskies.core.util.assertions.stages.constraints.StageConstraint.Companion.requireNoDuplicates
import org.valkyrienskies.core.util.assertions.stages.constraints.StageConstraint.Companion.requireOrder
import org.valkyrienskies.core.util.assertions.stages.constraints.StageConstraint.Companion.requireStages
import org.valkyrienskies.core.util.assertions.stages.constraints.StageConstraint.Companion.requireStagesAndOrder
import org.valkyrienskies.core.util.assertions.stages.constraints.StageConstraint.Companion.requireThread
import org.valkyrienskies.core.util.assertions.stages.predicates.StagePredicate
import java.util.function.Predicate

/**
 * StageConstraints are used by the [TickStageEnforcer] to make sure the stages since the last reset stage are valid.
 * There are several pre-defined constraints for you to work with, such as:
 *
 * - [requireOrder]
 * - [requireStages]
 * - [requireStagesAndOrder]
 * - [requireExact]
 * - [requireExactOrder]
 * - [requireThread]
 * - [requireNoDuplicates]
 * - [requireFinal]
 *
 * You can easily implement your own StageConstraint and pass it to the [TickStageEnforcer] if the built-in
 * constraints don't satisfy your needs.
 */
fun interface StageConstraint<S> {

    /**
     * This will be called whenever a new stage is executed. When a reset stage is executed, this function will be
     * called twice. Once with [stagesSinceReset] containing the same stages as it did before (not containing the
     * final reset stage) and [isReset] set to `true`. The second time [isReset] will be set to `false` and
     * [stagesSinceReset] will only contain the reset stage.
     */
    fun check(stagesSinceReset: List<S>, isReset: Boolean): String?

    companion object {

        /**
         * Requires [stages] to be the final stages before the reset stage
         */
        fun <S> requireFinal(vararg stages: S): StageConstraint<S> = RequireFinal(stages.asList())

        /**
         * Requires that the following [stages] are executed
         */
        fun <S> requireStages(vararg stages: S): StageConstraint<S> = RequireStages(*stages)

        /**
         * Requires that if the specified [stages] are executed, they are executed in the provided order
         */
        fun <S> requireOrder(vararg stages: S): StageConstraint<S> = RequireOrder(stages.asList())

        /**
         * Requires that if the specified [stages] are executed, they are executed in the provided order
         */
        fun <S> requireOrder(stages: List<S>): StageConstraint<S> = RequireOrder(stages.toList())

        /**
         * Requires that if a stage matches any of the specified [predicates], subsequent stages must
         * match only the same or subsequent predicates
         */
        fun <S> requireOrder(vararg predicates: StagePredicate<S>): StageConstraint<S> =
            RequireOrderPredicate(predicates.asList())

        /**
         * Requires that if a stage matches any of the specified [predicates], subsequent stages must
         * match only the same or subsequent predicates
         */
        @JvmName("requireOrderList")
        fun <S> requireOrder(predicates: List<StagePredicate<S>>): StageConstraint<S> =
            RequireOrderPredicate(predicates.toList())

        /**
         * Requires that if a stage matches any of the specified predicates, subsequent stages must
         * match only the same or subsequent predicates
         */
        fun <S> requireOrder(block: StagePredicatesBuilder<S>.() -> Unit): StageConstraint<S> =
            requireOrder(StagePredicatesBuilder<S>().apply(block).build())

        /**
         * Requires that exactly [stages] must be executed before reset (in any order)
         */
        fun <S> requireExact(vararg stages: S): StageConstraint<S> = RequireExact(*stages)

        /**
         * Requires that if any of [stages] are executed, they are executed only once
         */
        fun <S> requireNoDuplicates(vararg stages: S): StageConstraint<S> = RequireNoDuplicates(*stages)

        fun <S> compose(vararg constraints: StageConstraint<S>): StageConstraint<S> = Compose(*constraints)

        /**
         * A combination of [requireOrder] and [requireStages]
         */
        fun <S> requireStagesAndOrder(vararg stages: S): StageConstraint<S> =
            compose(requireOrder(*stages), requireStages(*stages))

        /**
         * A combination of [requireOrder] and [requireExact]
         */
        fun <S> requireExactOrder(vararg stages: S): StageConstraint<S> =
            compose(requireOrder(*stages), requireExact(*stages))

        /**
         * Requires that [stages] be executed on a thread that matches the [thread] predicate
         */
        fun <S> requireThread(thread: Predicate<Thread>, vararg stages: S): StageConstraint<S> =
            RequireThread(thread, *stages)
    }
}
