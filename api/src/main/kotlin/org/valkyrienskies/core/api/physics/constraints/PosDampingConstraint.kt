package org.valkyrienskies.core.api.physics.constraints

import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.bodies.properties.BodyId

/**
 * Dampens the relative velocity between [localPos0] and [localPos1] in the world.
 */
@VSBeta
data class PosDampingConstraint(
    override val bodyId0: BodyId,
    override val bodyId1: BodyId,
    override val compliance: Double,
    override val localPos0: Vector3dc,
    override val localPos1: Vector3dc,
    override val maxForce: Double,
    val posDamping: Double
) : VSForceConstraint {
    override fun withLocalPositions(pos0: Vector3dc, pos1: Vector3dc): PosDampingConstraint {
        return PosDampingConstraint(bodyId0, bodyId1, compliance, pos0, pos1, maxForce, posDamping)
    }
}
