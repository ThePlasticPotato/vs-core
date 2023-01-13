package org.valkyrienskies.core.apigame.constraints

import org.joml.Quaterniondc
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Force the relative rotation of two bodies to be the same
 */
data class VSFixedOrientationConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double
) : VSTorqueConstraint {
    override val constraintType: VSConstraintType = VSConstraintType.FIXED_ORIENTATION
}
