package org.valkyrienskies.core.apigame.constraints

import org.joml.Quaterniondc
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Force the first axis of rotation (with respect to [localRot0] and [localRot1]) of two bodies to be the same.
 *
 * The result is the two bodies will be forced to rotate along a hinge.
 */
data class VSHingeOrientationConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localRot0: Quaterniondc,
    override val localRot1: Quaterniondc,
    override val maxTorque: Double
) : VSTorqueConstraint {
    override val constraintType: VSConstraintType = VSConstraintType.HINGE_ORIENTATION
}
