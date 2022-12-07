package org.valkyrienskies.core.impl.collision

import kotlin.math.abs

/**
 * An immutable view of [CollisionRange].
 */
interface CollisionRangec {
    val min: Double
    val max: Double

    companion object {
        /**
         * @return The offset we must move [collisionRange1] such that it is no longer overlapping with [collisionRange2]. If [collisionRange1] and [collisionRange2] are not overlapping then this returns 0.
         */
        fun computeCollisionResponse(
            collisionRange1: CollisionRangec,
            collisionRange2: CollisionRangec
        ): Double {
            val pushLeft = -collisionRange1.max + collisionRange2.min
            val pushRight = -collisionRange1.min + collisionRange2.max

            return if (pushRight <= 0 || pushLeft >= 0) {
                // Not overlapping
                0.0
            } else if (abs(pushRight) < abs(pushLeft)) {
                // Its more efficient to push [collisionRange1] right
                pushRight
            } else {
                // Its more efficient to push [collisionRange1] left
                pushLeft
            }
        }

        /**
         * @return The offset we must move [collisionRange1] such that it is no longer overlapping with [collisionRange2]. If [collisionRange1] and [collisionRange2] are not overlapping then this returns 0.
         */
        fun computeCollisionResponseGivenVelocity(
            collisionRange1: CollisionRangec,
            collisionRange2: CollisionRangec,
            collisionRange1Velocity: Double
        ): Double {
            // Assume that [collisionRange1] is moving with vel [collisionRange1Velocity]
            //
            // Return the collision response to apply to [collisionRange1] after it has moved by [collisionRange1Velocity],
            // being careful not to push [collisionRange1] to the opposite side of [collisionRange2] where it was initially
            val pushLeftOriginal = -collisionRange1.max + collisionRange2.min
            val pushRightOriginal = -collisionRange1.min + collisionRange2.max

            if (abs(pushRightOriginal) < abs(pushLeftOriginal)) {
                // [collisionRange1] is initially to the right of [collisionRange2], so only push to the right
                val collisionResponse = pushRightOriginal - collisionRange1Velocity
                return if (collisionResponse > 0.0)
                    collisionResponse
                else
                // [collisionRange1] is not colliding after it moved by [collisionRange1Velocity]
                    0.0
            } else {
                // [collisionRange1] is initially to the left of [collisionRange2], so only push to the left
                val collisionResponse = pushLeftOriginal - collisionRange1Velocity
                return if (collisionResponse < 0.0)
                    collisionResponse
                else
                // [collisionRange1] is not colliding after it moved by [collisionRange1Velocity]
                    0.0
            }
        }

        /**
         * @return The time when [collisionRange1] will collide with [collisionRange2] given [collisionRange1Velocity].
         *         If they are initially overlapping then we return 0.0, if they will never overlap we return
         *         [Double.POSITIVE_INFINITY]
         */
        fun computeCollisionTime(
            collisionRange1: CollisionRangec,
            collisionRange2: CollisionRangec,
            collisionRange1Velocity: Double
        ): Double {
            val pushLeft = -collisionRange1.max + collisionRange2.min
            val pushRight = -collisionRange1.min + collisionRange2.max

            return if (pushRight <= 0 || pushLeft >= 0) {
                // If vel is too small then assume that it will take infinite time to collide
                if (abs(collisionRange1Velocity) < 1e-8) return Double.POSITIVE_INFINITY

                // Compute time before pushRight is > 0 or pushLeft < 0
                if (collisionRange1Velocity > 0) {
                    if (pushLeft >= 0) abs(pushLeft / collisionRange1Velocity) // Add abs() so we don't return -0.0
                    else Double.POSITIVE_INFINITY
                } else {
                    if (pushRight <= 0) abs(pushRight / collisionRange1Velocity) // Add abs() so we don't return -0.0
                    else Double.POSITIVE_INFINITY
                }
            } else {
                // Already overlapping, so time of impact is 0
                0.0
            }
        }
    }
}
