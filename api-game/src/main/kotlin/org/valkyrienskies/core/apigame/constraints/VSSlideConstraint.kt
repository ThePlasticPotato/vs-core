package org.valkyrienskies.core.apigame.constraints

import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Force two positions of two bodies to slide along an axis, with a max distance of [maxDistBetweenPoints] between them.
 *
 * The axis of the slide constraint is set to be (body0.rot * localSlideAxis0)
 *
 * This should always be used with a [HingeOrientationConstraint] to prevent the axis of the slide constraint from
 * changing.
 */
data class VSSlideConstraint(
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
    override fun offsetLocalPositions(offset0: Vector3dc, offset1: Vector3dc): VSSlideConstraint {
        return VSSlideConstraint(
            shipId0, shipId1, compliance, localPos0.add(offset0, Vector3d()), localPos1.add(offset1, Vector3d()),
            maxForce, localSlideAxis0, maxDistBetweenPoints
        )
    }

    override val constraintType: VSConstraintType = VSConstraintType.SLIDE
}
