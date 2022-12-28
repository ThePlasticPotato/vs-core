package org.valkyrienskies.core.api.physics.constraints

import org.joml.Vector3d
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
    override fun offsetLocalPositions(offset0: Vector3dc, offset1: Vector3dc): PosDampingConstraint {
        return PosDampingConstraint(
            shipId0, shipId1, compliance, localPos0.add(offset0, Vector3d()), localPos1.add(offset1, Vector3d()),
            maxForce, posDamping
        )
    }

    override val constraintType: VSConstraintType = POS_DAMPING
}
