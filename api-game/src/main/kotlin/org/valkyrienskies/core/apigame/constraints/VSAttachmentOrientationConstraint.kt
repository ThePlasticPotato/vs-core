package org.valkyrienskies.core.apigame.constraints

import org.joml.Quaterniondc
import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Force two positions of two bodies to be within [fixedDistance] of each other.
 */
data class VSAttachmentOrientationConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localPos0: Vector3dc,
    override val localPos1: Vector3dc,
    override val maxForce: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double
) : VSForceConstraint, VSTorqueConstraint {
    override fun offsetLocalPositions(offset0: Vector3dc, offset1: Vector3dc): VSAttachmentOrientationConstraint {
        return VSAttachmentOrientationConstraint(
            shipId0, shipId1, compliance, localPos0.add(offset0, Vector3d()), localPos1.add(offset1, Vector3d()),
            maxForce, localRot0, localRot1, maxTorque
        )
    }

    override val constraintType: VSConstraintType = VSConstraintType.FIXED_ATTACHMENT_ORIENTATION
}
