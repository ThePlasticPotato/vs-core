package org.valkyrienskies.core.impl.util.assertions.stages.constraints

class RequireFinal<S>(private val stages: List<S>) : StageConstraint<S> {
    init {
        require(stages.isNotEmpty())
    }

    override fun check(stagesSinceReset: List<S>, isReset: Boolean): String? {
        if (isReset && stagesSinceReset.subList(stagesSinceReset.size - stages.size, stagesSinceReset.size) != stages) {
            return "Required final stages to be $stages"
        }

        return null
    }
}
