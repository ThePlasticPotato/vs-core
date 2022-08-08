package org.valkyrienskies.core.util.assertions.stages.constraints

internal class Compose<S>(private vararg val constraints: StageConstraint<S>) : StageConstraint<S> {
    override fun check(stagesSinceReset: List<S>, isReset: Boolean): String? {
        val checks = constraints.mapNotNull { it.check(stagesSinceReset, isReset) }

        return when {
            checks.size > 1 -> "Composite failure: " + checks.joinToString()
            checks.size == 1 -> checks.first()
            else -> null
        }
    }
}
