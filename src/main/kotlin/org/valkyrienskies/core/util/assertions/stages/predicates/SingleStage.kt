package org.valkyrienskies.core.util.assertions.stages.predicates

internal class SingleStage<S>(private val stage: S) : StagePredicate<S> {
    override fun test(stage: S): Boolean = this.stage == stage
    override fun toString(): String = this.stage.toString()
}
