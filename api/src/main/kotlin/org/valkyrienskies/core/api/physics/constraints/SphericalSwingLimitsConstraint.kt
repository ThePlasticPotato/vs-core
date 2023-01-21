package org.valkyrienskies.core.api.physics.constraints

import org.joml.Quaterniondc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.bodies.properties.BodyId

/**
 * Force the angle of the rotation axis (with respect to [localRot0] and [localRot1]) relative to the axis of two bodies
 * to be within the swing limits of [minSwingAngle] to [maxSwingAngle].
 */
@VSBeta
data class SphericalSwingLimitsConstraint(
    override val bodyId0: BodyId,
    override val bodyId1: BodyId,
    override val compliance: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double,
    val minSwingAngle: Double,
    val maxSwingAngle: Double
) : VSTorqueConstraint
