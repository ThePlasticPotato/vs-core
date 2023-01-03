package org.valkyrienskies.core.api.physics.constraints

import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.POS_DAMPING
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Dampens the relative velocity between [localPos0] and [localPos1] in the world.
 */
@VSBeta
data class PosDampingConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localPos0: Vector3dc,
    override val localPos1: Vector3dc,
    override val maxForce: Double,
    val posDamping: Double
) : VSForceConstraint {
    override fun setLocalPositions(pos0: Vector3dc, pos1: Vector3dc): PosDampingConstraint {
        return PosDampingConstraint(shipId0, shipId1, compliance, pos0, pos1, maxForce, posDamping)
    }

    override val constraintType: VSConstraintType = POS_DAMPING
}
