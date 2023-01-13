package org.valkyrienskies.core.apigame.constraints

import org.joml.Quaterniondc
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Force the angle of rotation (with respect to [localRot0] and [localRot1]) along the hinge axis of two bodies to be
 * within to be [targetAngle]
 */
data class VSHingeTargetAngleConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double,
    val targetAngle: Double,
    val nextTickTargetAngle: Double
) : VSTorqueConstraint {
    override val constraintType: VSConstraintType = VSConstraintType.HINGE_TARGET_ANGLE
}
