package org.valkyrienskies.core.impl.entities.bodies.server_to_physics

import org.valkyrienskies.core.api.bodies.PhysicsVSBody
import org.valkyrienskies.core.impl.entities.world.EntityWorldImpl
import org.valkyrienskies.core.impl.util.cud.ReadableUpdateQueue
import java.util.function.LongConsumer

/**
 * Receives immutable [ServerToPhysicsBodyUpdate] frames from the server thread
 * and applies them to the supplied world, [bodies]
 *
 * @param updateQueue
 * The update queue this will read updates from
 *
 * @param bodies
 * The physics world this will apply updates to
 *
 * @param lastReceivedUpdateSetter
 * Called with the tick number of the last received update from the server thread
 */
class ServerToPhysicsBodyWorldReceiver(
    private val updateQueue: ReadableUpdateQueue<ServerToPhysicsBodyWorldUpdate>,
    private val bodies: EntityWorldImpl<PhysicsVSBody>,
    private val lastReceivedUpdateSetter: LongConsumer
) {

    /**
     * Called on physics tick; polls the update queue and applies pending updates
     */
    fun tickPhysics() {

    }


    private fun applyUpdates() {
        val update = updateQueue.poll() ?: return


    }

}