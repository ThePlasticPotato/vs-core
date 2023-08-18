package org.valkyrienskies.core.apigame.constraints

import org.joml.Vector3dc

/**
 * A constraint that applies a force on two bodies.
 */
interface VSForceConstraint : VSConstraint {
    // The local position of the constraint in body0
    val localPos0: Vector3dc

    // The local position of the constraint in body1
    val localPos1: Vector3dc

    // The maximum force this constraint can tolerate before it breaks
    val maxForce: Double

    fun offsetLocalPositions(offset0: Vector3dc, offset1: Vector3dc): VSForceConstraint

    fun scaleLocalPositions(scale0: Double, scale1: Double): VSForceConstraint
}
