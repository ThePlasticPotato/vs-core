package org.valkyrienskies.core.impl.collision

import org.joml.Matrix4dc
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.impl.util.*
import kotlin.math.abs
import kotlin.math.tan

object EntityPolygonCollider {

    private val X_NORMAL: Vector3dc = Vector3d(1.0, 0.0, 0.0)
    private val Y_NORMAL: Vector3dc = Vector3d(0.0, 1.0, 0.0)
    private val Z_NORMAL: Vector3dc = Vector3d(0.0, 0.0, 1.0)
    private val UNIT_NORMALS = arrayOf(X_NORMAL, Y_NORMAL, Z_NORMAL)

    fun createPolygonFromAABB(aabb: AABBdc, transform: Matrix4dc? = null, shipFrom: ShipId? = null): ConvexPolygonc {
        return TransformedCuboidPolygon.createFromAABB(aabb, transform, shipFrom)
    }

    /**
     * @return [movement] modified such that the entity is colliding with [collidingPolygons]
     */
    fun adjustEntityMovementForPolygonCollisions(
        movement: Vector3dc,
        entityBoundingBox: AABBdc,
        entityStepHeight: Double,
        collidingPolygons: List<ConvexPolygonc>
    ): Pair<Vector3dc, ShipId?> {
        return adjustMovementComponentWise(entityBoundingBox, movement, entityStepHeight, collidingPolygons)
    }

    /**
     * @return [entityVelocity] modified such that the entity is colliding with [collidingPolygons], with the Y-axis prioritized
     */
    private fun adjustMovementComponentWise(
        entityBoundingBox: AABBdc, entityVelocity: Vector3dc, entityStepHeight: Double,
        collidingPolygons: List<ConvexPolygonc>
    ): Pair<Vector3dc, ShipId?> {
        // Let the player climb slopes up to 45 degrees without being slowed down horizontally
        val maxSlopeClimbAngle = 45.0

        // Only attempt to step if the player has some horizontal velocity, and a step height greater than 0.0
        val attemptStep = entityVelocity.horizontalLengthSq() > 1e-8 && entityStepHeight > 0.0
        if (attemptStep) {
            // Push the player down by up to -0.2 before checking if they are standing on the ground
            val yOnlyCollision =
                collide(entityBoundingBox, collidingPolygons, Vector3d(0.0, -0.2, 0.0), maxSlopeClimbAngle)
            val canStepOriginally = canStep4(
                entityBoundingBox.translate(yOnlyCollision.first, AABBd()), collidingPolygons, maxSlopeClimbAngle
            )
            if (canStepOriginally) {
                // Try to collide with step
                val collisionWithStep = collideWithStep(
                    entityBoundingBox, collidingPolygons, entityVelocity, maxSlopeClimbAngle, entityStepHeight
                ) ?: return collideWithoutStep(entityBoundingBox, collidingPolygons, entityVelocity, maxSlopeClimbAngle)
                return if (collisionWithStep.first.differenceHorLengthSq(entityVelocity) < 1e-8) {
                    // If the step collision doesn't change the horizontal velocity, then return it
                    collisionWithStep
                } else {
                    // If the step collision does change the horizontal velocity then do collide without stepping
                    // and choose the result that minimizes the change to the horizontal velocity of the player
                    val collisionWithoutStep =
                        collideWithoutStep(entityBoundingBox, collidingPolygons, entityVelocity, maxSlopeClimbAngle)

                    // Choose the collision method that maximizes horizontal velocity
                    if (
                        collisionWithStep.first.differenceHorLengthSq(entityVelocity) <=
                        collisionWithoutStep.first.differenceHorLengthSq(entityVelocity)
                    ) collisionWithStep else collisionWithoutStep
                }
            }
        }
        return collideWithoutStep(entityBoundingBox, collidingPolygons, entityVelocity, maxSlopeClimbAngle)
    }

