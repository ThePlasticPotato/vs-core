package org.valkyrienskies.core.pipelines

import org.valkyrienskies.core.util.logger
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.min

class VSPhysicsPipelineBackgroundTask(private val vsPipeline: VSPipeline, private var idealPhysicsTps: Int = 60) :
    Runnable {
    // When this is set to true, this task will kill itself at the next opportunity
    private var killTask = false

    // A non-blocking thread-safe queue
    private val queuedTasksQueue = ConcurrentLinkedQueue<() -> Unit>()

    private var lostTime: Long = 0

    private val prevPhysTicksTimeMillis: Queue<Long> = LinkedList()

    override fun run() {
        try {
            while (true) {
                if (killTask) break // Stop looping

                val timeToSimulateNs = 1e9 / idealPhysicsTps.toDouble()

                val timeBeforePhysicsTick = System.nanoTime()

                // Execute queued tasks
                while (!queuedTasksQueue.isEmpty()) queuedTasksQueue.remove()()

                val timeStep = timeToSimulateNs / 1e9

                // Run the physics tick
                vsPipeline.tickPhysics(vsPipeline.getPhysicsGravity(), timeStep)

                val timeToRunPhysTick = System.nanoTime() - timeBeforePhysicsTick

                // Keep track of when physics tick finished
                val currentTimeMillis = System.currentTimeMillis()
                prevPhysTicksTimeMillis.add(currentTimeMillis)
                // Remove physics ticks that were over [PHYS_TICK_AVERAGE_WINDOW_MS] ms ago
                while (prevPhysTicksTimeMillis.isNotEmpty() &&
                    prevPhysTicksTimeMillis.peek() + PHYS_TICK_AVERAGE_WINDOW_MS < currentTimeMillis
                ) {
                    prevPhysTicksTimeMillis.remove()
                }

                // Ideal time minus actual time to run physics tick
                val timeDif = timeToSimulateNs - timeToRunPhysTick

                if (timeDif < 0) {
                    // Physics tick took too long, store some lost time to catch up
                    lostTime = min(lostTime - timeDif.toLong(), MAX_LOST_TIME)
                } else {
                    if (lostTime > timeDif) {
                        // Catch up
                        lostTime -= timeDif.toLong()
                    } else {
                        val timeToWait = timeDif - lostTime
                        lostTime = 0
                        Thread.sleep((timeToWait / 1e6).toLong())
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error in physics pipeline background task", e)
            repeat(10) { logger.error("!!!!!!! VS PHYSICS THREAD CRASHED !!!!!!!") }
        }
        logger.warn("Physics pipeline ending")
    }

    fun tellTaskToKillItself() {
        killTask = true
    }

    fun queueTask(task: () -> Unit) {
        queuedTasksQueue.add(task)
    }

    fun computePhysicsTPS(): Double {
        return prevPhysTicksTimeMillis.size.toDouble() / (PHYS_TICK_AVERAGE_WINDOW_MS.toDouble() / 1000.0)
    }

    companion object {
        private const val MAX_LOST_TIME: Long = 1e9.toLong()
        private const val PHYS_TICK_AVERAGE_WINDOW_MS = 1000
        private val logger by logger()
    }
}
