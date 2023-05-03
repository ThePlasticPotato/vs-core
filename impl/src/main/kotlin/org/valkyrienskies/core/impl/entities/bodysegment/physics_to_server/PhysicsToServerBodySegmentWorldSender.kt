package org.valkyrienskies.core.impl.entities.bodysegment.physics_to_server

import it.unimi.dsi.fastutil.longs.LongArrayList
import org.valkyrienskies.core.api.bodysegments.PhysicsBodySegment
import org.valkyrienskies.core.impl.entities.bodysegment.ImmutableBodySegmentImpl
import org.valkyrienskies.core.impl.entities.world.EntityWorldImpl
import org.valkyrienskies.core.impl.util.cud.UpdateQueue

/**
 * Receives updates from the physics and applies them to the server body segment world
 */
class PhysicsToServerBodySegmentWorldSender(
    /**
     * The queue of updates from the physics
     */
    private val updateQueue: UpdateQueue<PhysicsToServerBodySegmentsUpdate>,
    /**
     * The physics body segment world to send updates from
     */
    private val bodySegments: EntityWorldImpl<PhysicsBodySegment>,
) {

    /**
     * Called on server tick; polls the update queue and applies pending updates
     */
    fun tickPhysics() {
        applyUpdates()
    }

    private fun applyUpdates() {
        // todo this is inefficient
        val newAndUpdated = bodySegments.entities.map(ImmutableBodySegmentImpl::create)
        val removed = LongArrayList(bodySegments.entitiesRemovedThisTick)

        updateQueue.update(PhysicsToServerBodySegmentsUpdate(newAndUpdated, removed))
    }
}