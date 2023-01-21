package org.valkyrienskies.core.api.physics.constraints

import org.joml.Quaterniondc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.bodies.properties.BodyId

/**
 * Dampens the relative angular velocity between two bodies.
 */
@VSBeta
data class RotDampingConstraint(
    override val bodyId0: BodyId,
    override val bodyId1: BodyId,
    override val compliance: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double,
    val rotDamping: Double,
    val rotDampingAxes: VSRotDampingAxes
) : VSTorqueConstraint

@VSBeta
enum class VSRotDampingAxes {
    PARALLEL, PERPENDICULAR, ALL_AXES
}
