package org.valkyrienskies.core.util.assertions.stages.constraints

import org.apache.commons.collections4.CollectionUtils

internal class RequireExact<S> constructor(vararg stages: S) : StageConstraint<S> {
    init {
        require(stages.isNotEmpty())
    }

    private val stages: List<S> = stages.asList()

    override fun check(stagesSinceReset: List<S>, isReset: Boolean): String? {
        // fail early if they add something not even in stages
        if (!CollectionUtils.isProperSubCollection(stagesSinceReset, stages)) {
            return "Required exact stages $stages"
        }

        // fail if we don't have the exact same stages on reset (in any order)
        if (isReset && !CollectionUtils.isEqualCollection(stagesSinceReset, stages)) {
            return "Required exact stages $stages"
        }
        return null
    }
}
