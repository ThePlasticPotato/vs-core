package org.valkyrienskies.core.collision

import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.util.horizontalLength
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.tan

/**
 * A basic implementation of [ConvexPolygonCollider] using the Separating Axis Theorem algorithm.
 */
object SATConvexPolygonCollider : ConvexPolygonCollider {
    override fun checkIfColliding(
        firstPolygon: ConvexPolygonc,
        secondPolygon: ConvexPolygonc,
        normals: Iterator<Vector3dc>,
        collisionResult: CollisionResult,
        temp1: CollisionRange,
        temp2: CollisionRange,
        forcedResponseNormal: Vector3dc?
    ) {
        var minCollisionDepth = Double.MAX_VALUE
        collisionResult._colliding = true // Initially assume that polygons are collided

        for (normal in normals) {
            // Calculate the overlapping range of the projection of both polygons along the [normal] axis
            val rangeOverlapResponse =
                computeCollisionResponseAlongNormal(
                    firstPolygon, secondPolygon, normal, temp1, temp2
                )

            if (abs(rangeOverlapResponse) < 1.0e-6) {
                // Polygons are separated along [normal], therefore they are NOT colliding
                collisionResult._colliding = false
                return
            } else {
                if (forcedResponseNormal != null) {
                    val dotProduct = forcedResponseNormal.dot(normal)
                    if (abs(dotProduct) < 1e-6) continue // Skip
                    val modifiedRangeOverlapResponse = rangeOverlapResponse / dotProduct

                    // Polygons are colliding along this axis, doesn't guarantee if the polygons are colliding or not
                    val collisionDepth = abs(modifiedRangeOverlapResponse)
                    if (collisionDepth < minCollisionDepth) {
                        minCollisionDepth = collisionDepth
                        collisionResult._collisionAxis.set(forcedResponseNormal)
                        collisionResult._penetrationOffset = modifiedRangeOverlapResponse
                    }
                } else {
                    // Polygons are colliding along this axis, doesn't guarantee if the polygons are colliding or not
                    val collisionDepth = abs(rangeOverlapResponse)
                    if (collisionDepth < minCollisionDepth) {
                        minCollisionDepth = collisionDepth
                        collisionResult._collisionAxis.set(normal)
                        collisionResult._penetrationOffset = rangeOverlapResponse
                    }
                }
            }
        }

        if (minCollisionDepth == Double.MAX_VALUE) collisionResult._colliding = false
    }

