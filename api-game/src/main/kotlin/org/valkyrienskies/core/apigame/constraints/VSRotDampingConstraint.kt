package org.valkyrienskies.core.apigame.constraints

import org.joml.Quaterniondc
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Dampens the relative angular velocity between two bodies.
 */
data class VSRotDampingConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double,
    val rotDamping: Double,
    val rotDampingAxes: VSRotDampingAxes
) : VSTorqueConstraint {
    override val constraintType: VSConstraintType = VSConstraintType.ROT_DAMPING
}

enum class VSRotDampingAxes {
    PARALLEL, PERPENDICULAR, ALL_AXES
}
