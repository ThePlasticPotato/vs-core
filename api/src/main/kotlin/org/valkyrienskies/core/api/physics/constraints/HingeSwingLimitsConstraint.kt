package org.valkyrienskies.core.api.physics.constraints

import org.joml.Quaterniondc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.bodies.properties.BodyId

/**
 * Force the angle of rotation (with respect to [localRot0] and [localRot1]) along the hinge axis of two bodies to be
 * within the swing limits of [minSwingAngle] to [maxSwingAngle].
 *
 * @param maxSwingAngle The maximum angle of rotation along the hinge axis in radians.
 * @param minSwingAngle The minimum angle of rotation along the hinge axis in radians.
 */
@VSBeta
data class HingeSwingLimitsConstraint(
    override val bodyId0: BodyId,
    override val bodyId1: BodyId,
    override val compliance: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double,
    val minSwingAngle: Double,
    val maxSwingAngle: Double
) : VSTorqueConstraint