    override fun computeResponseMinimizingChangesToVel(
        firstPolygon: ConvexPolygonc,
        firstPolygonVel: Vector3dc,
        secondPolygon: ConvexPolygonc,
        normals: Iterator<Vector3dc>,
        temp1: CollisionRange,
        temp2: CollisionRange,
        maxSlopeClimbAngle: Double,
        forcedResponseNormalFromCaller: Vector3dc?
    ): Vector3dc {
        var newFirstPolygonVel: Vector3dc? = null

        for (normal in normals) {
            // Calculate the overlapping range of the projection of both polygons along the [normal] axis
            val collisionResponseAlongNormal =
                computeCollisionResponseAlongNormalWithVel(
                    firstPolygon, firstPolygonVel, secondPolygon, normal, temp1, temp2
                )

            // Not colliding, even with the velocity
            if (collisionResponseAlongNormal == 0.0)
                return firstPolygonVel

            var potentialNewVel: Vector3dc? = null
            if (forcedResponseNormalFromCaller != null) {
                // Player is trying to climb a very steep slope, in this case don't change the y, only change horizontal components
                // Adjust the collision vector such that the y-component is 0
                val dotProduct = normal.dot(forcedResponseNormalFromCaller)
                // Avoid divide by zero errors
                if (abs(dotProduct) < 1e-6) continue
                val newCollisionResponse = Vector3d(
                    collisionResponseAlongNormal * forcedResponseNormalFromCaller.x() / dotProduct,
                    collisionResponseAlongNormal * forcedResponseNormalFromCaller.y() / dotProduct,
                    collisionResponseAlongNormal * forcedResponseNormalFromCaller.z() / dotProduct
                )
                potentialNewVel = firstPolygonVel.add(newCollisionResponse, Vector3d())
            } else {
                // Polygons are colliding along this axis, doesn't guarantee if the polygons are colliding or not
                if (abs(normal.y()) >= sin(Math.toRadians(maxSlopeClimbAngle))) {
                    // Adjust the collision vector such that the player can climb up slopes
                    // So adjust the y value such that the horizontal components are not changed.
                    val newCollisionResponse = Vector3d(0.0, collisionResponseAlongNormal / normal.y(), 0.0)
                    potentialNewVel = firstPolygonVel.add(newCollisionResponse, Vector3d())
                    val potentialNewVelHorizontalComponent = potentialNewVel.horizontalLength()
                    if (potentialNewVelHorizontalComponent > 1e-6) {
                        // TODO: This logic is only valid if we aren't initially colliding with the block!
                        // Check if the climb slope is less steep than [maxSlopeClimbAngle]
                        val climbSlope = abs(potentialNewVel.y()) / potentialNewVelHorizontalComponent
                        if (climbSlope >= tan(Math.toRadians(maxSlopeClimbAngle)))
                            potentialNewVel = null // The climb is too steep
                    }
                }
                if (potentialNewVel == null) {
                    // Player is trying to climb a very steep slope, in this case don't change the y, only change horizontal components
                    // Adjust the collision vector such that the y-component is 0
                    val forcedResponseNormal = Vector3d(normal.x(), 0.0, normal.z()).normalize()
                    if (forcedResponseNormal.isFinite) {
                        val dotProduct = normal.dot(forcedResponseNormal)
                        val newCollisionResponse = Vector3d(
                            collisionResponseAlongNormal * forcedResponseNormal.x() / dotProduct, 0.0,
                            collisionResponseAlongNormal * forcedResponseNormal.z() / dotProduct
                        )
                        potentialNewVel = firstPolygonVel.add(newCollisionResponse, Vector3d())
                    } else {
                        // Skip
                        continue
                    }
                }
            }
            if (newFirstPolygonVel == null) {
                newFirstPolygonVel = potentialNewVel
            } else {
                val potentialNewVelDif = Vector3d(potentialNewVel!!).sub(firstPolygonVel)
                val newFirstPolygonVelDif = Vector3d(newFirstPolygonVel).sub(firstPolygonVel)

                // Add the response
                if (potentialNewVelDif.lengthSquared() < newFirstPolygonVelDif.lengthSquared())
                    newFirstPolygonVel = potentialNewVel
            }
        }

        return newFirstPolygonVel!!
    }

    override fun computeResponseMinimizingChangesToVelHorOnly(
        firstPolygon: ConvexPolygonc,
        firstPolygonVel: Vector3dc,
        secondPolygon: ConvexPolygonc,
        normals: Iterator<Vector3dc>,
        temp1: CollisionRange,
        temp2: CollisionRange
    ): Vector3dc {
        var newFirstPolygonVel: Vector3dc? = null

        for (normal in normals) {
            // Calculate the overlapping range of the projection of both polygons along the [normal] axis
            val collisionResponseAlongNormal =
                computeCollisionResponseAlongNormalWithVel(
                    firstPolygon, firstPolygonVel, secondPolygon, normal, temp1, temp2
                )

            // Not colliding, even with the velocity
            if (collisionResponseAlongNormal == 0.0)
                return firstPolygonVel

            val potentialNewVel: Vector3dc

            // Player is trying to climb a very steep slope, in this case don't change the y, only change horizontal components
            // Adjust the collision vector such that the y-component is 0
            val forcedResponseNormal = Vector3d(normal.x(), 0.0, normal.z()).normalize()
            if (forcedResponseNormal.isFinite) {
                val dotProduct = normal.dot(forcedResponseNormal)
                val newCollisionResponse = Vector3d(
                    collisionResponseAlongNormal * forcedResponseNormal.x() / dotProduct, 0.0,
                    collisionResponseAlongNormal * forcedResponseNormal.z() / dotProduct
                )
                potentialNewVel = firstPolygonVel.add(newCollisionResponse, Vector3d())
            } else {
                // Skip
                continue
            }

            if (newFirstPolygonVel == null) {
                newFirstPolygonVel = potentialNewVel
            } else {
                val potentialNewVelDif = Vector3d(potentialNewVel).sub(firstPolygonVel)
                val newFirstPolygonVelDif = Vector3d(newFirstPolygonVel).sub(firstPolygonVel)

                // Add the response
                if (potentialNewVelDif.lengthSquared() < newFirstPolygonVelDif.lengthSquared())
                    newFirstPolygonVel = potentialNewVel
            }
        }

        return newFirstPolygonVel!!
    }

