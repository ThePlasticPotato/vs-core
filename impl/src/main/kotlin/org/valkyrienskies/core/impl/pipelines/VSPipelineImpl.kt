package org.valkyrienskies.core.impl.pipelines

import dagger.Subcomponent
import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.apigame.world.VSPipeline
import org.valkyrienskies.core.impl.config.VSCoreConfig
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld
import org.valkyrienskies.core.impl.game.ships.modules.ShipWorldModule
import org.valkyrienskies.core.impl.hooks.CoreHooksImpl
import org.valkyrienskies.core.impl.util.WorldScoped
import javax.inject.Inject
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

@WorldScoped
@Subcomponent(modules = [ShipWorldModule::class, ShipWorldModule.Declarations::class])
interface VSPipelineComponent {
    fun newPipeline(): VSPipelineImpl

    @Subcomponent.Factory
    interface Factory {
        fun newPipelineComponent(module: ShipWorldModule): VSPipelineComponent
    }
}

/**
 * A pipeline that moves data between the game, the physics, and the network stages.
 *
 * The Game stage sends [VSGameFrame]s to the Physics stage.
 *
 * The Physics stage sends [VSPhysicsFrame]s to the Game stage and to the Network stage.
 *
 * Game <--> Physics --> Network
 */
@WorldScoped
class VSPipelineImpl @Inject constructor(
    override val shipWorld: ShipObjectServerWorld,
    private val gameStage: VSGamePipelineStage,
    private var physicsStage: VSPhysicsPipelineStage?,
    private val networkStage: VSNetworkPipelineStage,
    hooks: CoreHooksImpl,
) : VSPipeline {
    @Volatile
    var synchronizePhysics = VSCoreConfig.SERVER.pt.synchronizePhysics
        private set

    private val physicsPipelineBackgroundTask: VSPhysicsPipelineBackgroundTask = VSPhysicsPipelineBackgroundTask(this)

    // start paused on client, start unpaused on server
    @Volatile
    override var arePhysicsRunning = !hooks.isPhysicalClient
        set(value) {
            field = value

            if (value) {
                physicsPipelineBackgroundTask.pauseLock.withLock {
                    physicsPipelineBackgroundTask.shouldUnpausePhysicsTick.signal()
                }
            }
        }

    // The thread the physics engine runs on
    private val physicsThread: Thread = thread(start = true, priority = 8, name = "Physics thread") {
        physicsPipelineBackgroundTask.run()
    }

    override var deleteResources = false

    override val isUsingDummyPhysics get() = physicsStage!!.isUsingDummy

    override fun preTickGame() {
        val prevSynchronizePhysics = synchronizePhysics
        synchronizePhysics = VSCoreConfig.SERVER.pt.synchronizePhysics

        if (prevSynchronizePhysics) {
            physicsPipelineBackgroundTask.syncLock.withLock {
                // indicate the game tick has completed and signal the physics thread
                physicsPipelineBackgroundTask.physicsTicksSinceLastGameTick = 0
                physicsPipelineBackgroundTask.shouldRunPhysicsTick.signal()
            }
        }

        gameStage.preTickGame()
    }

    override fun postTickGame() {
        if (synchronizePhysics) {
            physicsPipelineBackgroundTask.syncLock.withLock {
                val physicsTicksPerGameTick = VSCoreConfig.SERVER.pt.physicsTicksPerGameTick

                // in the event the physics thread hasn't produced all the required ticks yet, wait until it does
                while (physicsPipelineBackgroundTask.physicsTicksSinceLastGameTick < physicsTicksPerGameTick)
                    physicsPipelineBackgroundTask.shouldRunGameTick.await()
            }
        }

        val gameFrame = gameStage.postTickGame()
        physicsStage!!.pushGameFrame(gameFrame)
    }

    fun deletePhysicsResources() {
        println("Running deletePhysicsResources")
        if (deleteResources) {
            // Do this to turn the physics back on, so it can delete itself
            arePhysicsRunning = true
            physicsStage!!.deleteResources()
            // Force this to be gc'd
            physicsStage = null
            physicsPipelineBackgroundTask.tellTaskToKillItself()
        }
    }

    fun tickPhysics(gravity: Vector3dc, timeStep: Double) {
        val physicsFrame = physicsStage!!.tickPhysics(gravity, timeStep, true)
        gameStage.pushPhysicsFrame(physicsFrame)
        networkStage.pushPhysicsFrame(physicsFrame)
    }

    fun getPhysicsGravity(): Vector3dc {
        return Vector3d(0.0, -10.0, 0.0)
    }

    override fun computePhysTps(): Double {
        return physicsPipelineBackgroundTask.computePhysicsTPS()
    }
}
