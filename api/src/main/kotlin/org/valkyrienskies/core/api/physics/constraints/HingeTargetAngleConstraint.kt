package org.valkyrienskies.core.api.physics.constraints

import org.joml.Quaterniondc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.bodies.properties.BodyId

/**
 * Force the angle of rotation (with respect to [localRot0] and [localRot1]) along the hinge axis of two bodies to be
 * within to be [targetAngle]
 *
 * @param targetAngle The angle in radians that the hinge should be at.
 * @param nextTickTargetAngle The angle in radians that the hinge should be at next tick.
 */
@VSBeta
data class HingeTargetAngleConstraint(
    override val bodyId0: BodyId,
    override val bodyId1: BodyId,
    override val compliance: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double,
    val targetAngle: Double,
    val nextTickTargetAngle: Double
) : VSTorqueConstraint