    private fun collideWithStep(
        entityAABB: AABBdc, collidingPolygons: List<ConvexPolygonc>, entityVel: Vector3dc, maxSlopeClimbAngle: Double,
        stepHeight: Double
    ): Pair<Vector3dc, ShipId?>? {
        // region Validate inputs
        assert(stepHeight >= 0.0) { "StepHeight was $stepHeight, which is less than 0.0!" }
        assert(maxSlopeClimbAngle >= 0.0) { "MaxSlopeClimbAngle was $maxSlopeClimbAngle, which is less than 0.0!" }
        assert(maxSlopeClimbAngle < 90.0) {
            "MaxSlopeClimbAngle was $maxSlopeClimbAngle, which is greater than or equal to 90.0!"
        }
        // endregion
        val upCollisionResultVel = collide(
            entityAABB, collidingPolygons, Vector3d(0.0, stepHeight, 0.0), maxSlopeClimbAngle, Y_NORMAL
        ).first
        // Limit the y-component of [upCollisionResultVel] to be between 0.0 and [stepHeight]
        if (upCollisionResultVel.y() !in 0.0..stepHeight) return null

        val playerBBMovedUp = entityAABB.translate(upCollisionResultVel, AABBd())
        val horizontalCollisionResultVel = collide(
            playerBBMovedUp, collidingPolygons, Vector3d(entityVel.x(), 0.0, entityVel.z()), maxSlopeClimbAngle
        ).first
        val moveDown = upCollisionResultVel.y() - entityVel.y()
        val playerBBMovedUpThenHorizontal = playerBBMovedUp.translate(horizontalCollisionResultVel, AABBd())

        val downCollisionResult = collide(
            playerBBMovedUpThenHorizontal, collidingPolygons, Vector3d(0.0, -moveDown, 0.0), maxSlopeClimbAngle,
            Y_NORMAL
        )

        val downCollisionResultVel = downCollisionResult.first
        val lastShipCollided = downCollisionResult.second
        val finalVelocity = Vector3d(upCollisionResultVel).add(horizontalCollisionResultVel).add(downCollisionResultVel)

        // Check if the player can step
        val playerBBAfterMove = entityAABB.translate(finalVelocity, AABBd())
        val canStepAfter = canStep4(playerBBAfterMove, collidingPolygons, maxSlopeClimbAngle)
        if (!canStepAfter) {
            val downCollisionResult2 = collide(
                playerBBAfterMove, collidingPolygons, Vector3d(0.0, -moveDown - downCollisionResultVel.y(), 0.0),
                maxSlopeClimbAngle
            )
            val newFinalVelocity = Vector3d(finalVelocity).add(downCollisionResult2.first)
            if (newFinalVelocity.y() !in 0.0..stepHeight) return null
            return Pair(roundNewVelToOriginal(newFinalVelocity, entityVel), downCollisionResult2.second)
        }

        if (finalVelocity.y() !in 0.0..stepHeight) return null
        return Pair(roundNewVelToOriginal(finalVelocity, entityVel), lastShipCollided)
    }

    private fun collideWithoutStep(
        entityAABB: AABBdc, collidingPolygons: List<ConvexPolygonc>, entityVel: Vector3dc, maxSlopeClimbAngle: Double
    ): Pair<Vector3dc, ShipId?> {
        return collide(entityAABB, collidingPolygons, entityVel, maxSlopeClimbAngle)
    }

