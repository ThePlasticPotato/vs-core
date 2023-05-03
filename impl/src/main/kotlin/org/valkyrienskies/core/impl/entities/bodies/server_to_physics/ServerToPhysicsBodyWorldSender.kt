package org.valkyrienskies.core.impl.entities.bodies.server_to_physics

import it.unimi.dsi.fastutil.longs.Long2LongMap
import org.valkyrienskies.core.api.bodies.properties.BodyTransform
import org.valkyrienskies.core.api.util.HasId
import org.valkyrienskies.core.impl.entities.bodies.ServerVSBodyImpl
import org.valkyrienskies.core.impl.entities.ObservableProperty
import org.valkyrienskies.core.impl.entities.bodies.physics_to_server.PhysicsToServerBodyReceiver
import org.valkyrienskies.core.impl.entities.world.EntityWorld
import org.valkyrienskies.core.impl.util.cud.WriteableUpdateQueue
import org.valkyrienskies.core.impl.util.fastForEach

/**
 * Collects changes from the server body world into immutable [ServerToPhysicsBodyUpdate]
 * frames and sends them to the physics thread
 *
 * @param updateQueue
 * The update queue this will push updates to
 *
 * @param bodies
 * The server body world to send updates from
 *
 * @param lastTransformUpdate
 * Maps body ID -> the server tick its last transform update was sent.
 * This is used to ignore transform updates for teleported bodies.
 * This must be safe to modify from server tick. This will be mutated
 */
class ServerToPhysicsBodyWorldSender(
    private val updateQueue: WriteableUpdateQueue<ServerToPhysicsBodyWorldUpdate>,
    private val bodies: EntityWorld<ServerVSBodyImpl>,
    private val lastTransformUpdate: Long2LongMap
) {

    /**
     * Called on the server tick. Should be called *after* the [PhysicsToServerBodyReceiver] is ticked
     */
    fun tickServer() {
        listenTransformUpdates()

        updateQueue.update(generateUpdate())
    }

    private fun generateUpdate(): ServerToPhysicsBodyWorldUpdate {
        val newBodies: List<ServerToPhysicsBodyNew> = bodies.entitiesAddedThisTick.values.map { body ->
            ServerToPhysicsBodyNew(
                body.id,
                body.dimension,
                body.transform,
                body.settings
            )
        }

        val updatedBodies = bodies.entities
            .filterNot { bodies.entitiesAddedThisTick.containsKey(it.id) } // exclude entities which were added
            .map { body -> ServerToPhysicsBodyUpdate(body.id, body.transform) } // generate updates

        val deletedBodies = bodies.entitiesRemovedThisTick
        return ServerToPhysicsBodyWorldUpdate(bodies.tickNum, newBodies, updatedBodies, deletedBodies)
    }

    /**
     * Register change listeners on new entities for [onTransformUpdate] and
     * removes stale entries from the [lastTransformUpdate] map
     */
    private fun listenTransformUpdates() {
        bodies.entitiesAddedThisTick.forEach { (_, body) ->
            body.transformProperty.addListener(this::onTransformUpdate)
        }
        bodies.entitiesRemovedThisTick.fastForEach(lastTransformUpdate::remove)
    }

    private fun onTransformUpdate(
        property: ObservableProperty<*>,
        entity: HasId,
        oldValue: BodyTransform,
        newValue: BodyTransform
    ) {
        // This update may not be sent until the next tick, so +1 the tick number
        // todo: is this correct?
        lastTransformUpdate.put(entity.id, bodies.tickNum + 1)
    }

}