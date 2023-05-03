package org.valkyrienskies.core.impl.entities.bodies.physics_to_server

import org.valkyrienskies.core.api.bodies.PhysicsVSBody
import org.valkyrienskies.core.impl.entities.world.EntityWorld
import org.valkyrienskies.core.impl.util.cud.WriteableUpdateQueue
import java.util.function.LongSupplier

/**
 * Sends updates from the physics thread to the server thread
 *
 * @param updateQueue
 * The update queue to push updates into
 *
 * @param physicsBodies
 * The physics world
 *
 * @param lastReceivedUpdateSupplier
 * Supplies the last tick received by the game thread from the physics thread.
 * Must be safe to access from the physics thread.
 */
class PhysicsToServerBodySender(
    private val updateQueue: WriteableUpdateQueue<PhysicsToServerBodyWorldUpdate>,
    private val physicsBodies: EntityWorld<PhysicsVSBody>,
    private val lastReceivedUpdateSupplier: LongSupplier
) {

    fun tickPhysics() {
        updateQueue.update(generateUpdate())
    }

    private fun generateUpdate(): PhysicsToServerBodyWorldUpdate {
        val lastReceivedUpdate = lastReceivedUpdateSupplier.asLong
        val updates = physicsBodies.entities.map(::convertToUpdate)

        return PhysicsToServerBodyWorldUpdate(lastReceivedUpdate, updates)
    }

    private fun convertToUpdate(body: PhysicsVSBody): PhysicsToServerBodyUpdate =
        PhysicsToServerBodyUpdate(body.id, body.transform, body.aabb)

}