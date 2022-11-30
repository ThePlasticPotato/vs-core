package org.valkyrienskies.core.util.assertions.stages.constraints

class RequireStages<S>(vararg stages: S) : StageConstraint<S> {
    init {
        require(stages.isNotEmpty())
    }

    private val stages: List<S> = stages.asList()

    override fun check(stagesSinceReset: List<S>, isReset: Boolean): String? {
        if (isReset && !stagesSinceReset.containsAll(stages)) {
            return "Required all stages $stages - missing ${stages - stagesSinceReset.toSet()})"
        }
        return null
    }
}
