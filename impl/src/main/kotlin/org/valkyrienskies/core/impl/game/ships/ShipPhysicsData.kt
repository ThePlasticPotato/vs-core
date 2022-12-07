package org.valkyrienskies.core.impl.game.ships

import org.joml.Vector3d
import org.joml.Vector3dc

/**
 * This class keeps track of a ships linear and angular velocity.
 */
data class ShipPhysicsData(
    var linearVelocity: Vector3dc,
    var angularVelocity: Vector3dc
) {
    companion object {
        fun createEmpty(): ShipPhysicsData {
            return ShipPhysicsData(Vector3d(), Vector3d())
        }
    }
}
