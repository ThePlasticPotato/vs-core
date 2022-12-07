package org.valkyrienskies.core.impl.collision

import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SATConvexPolygonColliderTest {

    /**
     * Test two polygons that are colliding
     */
    @Test
    fun checkIfColliding() {
        // The optimal collision response is to move firstPolygon by (0, -0.1, 0)
        val firstPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 0.0, 0.0, 1.0, 1.0, 1.0))
        val secondPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 0.9, 0.0, 1.0, 1.0, 1.0))
        val normals: List<Vector3dc> = listOf(Vector3d(1.0, 0.0, 0.0), Vector3d(0.0, 1.0, 0.0), Vector3d(0.0, 0.0, 1.0))
        val collisionResult: CollisionResult = CollisionResult.create()
        val temp1: CollisionRange = CollisionRange.create()
        val temp2: CollisionRange = CollisionRange.create()

        SATConvexPolygonCollider.checkIfColliding(
            firstPolygon, secondPolygon, normals.iterator(), collisionResult, temp1, temp2
        )

        assertTrue(collisionResult.colliding)
        assertEquals(Vector3d(0.0, 1.0, 0.0), collisionResult._collisionAxis)
        assertEquals(-0.1, collisionResult.penetrationOffset, EPSILON)
    }

    /**
     * Test two polygons that are not colliding
     */
    @Test
    fun checkIfColliding2() {
        val firstPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 0.0, 0.0, 1.0, 1.0, 1.0))
        val secondPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(2.0, 0.0, 0.0, 3.0, 1.0, 1.0))
        val normals: List<Vector3dc> = listOf(Vector3d(1.0, 0.0, 0.0), Vector3d(0.0, 1.0, 0.0), Vector3d(0.0, 0.0, 1.0))
        val collisionResult: CollisionResult = CollisionResult.create()
        val temp1: CollisionRange = CollisionRange.create()
        val temp2: CollisionRange = CollisionRange.create()

        SATConvexPolygonCollider.checkIfColliding(
            firstPolygon, secondPolygon, normals.iterator(), collisionResult, temp1, temp2
        )

        assertFalse(collisionResult.colliding)
    }

    /**
     * Test the projection of a polygon along a normal axis
     */
    @Test
    fun computeOverlapAlongNormal() {
        // The optimal collision response is to move firstPolygon by (0, 0.2, 0)
        val firstPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 0.8, 0.0, 1.0, 1.0, 1.0))
        val secondPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 0.0, 0.0, 1.0, 1.0, 1.0))
        val normal: Vector3dc = Vector3d(0.0, 1.0, 0.0)
        val temp1: CollisionRange = CollisionRange.create()
        val temp2: CollisionRange = CollisionRange.create()

        val collisionResponse = SATConvexPolygonCollider.computeCollisionResponseAlongNormal(
            firstPolygon, secondPolygon, normal, temp1, temp2
        )

        assertEquals(0.2, collisionResponse, EPSILON)
    }

    @Test
    fun testComputeTimeToCollisionAlongNormal() {
        val firstPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 2.0, 0.0, 1.0, 3.0, 1.0))
        val secondPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 0.0, 0.0, 1.0, 1.0, 1.0))
        val normal: Vector3dc = Vector3d(0.0, 1.0, 0.0)
        val temp1: CollisionRange = CollisionRange.create()
        val temp2: CollisionRange = CollisionRange.create()
        val firstPolygonVelocity = Vector3d(0.0, -2.0, 0.0)

        val timeToCollision = SATConvexPolygonCollider.computeTimeToCollisionAlongNormal(
            firstPolygon, secondPolygon, firstPolygonVelocity, normal, temp1, temp2
        )

        assertEquals(0.5, timeToCollision, EPSILON)
    }

    @Test
    fun testComputeTimeToCollisionAlongNormal2() {
        val firstPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 1.0, 0.0, 1.0, 2.0, 1.0))
        val secondPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 0.0, 0.0, 1.0, 1.0, 1.0))
        val normal: Vector3dc = Vector3d(0.0, 1.0, 0.0)
        val temp1: CollisionRange = CollisionRange.create()
        val temp2: CollisionRange = CollisionRange.create()
        val firstPolygonVelocity = Vector3d(0.0, -2.0, 0.0)

        val timeToCollision = SATConvexPolygonCollider.computeTimeToCollisionAlongNormal(
            firstPolygon, secondPolygon, firstPolygonVelocity, normal, temp1, temp2
        )

        assertEquals(0.0, timeToCollision)
    }

    @Test
    fun testComputeTimeToCollisionAlongNormal3() {
        val firstPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 2.0, 0.0, 1.0, 3.0, 1.0))
        val secondPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 0.0, 0.0, 1.0, 1.0, 1.0))
        val normal: Vector3dc = Vector3d(0.0, 1.0, 0.0)
        val temp1: CollisionRange = CollisionRange.create()
        val temp2: CollisionRange = CollisionRange.create()
        val firstPolygonVelocity = Vector3d(0.0, 2.0, 0.0)

        val timeToCollision = SATConvexPolygonCollider.computeTimeToCollisionAlongNormal(
            firstPolygon, secondPolygon, firstPolygonVelocity, normal, temp1, temp2
        )

        assertEquals(Double.POSITIVE_INFINITY, timeToCollision)
    }

    @Test
    fun testTimeToCollision() {
        val firstPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 2.0, 0.0, 1.0, 3.0, 1.0))
        val secondPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 0.9, 0.0, 1.0, 1.0, 1.0))
        val normals: List<Vector3dc> = listOf(Vector3d(1.0, 0.0, 0.0), Vector3d(0.0, 1.0, 0.0), Vector3d(0.0, 0.0, 1.0))

        val firstPolyVel = Vector3d(0.0, -2.0, 0.0)

        val collisionResult = SATConvexPolygonCollider.timeToCollision(
            firstPolygon, secondPolygon, firstPolyVel, normals.iterator()
        )

        assertFalse(collisionResult.initiallyColliding)
        assertEquals(Vector3d(0.0, 1.0, 0.0), collisionResult.collisionAxis)
        assertEquals(0.5, collisionResult.timeToCollision, EPSILON)
    }

    @Test
    fun testTimeToCollision2() {
        val firstPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 2.0, 0.0, 1.0, 3.0, 1.0))
        val secondPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 0.9, 0.0, 1.0, 1.0, 1.0))
        val normals: List<Vector3dc> = listOf(Vector3d(1.0, 0.0, 0.0), Vector3d(0.0, 1.0, 0.0), Vector3d(0.0, 0.0, 1.0))

        val firstPolyVel = Vector3d(0.0, -1.0, 0.0)

        val collisionResult = SATConvexPolygonCollider.timeToCollision(
            firstPolygon, secondPolygon, firstPolyVel, normals.iterator()
        )

        assertFalse(collisionResult.initiallyColliding)
        assertEquals(Vector3d(0.0, 1.0, 0.0), collisionResult.collisionAxis)
        assertEquals(1.0, collisionResult.timeToCollision, EPSILON)
    }

    @Test
    fun testTimeToCollision3() {
        val firstPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 2.0, 0.0, 1.0, 3.0, 1.0))
        val secondPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 0.9, 0.0, 1.0, 1.0, 1.0))
        val normals: List<Vector3dc> = listOf(Vector3d(1.0, 0.0, 0.0), Vector3d(0.0, 1.0, 0.0), Vector3d(0.0, 0.0, 1.0))

        val firstPolyVel = Vector3d(0.0, 1.0, 0.0)

        val collisionResult = SATConvexPolygonCollider.timeToCollision(
            firstPolygon, secondPolygon, firstPolyVel, normals.iterator()
        )

        assertFalse(collisionResult.initiallyColliding)
        assertThrows<NeverCollidingException> { collisionResult.collisionAxis }
        assertEquals(Double.POSITIVE_INFINITY, collisionResult.timeToCollision)
    }

    @Test
    fun testTimeToCollision4() {
        val firstPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 0.95, 0.0, 1.0, 1.95, 1.0))
        val secondPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 0.9, 0.0, 1.0, 1.0, 1.0))
        val normals: List<Vector3dc> = listOf(Vector3d(1.0, 0.0, 0.0), Vector3d(0.0, 1.0, 0.0), Vector3d(0.0, 0.0, 1.0))

        val firstPolyVel = Vector3d(0.0, -2.0, 0.0)

        val collisionResult = SATConvexPolygonCollider.timeToCollision(
            firstPolygon, secondPolygon, firstPolyVel, normals.iterator()
        )

        assertTrue(collisionResult.initiallyColliding)
        assertThrows<CollidingException> { collisionResult.collisionAxis }
        assertThrows<CollidingException> { collisionResult.timeToCollision }
    }

    @Test
    fun testTimeToCollisionMatchesCheckIfColliding() {
        val firstPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 1.0, 0.0, 1.0, 2.0, 1.0))
        val secondPolygon: ConvexPolygonc =
            TransformedCuboidPolygon.createFromAABB(AABBd(0.0, 0.0, 0.0, 1.0, 1.0, 1.0))
        val normals: List<Vector3dc> = listOf(Vector3d(1.0, 0.0, 0.0), Vector3d(0.0, 1.0, 0.0), Vector3d(0.0, 0.0, 1.0))

        val firstPolyVel = Vector3d(0.0, 0.0, 0.0)

        val timeToCollisionResult = SATConvexPolygonCollider.timeToCollision(
            firstPolygon, secondPolygon, firstPolyVel, normals.iterator()
        )

        val checkIfCollidingResult = CollisionResult.create()

        SATConvexPolygonCollider.checkIfColliding(
            firstPolygon,
            secondPolygon,
            normals.iterator(),
            checkIfCollidingResult,
            CollisionRange.create(),
            CollisionRange.create()
        )

        assertFalse(timeToCollisionResult.initiallyColliding)
        assertFalse(checkIfCollidingResult.colliding)
    }

    companion object {
        // The epsilon we use to check if two floating point numbers are equal, to account for floating point error
        private const val EPSILON = 1e-8
    }
}
