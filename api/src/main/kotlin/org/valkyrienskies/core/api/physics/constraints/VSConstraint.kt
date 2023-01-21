package org.valkyrienskies.core.api.physics.constraints

import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.bodies.properties.BodyId

@VSBeta
sealed interface VSConstraint {
    val bodyId0: BodyId
    val bodyId1: BodyId

    // Inverse of stiffness, a compliance of 0 is an infinitely stiff constraint, however you typically want to use
    // values around 1e-6 to 1e-8 to avoid numerical instability.
    val compliance: Double
}

typealias VSConstraintId = Long
