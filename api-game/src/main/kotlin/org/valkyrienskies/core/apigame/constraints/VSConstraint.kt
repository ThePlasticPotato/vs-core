package org.valkyrienskies.core.apigame.constraints

import org.valkyrienskies.core.api.ships.properties.ShipId

sealed interface VSConstraint {
    val shipId0: ShipId
    val shipId1: ShipId

    // Inverse of stiffness, a compliance of 0 is an infinitely stiff constraint, however you typically want to use
    // values around 1e-6 to 1e-8 to avoid numerical instability.
    val compliance: Double
    val constraintType: VSConstraintType
}

typealias VSConstraintId = Int
