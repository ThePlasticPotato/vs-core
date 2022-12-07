package org.valkyrienskies.core.impl.util.assertions.stages.predicates

class SingleStage<S>(private val stage: S) : StagePredicate<S> {
    override fun test(stage: S): Boolean = this.stage == stage
    override fun toString(): String = this.stage.toString()
}
