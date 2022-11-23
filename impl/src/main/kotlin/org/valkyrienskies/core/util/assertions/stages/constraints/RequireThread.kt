package org.valkyrienskies.core.util.assertions.stages.constraints

import java.util.function.Predicate

class RequireThread<S>(
    private val checkThread: Predicate<Thread>,
    private vararg val stages: S
) : StageConstraint<S> {
    init {
        require(stages.isNotEmpty())
    }

    override fun check(stagesSinceReset: List<S>, isReset: Boolean): String? {
        if (stages.contains(stagesSinceReset.last()) && !checkThread.test(Thread.currentThread())) {
            return "Stages $stages require a different thread. Current thread: ${Thread.currentThread()}"
        }

        return null
    }
}
