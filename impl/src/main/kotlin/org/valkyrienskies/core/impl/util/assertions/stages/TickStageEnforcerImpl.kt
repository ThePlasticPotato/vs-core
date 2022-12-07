package org.valkyrienskies.core.impl.util.assertions.stages

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import org.valkyrienskies.core.impl.util.assertions.stages.constraints.StageConstraint

class TickStageEnforcerImpl<S>(
    private val resetStage: S,
    private val constraints: List<StageConstraint<S>>,
    private val ignoreUntilFirstReset: Boolean = false,
    private val ignoreRepeatFailures: Boolean = true,
) : TickStageEnforcer<S> {

    private val stagesSinceReset = ArrayList<S>()
    private val failedConstraintsSinceReset: IntSet = IntOpenHashSet()

    override fun stage(stage: S) {
        val reset = stage == resetStage
        val isFirstStage = stagesSinceReset.isEmpty()

        if (ignoreUntilFirstReset && !reset && isFirstStage) {
            return
        }

        if (!reset && isFirstStage) {
            throw IllegalArgumentException("First executed stage must be the reset ($resetStage) but was $stage")
        }

        val errors = mutableListOf<String>()

        if (reset && !isFirstStage) {
            constraints
                .asSequence()
                .filterIndexed { i, _ -> !failedConstraintsSinceReset.contains(i) }
                .mapNotNullTo(errors) { it.check(stagesSinceReset, true) }

            if (errors.isNotEmpty()) {
                throw ConstraintFailedException(
                    "Constraints failed (last stage before reset). Stages since last reset: $stagesSinceReset" +
                        "\n${errors.joinToString("\n")}"
                )
            }

            failedConstraintsSinceReset.clear()
            stagesSinceReset.clear()
        }

        stagesSinceReset.add(stage)
        constraints.mapIndexedNotNullTo(errors) { index, constraint ->
            constraint.check(stagesSinceReset, false)?.also {
                if (ignoreRepeatFailures) {
                    failedConstraintsSinceReset.add(index)
                }
            }
        }

        if (errors.isNotEmpty()) {
            throw ConstraintFailedException(
                "Constraints failed. Stages since last reset: $stagesSinceReset" +
                    "\n${errors.joinToString("\n")}"
            )
        }
    }
}
