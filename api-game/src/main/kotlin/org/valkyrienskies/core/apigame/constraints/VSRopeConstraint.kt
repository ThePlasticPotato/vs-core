package org.valkyrienskies.core.apigame.constraints

import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Force two positions of two bodies to be within [ropeLength].
 */
data class VSRopeConstraint(
    override val shipId0: ShipId,
    override val shipId1: ShipId,
    override val compliance: Double,
    override val localPos0: Vector3dc,
    override val localPos1: Vector3dc,
    override val maxForce: Double,
    // The length of the rope, when the points are within this distance this rope will be slack, and will not apply
    // any force
    val ropeLength: Double
) : VSForceConstraint {
    override fun offsetLocalPositions(offset0: Vector3dc, offset1: Vector3dc): VSRopeConstraint {
        return VSRopeConstraint(
            shipId0, shipId1, compliance, localPos0.add(offset0, Vector3d()), localPos1.add(offset1, Vector3d()),
            maxForce, ropeLength
        )
    }

    override fun scaleLocalPositions(scale0: Double, scale1: Double): VSRopeConstraint {
        return VSRopeConstraint(
            shipId0, shipId1, compliance, localPos0.mul(scale0, Vector3d()), localPos1.mul(scale1, Vector3d()),
            maxForce, ropeLength
        )
    }

    override val constraintType: VSConstraintType = VSConstraintType.ROPE
}
