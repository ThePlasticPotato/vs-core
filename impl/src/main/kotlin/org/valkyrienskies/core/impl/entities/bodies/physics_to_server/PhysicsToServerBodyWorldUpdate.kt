package org.valkyrienskies.core.impl.entities.bodies.physics_to_server

import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyTransform

/**
 * Update frame sent from the physics to the server thread every tick
 */
class PhysicsToServerBodyWorldUpdate(
    /**
     * The last tick the physics received from the server, used by the server to ignore transform updates
     * to bodies that were teleported recently
     */
    val lastReceivedServerTick: Long,
    val bodyUpdates: List<PhysicsToServerBodyUpdate>
)

class PhysicsToServerBodyUpdate(
    val id: BodyId,
    val transform: BodyTransform,
    val velocity: Vector3dc,
    val omega: Vector3dc,
    /**
     * AABB in model space
     */
    val aabb: AABBdc
)