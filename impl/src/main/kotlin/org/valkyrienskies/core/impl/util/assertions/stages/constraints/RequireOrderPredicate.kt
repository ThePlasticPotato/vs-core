package org.valkyrienskies.core.impl.util.assertions.stages.constraints

import org.valkyrienskies.core.impl.util.assertions.stages.predicates.StagePredicate

class RequireOrderPredicate<S>(val stages: List<StagePredicate<S>>) : StageConstraint<S> {
    init {
        require(stages.isNotEmpty())
    }

    override fun check(stagesSinceReset: List<S>, isReset: Boolean): String? {
        var minMatching = 0
        for (stage in stagesSinceReset) {
            val firstMatching = stages.indexOfFirst { predicate -> predicate.test(stage) }

            if (firstMatching == -1) {
                continue
            }

            if (firstMatching < minMatching) {
                return "Required stages matching predicate in the following order: $stages"
            }

            minMatching = firstMatching
        }
        return null
    }
}
