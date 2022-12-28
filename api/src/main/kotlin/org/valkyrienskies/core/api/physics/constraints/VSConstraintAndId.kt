package org.valkyrienskies.core.api.physics.constraints

import org.valkyrienskies.core.api.VSBeta

@VSBeta
data class VSConstraintAndId(
    val constraintId: VSConstraintId,
    val vsConstraint: VSConstraint
)
