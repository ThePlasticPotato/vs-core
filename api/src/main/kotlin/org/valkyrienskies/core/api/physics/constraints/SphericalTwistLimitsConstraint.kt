package org.valkyrienskies.core.api.physics.constraints

import org.joml.Quaterniondc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.bodies.properties.BodyId

/**
 * Force the angle of the rotations (with respect to [localRot0] and [localRot1]) along the rotation axis of two bodies
 * to be within the twist limits of [minTwistAngle] to [maxTwistAngle].
 */
@VSBeta
data class SphericalTwistLimitsConstraint(
    override val bodyId0: BodyId,
    override val bodyId1: BodyId,
    override val compliance: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double,
    val minTwistAngle: Double,
    val maxTwistAngle: Double
) : VSTorqueConstraint
