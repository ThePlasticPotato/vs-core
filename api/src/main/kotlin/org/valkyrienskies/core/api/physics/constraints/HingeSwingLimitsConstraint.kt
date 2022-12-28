package org.valkyrienskies.core.api.physics.constraints

import org.joml.Quaterniondc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.HINGE_SWING_LIMITS
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Force the angle of rotation (with respect to [localRot0] and [localRot1]) along the hinge axis of two bodies to be
 * within the swing limits of [minSwingAngle] to [maxSwingAngle].
 *
 * @param maxSwingAngle The maximum angle of rotation along the hinge axis in radians.
 * @param minSwingAngle The minimum angle of rotation along the hinge axis in radians.
 */
@VSBeta
data class HingeSwingLimitsConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double,
    val minSwingAngle: Double,
    val maxSwingAngle: Double
) : VSTorqueConstraint {
    override val constraintType: VSConstraintType = HINGE_SWING_LIMITS
}
