package org.valkyrienskies.core.api.physics.constraints

import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta

/**
 * A constraint that applies a force on two bodies.
 */
@VSBeta
interface VSForceConstraint : VSConstraint {
    // The local position of the constraint in body0
    val localPos0: Vector3dc

    // The local position of the constraint in body1
    val localPos1: Vector3dc

    // The maximum force this constraint can tolerate before it breaks
    val maxForce: Double

    fun withLocalPositions(pos0: Vector3dc, pos1: Vector3dc): VSConstraint

    @Deprecated("renamed", ReplaceWith("withLocalPositions"))
    fun setLocalPositions(pos0: Vector3dc, pos1: Vector3dc): VSConstraint = withLocalPositions(pos0, pos1)
}
