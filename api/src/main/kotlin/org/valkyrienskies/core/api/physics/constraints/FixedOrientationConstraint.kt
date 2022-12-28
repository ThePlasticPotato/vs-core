package org.valkyrienskies.core.api.physics.constraints

import org.joml.Quaterniondc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.FIXED_ORIENTATION
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Force the relative rotation of two bodies to be the same
 */
@VSBeta
data class FixedOrientationConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double
) : VSTorqueConstraint {
    override val constraintType: VSConstraintType = FIXED_ORIENTATION
}
