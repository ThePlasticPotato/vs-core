package org.valkyrienskies.core.impl.util.assertions.stages

class TickStageEnforcerBuilder<S>(private val resetStage: S) : StageConstraintsBuilder<S>() {

    private var ignoreUntilFirstReset = false
    private var ignoreRepeatFailures = true
    private var synchronized = false

    fun synchronized() {
        synchronized = true
    }

    fun dontIgnoreRepeatFailures() {
        ignoreRepeatFailures = false
    }

    /**
     * Specifies that this enforcer should ignore all stages until the first reset
     */
    fun ignoreUntilFirstReset() {
        ignoreUntilFirstReset = true
    }

    fun buildEnforcer(): TickStageEnforcer<S> {
        val enforcer = TickStageEnforcerImpl(
            resetStage,
            buildConstraints(),
            ignoreUntilFirstReset,
            ignoreRepeatFailures
        )

        return if (synchronized) enforcer.synchronized() else enforcer
    }
}
