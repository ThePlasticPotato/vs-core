package org.valkyrienskies.core.apigame.constraints

import org.joml.Quaterniondc
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Force the angle of the rotations (with respect to [localRot0] and [localRot1]) along the rotation axis of two bodies
 * to be within the twist limits of [minTwistAngle] to [maxTwistAngle].
 */
data class VSSphericalTwistLimitsConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double,
    val minTwistAngle: Double,
    val maxTwistAngle: Double
) : VSTorqueConstraint {
    override val constraintType: VSConstraintType = VSConstraintType.SPHERICAL_TWIST_LIMITS
}
