package org.valkyrienskies.core.impl.util

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor

class TickableExecutor : Executor {
    private val toRun = ConcurrentLinkedQueue<Runnable>()

    fun tick() {
        toRun.pollUntilEmpty { it.run() }
    }

    override fun execute(command: Runnable) {
        toRun.add(command)
    }
}