package org.valkyrienskies.core.api.physics.constraints

import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.physics.constraints.VSConstraintType.SLIDE
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Force two positions of two bodies to slide along an axis, with a max distance of [maxDistBetweenPoints] between them.
 *
 * The axis of the slide constraint is set to be (body0.rot * localSlideAxis0)
 *
 * This should always be used with a [HingeOrientationConstraint] to prevent the axis of the slide constraint from
 * changing.
 */
@VSBeta
data class SlideConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localPos0: Vector3dc,
    override val localPos1: Vector3dc,
    override val maxForce: Double,
    // The axis of the slide constraint with respect to body0
    val localSlideAxis0: Vector3dc,
    // The max distance the two points can differ along the slide axis
    val maxDistBetweenPoints: Double
) : VSForceConstraint {
    override fun setLocalPositions(pos0: Vector3dc, pos1: Vector3dc): SlideConstraint {
        return SlideConstraint(
            shipId0, shipId1, compliance, pos0, pos1, maxForce, localSlideAxis0, maxDistBetweenPoints
        )
    }

    override val constraintType: VSConstraintType = SLIDE
}
