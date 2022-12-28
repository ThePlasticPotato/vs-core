package org.valkyrienskies.core.api.physics.constraints

import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.ROPE
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Force two positions of two bodies to be within [maxLength].
 */
@VSBeta
data class MaxDistanceConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localPos0: Vector3dc,
    override val localPos1: Vector3dc,
    override val maxForce: Double,
    val maxLength: Double
) : VSForceConstraint {
    override fun offsetLocalPositions(offset0: Vector3dc, offset1: Vector3dc): MaxDistanceConstraint {
        return MaxDistanceConstraint(
            shipId0, shipId1, compliance, localPos0.add(offset0, Vector3d()), localPos1.add(offset1, Vector3d()),
            maxForce, maxLength
        )
    }

    override val constraintType: VSConstraintType = ROPE
}
