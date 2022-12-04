package org.valkyrienskies.core.impl.util.assertions.stages.constraints

class RequireOrder<S>(private val stages: List<S>) : StageConstraint<S> {

    init {
        require(stages.isNotEmpty())
    }

    override fun check(stagesSinceReset: List<S>, isReset: Boolean): String? {
        var requiredIndex = 0
        for (stage in stagesSinceReset) {
            if (stages.contains(stage) &&
                stage != stages[requiredIndex] &&
                requiredIndex + 1 < stages.size && // check if we can increase required index
                stage != stages[++requiredIndex] // increase requiredIndex, this is not a duplicate
            ) {
                return "Required stages in the following order: $stages"
            }
        }
        return null
    }
}