    private fun angleCosHorizontalComponents(a: Vector3dc, b: Vector3dc): Double {
        return Vector3d(a.x(), 0.0, a.z()).angleCos(Vector3d(b.x(), 0.0, b.z()))
    }

    override fun timeToCollision(
        firstPolygon: ConvexPolygonc, secondPolygon: ConvexPolygonc, firstPolygonVelocity: Vector3dc,
        normals: Iterator<Vector3dc>
    ): CollisionResultTimeToCollisionc {
        val temp1 = CollisionRange.create()
        val temp2 = CollisionRange.create()
        val result = CollisionResultTimeToCollision.createEmptyCollisionResultTimeToCollision()

        var maxTimeToCollision = 0.0
        result._initiallyColliding = true // Initially assume that polygons are collided

        for (normal in normals) {
            // Calculate the overlapping range of the projection of both polygons along the [normal] axis
            val timeToImpactResponse =
                computeTimeToCollisionAlongNormal(
                    firstPolygon, secondPolygon, firstPolygonVelocity, normal, temp1, temp2
                )

            if (timeToImpactResponse != 0.0) {
                // Polygons are not colliding along [normal]
                result._initiallyColliding = false
                if (timeToImpactResponse > maxTimeToCollision) {
                    maxTimeToCollision = timeToImpactResponse
                    result._collisionAxis.set(normal)
                    result._timeToCollision = maxTimeToCollision
                    // Stop looping if we will never collide with the other polygon
                    if (timeToImpactResponse == Double.POSITIVE_INFINITY) break
                }
            }
        }

        return result
    }

    fun computeCollisionResponseAlongNormal(
        firstPolygon: ConvexPolygonc,
        secondPolygon: ConvexPolygonc,
        normal: Vector3dc,
        temp1: CollisionRange,
        temp2: CollisionRange
    ): Double {
        // Check if the polygons are separated along the [normal] axis
        val firstCollisionRange: CollisionRangec = firstPolygon.getProjectionAlongAxis(normal, temp1)
        val secondCollisionRange: CollisionRangec = secondPolygon.getProjectionAlongAxis(normal, temp2)

        return CollisionRangec.computeCollisionResponse(
            firstCollisionRange,
            secondCollisionRange
        )
    }

    private fun computeCollisionResponseAlongNormalWithVel(
        firstPolygon: ConvexPolygonc,
        firstPolygonVel: Vector3dc,
        secondPolygon: ConvexPolygonc,
        normal: Vector3dc,
        temp1: CollisionRange,
        temp2: CollisionRange
    ): Double {
        // Check if the polygons are separated along the [normal] axis
        val firstCollisionRange: CollisionRangec = firstPolygon.getProjectionAlongAxis(normal, temp1)
        val secondCollisionRange: CollisionRangec = secondPolygon.getProjectionAlongAxis(normal, temp2)
        val firstVelAlongNormal = normal.dot(firstPolygonVel)

        return CollisionRangec.computeCollisionResponseGivenVelocity(
            firstCollisionRange,
            secondCollisionRange,
            firstVelAlongNormal
        )
    }

    fun computeTimeToCollisionAlongNormal(
        firstPolygon: ConvexPolygonc,
        secondPolygon: ConvexPolygonc,
        firstPolygonVelocity: Vector3dc,
        normal: Vector3dc,
        temp1: CollisionRange,
        temp2: CollisionRange
    ): Double {
        // Check if the polygons are separated along the [normal] axis
        val firstCollisionRange: CollisionRangec = firstPolygon.getProjectionAlongAxis(normal, temp1)
        val secondCollisionRange: CollisionRangec = secondPolygon.getProjectionAlongAxis(normal, temp2)
        val firstRangeVelocityAlongNormal = firstPolygonVelocity.dot(normal)

        return CollisionRangec.computeCollisionTime(
            firstCollisionRange,
            secondCollisionRange,
            firstRangeVelocityAlongNormal
        )
    }
}
