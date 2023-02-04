package org.valkyrienskies.core.api.physics.constraints

import org.valkyrienskies.core.api.VSBeta

/**
 * An enum that identifies different types of constraints. Do not use [VSConstraintType] for more than one constraint!
 */
@VSBeta //TODO prob want to remove this (for sure out of api)
enum class VSConstraintType {
    ATTACHMENT, FIXED_ATTACHMENT_ORIENTATION, FIXED_ORIENTATION, HINGE_ORIENTATION, HINGE_SWING_LIMITS,
    HINGE_TARGET_ANGLE, POS_DAMPING, ROPE, ROT_DAMPING, SLIDE, SPHERICAL_SWING_LIMITS, SPHERICAL_TWIST_LIMITS
}
