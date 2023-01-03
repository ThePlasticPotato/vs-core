package org.valkyrienskies.core.api.physics.constraints

import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.ATTACHMENT
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Force two positions of two bodies to be within [fixedDistance] of each other.
 */

@VSBeta
data class AttachmentConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localPos0: Vector3dc,
    override val localPos1: Vector3dc,
    override val maxForce: Double,
    val fixedDistance: Double
) : VSForceConstraint {
    override fun setLocalPositions(pos0: Vector3dc, pos1: Vector3dc): AttachmentConstraint {
        return AttachmentConstraint(
            shipId0, shipId1, compliance, pos0, pos1, maxForce, fixedDistance
        )
    }

    override val constraintType: VSConstraintType = ATTACHMENT
}
