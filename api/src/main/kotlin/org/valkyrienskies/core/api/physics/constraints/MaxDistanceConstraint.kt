package org.valkyrienskies.core.api.physics.constraints

import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.bodies.properties.BodyId

/**
 * Force two positions of two bodies to be within [maxLength].
 */
@VSBeta
data class MaxDistanceConstraint(
    override val bodyId0: BodyId,
    override val bodyId1: BodyId,
    override val compliance: Double,
    override val localPos0: Vector3dc,
    override val localPos1: Vector3dc,
    override val maxForce: Double,
    val maxLength: Double
) : VSForceConstraint {
    override fun withLocalPositions(pos0: Vector3dc, pos1: Vector3dc): MaxDistanceConstraint {
        return MaxDistanceConstraint(
            bodyId0, bodyId1, compliance, pos0, pos1, maxForce, maxLength
        )
    }
    
}
