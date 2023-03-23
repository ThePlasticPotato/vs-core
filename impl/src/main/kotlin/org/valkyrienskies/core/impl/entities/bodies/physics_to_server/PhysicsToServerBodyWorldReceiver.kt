package org.valkyrienskies.core.impl.entities.bodies.physics_to_server

import it.unimi.dsi.fastutil.longs.Long2LongMap
import it.unimi.dsi.fastutil.longs.Long2LongMaps
import org.valkyrienskies.core.impl.bodies.ServerVSBodyImpl
import org.valkyrienskies.core.impl.entities.EntityWorld
import org.valkyrienskies.core.impl.util.cud.UpdateQueue



/**
 * Receives updates from the physics and applies them to the server body world
 */
class PhysicsToServerBodyWorldReceiver(
    /**
     * The queue of updates from the physics
     */
    private val updateQueue: UpdateQueue<PhysicsToServerBodyWorldUpdate>,
    /**
     * The server body world to update
     */
    private val bodies: EntityWorld<ServerVSBodyImpl>,
    /**
     * Maps body ID -> the server tick its last transform update was sent.
     *
     * This is used to ignore transform updates for teleported bodies
     *
     * This must be safe to access from server tick. This will never be mutated.
     */
    lastTransformUpdate: Long2LongMap
) {

    private val lastTransformUpdate = Long2LongMaps.unmodifiable(lastTransformUpdate)

    /**
     * Called on server tick; polls the update queue and applies pending updates
     */
    fun tickServer() {
        applyUpdates()
    }

    private fun applyUpdates() {
        val update = updateQueue.poll() ?: return

        for (bodyUpdate in update.bodyUpdates) {
            val lastSentUpdate = lastTransformUpdate.getOrDefault(bodyUpdate.id, -1L)

            // If an update hasn't yet been sent OR if the last update was sent on or before the last tick the
            // server received, then apply it -- otherwise ignore.
            if (lastSentUpdate == -1L || lastSentUpdate <= update.lastReceivedServerTick) {
                val body = bodies.getEntity(bodyUpdate.id) ?: continue
                applyUpdate(body, bodyUpdate)
            }
        }
    }

    private fun applyUpdate(body: ServerVSBodyImpl, update: PhysicsToServerBodyUpdate) {
        TODO()
    }
}