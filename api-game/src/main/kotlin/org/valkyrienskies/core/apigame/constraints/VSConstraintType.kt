package org.valkyrienskies.core.apigame.constraints

/**
 * An enum that identifies different types of constraints. Do not use [VSConstraintType] for more than one constraint!
 */
enum class VSConstraintType {
    ATTACHMENT, FIXED_ORIENTATION, HINGE_ORIENTATION, HINGE_SWING_LIMITS, HINGE_TARGET_ANGLE, POS_DAMPING, ROPE,
    ROT_DAMPING, SLIDE, SPHERICAL_SWING_LIMITS, SPHERICAL_TWIST_LIMITS
}
