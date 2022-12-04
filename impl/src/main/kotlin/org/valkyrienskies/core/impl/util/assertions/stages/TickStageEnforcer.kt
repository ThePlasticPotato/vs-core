package org.valkyrienskies.core.impl.util.assertions.stages

import org.valkyrienskies.core.impl.util.assertions.stages.constraints.StageConstraint

/**
 * A TickStageEnforcer helps ensure that certain stages are executed at the right time, using [StageConstraint]s that
 * enforce things such as:
 *
 * - the stages are executed in a certain order
 * - some stages are required to execute
 * - some stages must execute on a certain thread
 * - etc.
 *
 * Stages ([S]) are required to be immutable and implement [equals]/[hashCode] correctly. It is also recommended, but not
 * required, that they implement [toString]. Common stage types are [Enum]s and [String]s
 *
 * Each [StageConstraint] processes the stages which have been executed since the last "reset stage" and ensures
 * that they follow all constraints each time you invoke [stage]. The reset stage is specified when you instantiate
 * the TickStageEnforcer
 *
 * Example:
 *
 * ```kt
 * val enforcer = TickStageEnforcer("a") { // "a" is your reset stage
 *      requireOrder("a", "b", "c")
 * }
 *
 * enforcer.stage("a")
 * enforcer.stage("d")
 * // will throw, because "b" has not been executed yet
 * enforcer.stage("c")
 * ```
 *
 * @param S The stage type. Must be immutable and implement equals/hashcode. Often an [Enum] or a [String]
 */
interface TickStageEnforcer<S> {
    /**
     * Execute a new stage. Will throw a [ConstraintFailedException] if this stage doesn't pass any of this
     * enforcer's constraints (e.g. it is out of order)
     *
     * @throws ConstraintFailedException
     */
    fun stage(stage: S)
}

/**
 * @see TickStageEnforcer
 */
fun <S> TickStageEnforcer(resetStage: S, vararg constraints: StageConstraint<S>): TickStageEnforcer<S> =
    TickStageEnforcerImpl(resetStage, constraints.asList(), false)

fun <S> TickStageEnforcer(resetStage: S, block: TickStageEnforcerBuilder<S>.() -> Unit): TickStageEnforcer<S> =
    TickStageEnforcerBuilder(resetStage).apply(block).buildEnforcer()
