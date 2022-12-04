package org.valkyrienskies.core.impl.collision

import org.joml.Vector3dc

/**
 * Interface to view the result of a collision test on two [ConvexPolygonc].
 */
interface CollisionResultTimeToCollisionc {
    /**
     * @return whether the polygons are initially colliding or not
     */
    val initiallyColliding: Boolean

    /**
     * Accessing this when [initiallyColliding] is true will throw a [CollidingException].
     *
     * Accessing this when [timeToCollision] is [Double.POSITIVE_INFINITY] will throw a [NeverCollidingException].
     *
     * @return the normal with the largest time to collision
     */
    val collisionAxis: Vector3dc

    /**
     * Accessing this when [initiallyColliding] is true will throw a [CollidingException].
     *
     * @return the time to collision
     */
    val timeToCollision: Double
}
