package org.valkyrienskies.core.apigame.constraints

import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Force two positions of two bodies to be within [fixedDistance] of each other.
 */
data class VSAttachmentConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localPos0: Vector3dc,
    override val localPos1: Vector3dc,
    override val maxForce: Double,
    val fixedDistance: Double
) : VSForceConstraint {
    override fun offsetLocalPositions(offset0: Vector3dc, offset1: Vector3dc): VSAttachmentConstraint {
        return VSAttachmentConstraint(
            shipId0, shipId1, compliance, localPos0.add(offset0, Vector3d()), localPos1.add(offset1, Vector3d()),
            maxForce, fixedDistance
        )
    }

    override fun scaleLocalPositions(scale0: Double, scale1: Double): VSForceConstraint {
        return VSAttachmentConstraint(
            shipId0, shipId1, compliance, localPos0.mul(scale0, Vector3d()), localPos1.mul(scale1, Vector3d()),
            maxForce, fixedDistance
        )
    }

    override val constraintType: VSConstraintType = VSConstraintType.ATTACHMENT
}
