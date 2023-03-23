package org.valkyrienskies.core.api.physics.constraints

import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.util.HasId

@VSBeta
data class VSConstraintAndId(
    override val id: VSConstraintId,
    val vsConstraint: VSConstraint
) : VSConstraint by vsConstraint, HasId {
    val constraintId: VSConstraintId get() = id
}
