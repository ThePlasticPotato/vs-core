package org.valkyrienskies.core.api.physics.constraints

import org.joml.Quaterniondc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.ROT_DAMPING
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Dampens the relative angular velocity between two bodies.
 */
@VSBeta
data class RotDampingConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double,
    val rotDamping: Double,
    val rotDampingAxes: VSRotDampingAxes
) : VSTorqueConstraint {
    override val constraintType: VSConstraintType = ROT_DAMPING
}

@VSBeta
enum class VSRotDampingAxes {
    PARALLEL, PERPENDICULAR, ALL_AXES
}
