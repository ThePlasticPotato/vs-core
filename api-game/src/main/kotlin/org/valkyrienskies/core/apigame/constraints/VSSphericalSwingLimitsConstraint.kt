package org.valkyrienskies.core.apigame.constraints

import org.joml.Quaterniondc
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Force the angle of the rotation axis (with respect to [localRot0] and [localRot1]) relative to the axis of two bodies
 * to be within the swing limits of [minSwingAngle] to [maxSwingAngle].
 */
data class VSSphericalSwingLimitsConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double,
    val minSwingAngle: Double,
    val maxSwingAngle: Double
) : VSTorqueConstraint {
    override val constraintType: VSConstraintType = VSConstraintType.SPHERICAL_SWING_LIMITS
}