    /**
     * Note that [maxSlopeClimbAngle] is in degrees
     */
    private fun collide(
        entityAABB: AABBdc, collidingPolygons: List<ConvexPolygonc>, entityVel: Vector3dc,
        maxSlopeClimbAngle: Double, forcedResponseNormalFromCaller: Vector3dc? = null
    ): Pair<Vector3dc, ShipId?> {
        val feetAABB = createFeetAABB(entityAABB)
        val entityPolygon: ConvexPolygonc = TransformedCuboidPolygon.createFromAABB(entityAABB)

        // Collide with polygons in order based on the distance to the player's feet
        val polysSorted = collidingPolygons.sortedBy {
            val centerPos = it.computeCenterPos(Vector3d())
            feetAABB.signedDistanceTo(centerPos)
        }

        var newEntityVelocity: Vector3dc = Vector3d(entityVel)
        var lastCollidedShipId: ShipId? = null

        polysSorted.forEach { shipPoly ->
            if (!AABBd(entityPolygon.aabb).extend(newEntityVelocity).intersectsAABB(shipPoly.aabb)) return@forEach
            val allNormals = generateAllNormals(shipPoly.normals)
            val timeOfImpactResponse = SATConvexPolygonCollider.timeToCollision(
                entityPolygon,
                shipPoly,
                newEntityVelocity,
                allNormals.iterator()
            )
            val timeToCollision =
                if (timeOfImpactResponse.initiallyColliding) 0.0 else timeOfImpactResponse.timeToCollision
            if (timeToCollision < 1.0) {
                val velAlreadyHappened: Vector3dc = newEntityVelocity.mul(timeToCollision, Vector3d())
                val velNotYetHappened: Vector3dc = newEntityVelocity.mul(1.0 - timeToCollision, Vector3d())
                lastCollidedShipId = shipPoly.shipFrom
                // Apply collision response to [velNotYetHappened]
                val entityAABBTranslatedByVelAlreadyHappened = entityAABB.translate(velAlreadyHappened, AABBd())
                val entityPolygonTranslatedByVelAlreadyHappened =
                    TransformedCuboidPolygon.createFromAABB(entityAABBTranslatedByVelAlreadyHappened)
                if (forcedResponseNormalFromCaller == null) {
                    // Try using y-only collision first
                    val canStep = canStep4(entityAABBTranslatedByVelAlreadyHappened, polysSorted, maxSlopeClimbAngle)
                    if (canStep) {
                        val velNotYetHappenedAfterCollision =
                            SATConvexPolygonCollider.computeResponseMinimizingChangesToVel(
                                entityPolygonTranslatedByVelAlreadyHappened,
                                velNotYetHappened,
                                shipPoly,
                                allNormals.iterator(),
                                CollisionRange.create(),
                                CollisionRange.create(),
                                maxSlopeClimbAngle,
                                Y_NORMAL
                            )
                        // TODO: Figure out a better number for this
                        // TODO: Maybe base this on a mix of entity height and gravity???
                        val entityHeight = 1.7
                        // Make this number sufficiently large to prevent falling through the ground when landing with large velocities, but not too large because that breaks things
                        val relMaxChange = 0.2
                        // [nextStep] is true if th y-only version of [velNotYetHappenedAfterCollision] doesn't change the y-velocity too drastically
                        val nextStep =
                            (
                                velNotYetHappened.y() < 0.0 &&
                                    velNotYetHappenedAfterCollision.y() > velNotYetHappened.y() &&
                                    velNotYetHappenedAfterCollision.y() < relMaxChange * entityHeight
                                ) || (
                                velNotYetHappened.y() > 0.0 &&
                                    velNotYetHappenedAfterCollision.y() < velNotYetHappened.y() &&
                                    velNotYetHappenedAfterCollision.y() > relMaxChange * entityHeight
                                )
                        if (nextStep) {
                            // Here for debug reasons
                            newEntityVelocity = velAlreadyHappened.add(velNotYetHappenedAfterCollision, Vector3d())
                            return@forEach
                        }
                    }
                }
                // Collide [entityPolygonTranslatedByVelAlreadyHappened] with velocity [velNotYetHappened] against [shipPoly]
                val velNotYetHappenedAfterCollision = SATConvexPolygonCollider.computeResponseMinimizingChangesToVel(
                    entityPolygonTranslatedByVelAlreadyHappened,
                    velNotYetHappened,
                    shipPoly,
                    allNormals.iterator(),
                    CollisionRange.create(),
                    CollisionRange.create(),
                    maxSlopeClimbAngle,
                    forcedResponseNormalFromCaller
                )
                val newVelocity = velAlreadyHappened.add(velNotYetHappenedAfterCollision, Vector3d())
                newEntityVelocity = newVelocity
            }
        }

        return Pair(roundNewVelToOriginal(newEntityVelocity, entityVel), lastCollidedShipId)
    }

    /**
     * Adjust components of [newVel] to match [originalVel] if they are sufficiently close enough, to account for numerical error.
     */
    private fun roundNewVelToOriginal(newVel: Vector3dc, originalVel: Vector3dc): Vector3dc {
        val newEntityVelocity = Vector3d(newVel)
        val velEpsilon = 1e-8
        if (abs(newEntityVelocity.x() - originalVel.x()) < velEpsilon)
            newEntityVelocity.x = originalVel.x()
        if (abs(newEntityVelocity.y() - originalVel.y()) < velEpsilon)
            newEntityVelocity.y = originalVel.y()
        if (abs(newEntityVelocity.z() - originalVel.z()) < velEpsilon)
            newEntityVelocity.z = originalVel.z()
        return newEntityVelocity
    }

