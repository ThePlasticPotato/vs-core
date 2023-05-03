package org.valkyrienskies.core.impl.entities.bodysegment.physics_to_server

import org.valkyrienskies.core.api.bodysegments.BodySegment
import org.valkyrienskies.core.impl.entities.world.MutableEntityWorld
import org.valkyrienskies.core.impl.util.cud.UpdateQueue
import org.valkyrienskies.core.impl.util.fastForEach

/**
 * Receives updates from the physics and applies them to the server body segment world
 */
class PhysicsToServerBodySegmentWorldReceiver(
    /**
     * The queue of updates from the physics
     */
    private val updateQueue: UpdateQueue<PhysicsToServerBodySegmentsUpdate>,
    /**
     * The server body world to update
     */
    private val bodies: MutableEntityWorld<BodySegment>,
) {

    /**
     * Called on server tick; polls the update queue and applies pending updates
     */
    fun tickServer() {
        applyUpdates()
    }

    private fun applyUpdates() {
        val update = updateQueue.poll() ?: return

        update.newAndUpdated.forEach(bodies::putEntity)
        update.removed.fastForEach(bodies::removeEntity)
    }
}