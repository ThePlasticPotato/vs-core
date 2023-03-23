package org.valkyrienskies.core.impl.util.assertions.stages

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * @see [synchronized]
 */
class SynchronizedTickStageEnforcer<S>(
    private val enforcer: TickStageEnforcer<S>
) : TickStageEnforcer<S> {
    private val lock = ReentrantLock()

    override fun stage(stage: S) {
        lock.withLock {
            enforcer.stage(stage)
        }
    }
}

/**
 * Returns a synchronized wrapper over this [TickStageEnforcer], similar to `Collections.synchronizedList`
 */
fun <S> TickStageEnforcer<S>.synchronized(): TickStageEnforcer<S> =
    if (this is SynchronizedTickStageEnforcer) this else SynchronizedTickStageEnforcer(this)