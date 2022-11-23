package org.valkyrienskies.core.pipelines

import org.valkyrienskies.core.config.VSCoreConfig
import org.valkyrienskies.core.pipelines.VSGamePipelineStage.Companion.GAME_TPS
import org.valkyrienskies.core.util.ThreadHints
import org.valkyrienskies.core.util.logger
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.min
import kotlin.system.measureNanoTime

class VSPhysicsPipelineBackgroundTask(
    private val vsPipeline: VSPipelineImpl, private var idealPhysicsTps: Int = 60
) :
    Runnable {
    // When this is set to true, this task will kill itself at the next opportunity
    private var killTask = false

    private var lostTime: Long = 0
    private val prevPhysTicksTimeMillis: Queue<Long> = LinkedList()

    @Volatile
    var physicsTicksSinceLastGameTick = 0

    val syncLock = ReentrantLock()
    val shouldRunGameTick = syncLock.newCondition()
    val shouldRunPhysicsTick = syncLock.newCondition()

    val pauseLock = ReentrantLock()
    val shouldUnpausePhysicsTick = pauseLock.newCondition()

    override fun run() {
        try {
            while (true) {
                if (killTask) break // Stop looping

                if (vsPipeline.synchronizePhysics) {
                    val physicsTicksPerGameTick = VSCoreConfig.SERVER.pt.physicsTicksPerGameTick
                    val timeStep = 1.0 / (GAME_TPS * physicsTicksPerGameTick)
                    syncLock.withLock {
                        // in the event we've done all 3 physics ticks for this tick, wait for game tick to run,
                        // set the physicsTicksSinceLastGameTick to 0, and signal
                        while (physicsTicksSinceLastGameTick >= physicsTicksPerGameTick)
                            shouldRunPhysicsTick.await()

                        vsPipeline.tickPhysics(vsPipeline.getPhysicsGravity(), timeStep)
                        physicsTicksSinceLastGameTick++

                        // if this is our final physics tick for this game tick,
                        // signal the game thread
                        if (physicsTicksSinceLastGameTick >= physicsTicksPerGameTick) {
                            shouldRunGameTick.signal()
                        }
                    }
                } else {
                    // If paused, wait until not paused
                    if (!vsPipeline.arePhysicsRunning) {
                        // reset timing stuff
                        lostTime = 0
                        prevPhysTicksTimeMillis.clear()

                        pauseLock.withLock {
                            while (!vsPipeline.arePhysicsRunning)
                                shouldUnpausePhysicsTick.await()
                        }
                    }

                    val timeToSimulateNs = 1e9 / idealPhysicsTps.toDouble()
                    val timeStep = timeToSimulateNs / 1e9

                    val timeToRunPhysTick = measureNanoTime {
                        // Run the physics tick
                        vsPipeline.tickPhysics(vsPipeline.getPhysicsGravity(), timeStep)
                    }

                    trackTickEnd()
                    sleepOnLostTime(timeToSimulateNs, timeToRunPhysTick)
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

    private fun trackTickEnd() {
        // Keep track of when physics tick finished
        val currentTimeMillis = System.currentTimeMillis()
        prevPhysTicksTimeMillis.add(currentTimeMillis)
        // Remove physics ticks that were over [PHYS_TICK_AVERAGE_WINDOW_MS] ms ago
        while (prevPhysTicksTimeMillis.isNotEmpty() &&
            prevPhysTicksTimeMillis.peek() + PHYS_TICK_AVERAGE_WINDOW_MS < currentTimeMillis
        ) {
            prevPhysTicksTimeMillis.remove()
        }
    }

    private fun sleepOnLostTime(timeToSimulateNs: Double, timeToRunPhysTick: Long) {
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
                sleepExact(timeToWait.toLong())
            }
        }
    }

    private fun sleepExact(sleepTimeNanos: Long) {
        val startTime = System.nanoTime()
        val timeToSleep = sleepTimeNanos - 1_000_000

        LockSupport.parkNanos(timeToSleep)

        while (System.nanoTime() - startTime < sleepTimeNanos) {
            ThreadHints.onSpinWait()
        }
    }

    fun computePhysicsTPS(): Double {
        return prevPhysTicksTimeMillis.size.toDouble() / (PHYS_TICK_AVERAGE_WINDOW_MS / 1000.0)
    }

    companion object {
        private const val MAX_LOST_TIME: Long = 1e9.toLong()
        private const val PHYS_TICK_AVERAGE_WINDOW_MS = 5000
        private val logger by logger()
    }
}
