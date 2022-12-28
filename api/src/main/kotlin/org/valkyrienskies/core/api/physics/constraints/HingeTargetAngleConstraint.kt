package org.valkyrienskies.core.api.physics.constraints

import org.joml.Quaterniondc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.HINGE_TARGET_ANGLE
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Force the angle of rotation (with respect to [localRot0] and [localRot1]) along the hinge axis of two bodies to be
 * within to be [targetAngle]
 *
 * @param targetAngle The angle in radians that the hinge should be at.
 * @param nextTickTargetAngle The angle in radians that the hinge should be at next tick.
 */
@VSBeta
data class HingeTargetAngleConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double,
    val targetAngle: Double,
    val nextTickTargetAngle: Double
) : VSTorqueConstraint {
    override val constraintType: VSConstraintType = HINGE_TARGET_ANGLE
}
