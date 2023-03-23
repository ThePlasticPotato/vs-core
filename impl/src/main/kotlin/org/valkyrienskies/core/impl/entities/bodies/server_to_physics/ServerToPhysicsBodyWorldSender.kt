package org.valkyrienskies.core.impl.entities.bodies.server_to_physics

import it.unimi.dsi.fastutil.longs.Long2LongMap
import org.valkyrienskies.core.api.bodies.properties.BodyTransform
import org.valkyrienskies.core.api.util.HasId
import org.valkyrienskies.core.impl.bodies.ServerVSBodyImpl
import org.valkyrienskies.core.impl.entities.EntityWorld
import org.valkyrienskies.core.impl.entities.ObservableProperty
import org.valkyrienskies.core.impl.util.cud.UpdateQueue
import org.valkyrienskies.core.impl.util.fastForEach

/**
 * Collects changes from the server body world into immutable update frames and sends them to the physics
 */
class ServerToPhysicsBodyWorldSender(
    /**
     * The update queue to push updates to
     */
    private val updateQueue: UpdateQueue<ServerToPhysicsBodyWorldUpdate>,
    /**
     * The server body world to send updates from
     */
    private val bodies: EntityWorld<ServerVSBodyImpl>,
    /**
     * Maps body ID -> the server tick its last transform update was sent.
     *
     * This is used to ignore transform updates for teleported bodies
     *
     * This must be safe to modify from server tick. This will be mutated
     */
    private val lastTransformUpdate: Long2LongMap
) {

    fun tickServer() {
        listenTransformUpdates()

    }

    private fun generateUpdate(): ServerToPhysicsBodyWorldUpdate {
//        val newBodies: List<ServerToPhysicsBodyNew> = bodies.entitiesAddedThisTick.values.map { body ->
//            ServerToPhysicsBodyNew(
//                body.id,
//                body.dimension,
//                body.transform
//                body.shape,
//                body.settings,
//                body.)
//        }
//        val update = ServerToPhysicsBodyWorldUpdate(bodies.tickNum, )
        TODO()
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