    /**
     * Return a thin slice of the bounding box, positioned below the feet of [entityAABB].
     */
    private fun getFeetSlice(entityAABB: AABBdc, topPosRelative: Double, botPosRelative: Double): AABBdc {
        assert(topPosRelative <= 0.0) { " topPosRelative was $topPosRelative. It must be less than or equal to 0!" }
        assert(
            botPosRelative < topPosRelative
        ) { " botPosRelative was $botPosRelative, which is less than topPosRelative with value of $topPosRelative" }
        val height = entityAABB.maxY() - entityAABB.minY()
        assert(height > 0.0) { "height was $height. It must be greater than 0.0!" }
        val footYPos = entityAABB.minY()
        val topPos = footYPos + topPosRelative * height
        val botPos = footYPos + botPosRelative * height
        return AABBd(entityAABB.minX(), botPos, entityAABB.minZ(), entityAABB.maxX(), topPos, entityAABB.maxZ())
    }

    /**
     * Returns true iff [entityAABB] is standing on the ground given [collidingPolygons] and [maxSlopeClimbAngle].
     */
    private fun canStep4(
        entityAABB: AABBdc, collidingPolygons: List<ConvexPolygonc>, maxSlopeClimbAngle: Double
    ): Boolean {
        val relativeHeight = 1e-3
        val yDepth = (entityAABB.maxY() - entityAABB.minY()) * relativeHeight

        // region Check if the foot box is colliding with anything
        val footBox = getFeetSlice(entityAABB, 0.0, relativeHeight)
        val footBoxPoly = TransformedCuboidPolygon.createFromAABB(footBox)

        // Sort [collidingPolygons] by center position distance to [topSlice]
        val polysSorted = collidingPolygons.sortedBy {
            val centerPos = it.computeCenterPos(Vector3d())
            footBox.signedDistanceTo(centerPos)
        }

        var footBoxResponse: Vector3dc = Vector3d()
        polysSorted.forEach { shipPoly ->
            if (!footBoxPoly.aabb.intersectsAABB(shipPoly.aabb)) return@forEach
            val allNormals = generateAllNormals(shipPoly.normals)
            footBoxResponse = SATConvexPolygonCollider.computeResponseMinimizingChangesToVelHorOnly(
                footBoxPoly,
                footBoxResponse,
                shipPoly,
                allNormals.iterator(),
                CollisionRange.create(),
                CollisionRange.create()
            )
        }
        // endregion

        // region Check if the shrunken slice below the foot box is colliding
        val maxHorLength = yDepth * tan(Math.toRadians(90.0 - maxSlopeClimbAngle))

        var topSlice = getFeetSlice(entityAABB, 0.0, -relativeHeight)
        // Shrink [topSlice] by [relativeHeight] to account for the overlap amount required to "stand"
        // Translate by [footBoxResponse] to handle the case when the foot box is colliding
        topSlice = AABBd(
            topSlice.minX() + maxHorLength, topSlice.minY(), topSlice.minZ() + maxHorLength,
            topSlice.maxX() - maxHorLength, topSlice.maxY(), topSlice.maxZ() - maxHorLength
        ).translate(footBoxResponse)

        val topSlicePoly = TransformedCuboidPolygon.createFromAABB(topSlice)

        polysSorted.forEach { shipPoly ->
            if (!topSlicePoly.aabb.intersectsAABB(shipPoly.aabb)) return@forEach
            val allNormals = generateAllNormals(shipPoly.normals)
            val topResponse = SATConvexPolygonCollider.computeResponseMinimizingChangesToVel(
                topSlicePoly,
                Vector3d(),
                shipPoly,
                allNormals.iterator(),
                CollisionRange.create(),
                CollisionRange.create(),
                maxSlopeClimbAngle
            )
            if (topResponse.lengthSquared() > 1e-8) {
                // If the shrunken slice box is colliding then we can step!
                return true
            }
        }
        // endregion
        return false
    }

    private fun createFeetAABB(aabb: AABBdc): AABBdc {
        return AABBd(
            aabb.minX(),
            aabb.minY(),
            aabb.minZ(),
            aabb.maxX(),
            aabb.minY() + 0.1 * (aabb.maxY() - aabb.minY()),
            aabb.maxZ()
        )
    }

    private fun generateAllNormals(shipNormals: Iterable<Vector3dc>): List<Vector3dc> {
        val normals = ArrayList<Vector3dc>()
        for (normal in UNIT_NORMALS) normals.add(normal)
        for (normal in shipNormals) {
            normals.add(normal)
            for (unitNormal in UNIT_NORMALS) {
                val crossProduct: Vector3dc = normal.cross(unitNormal, Vector3d()).normalize()
                if (crossProduct.lengthSquared() > 1.0e-6) {
                    normals.add(crossProduct)
                }
            }
        }
        return normals
    }
}
