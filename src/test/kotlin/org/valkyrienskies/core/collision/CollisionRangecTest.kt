package org.valkyrienskies.core.collision

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CollisionRangecTest {

    @Test
    fun testComputeCollisionTime1() {
        val playerCollisionRange = CollisionRange(1.0, 2.0)
        val shipCollisionRange = CollisionRange(3.0, 4.0)

        run {
            val playerVelocity = 1.0
            assertEquals(
                1.0, CollisionRangec.computeCollisionTime(playerCollisionRange, shipCollisionRange, playerVelocity)
            )
        }

        run {
            val playerVelocity = 2.0
            assertEquals(
                0.5, CollisionRangec.computeCollisionTime(playerCollisionRange, shipCollisionRange, playerVelocity)
            )
        }

        run {
            val playerVelocity = -1.0
            assertEquals(
                Double.POSITIVE_INFINITY,
                CollisionRangec.computeCollisionTime(playerCollisionRange, shipCollisionRange, playerVelocity)
            )
        }

        run {
            val playerVelocity = 1e-30
            assertEquals(
                Double.POSITIVE_INFINITY,
                CollisionRangec.computeCollisionTime(playerCollisionRange, shipCollisionRange, playerVelocity)
            )
        }
    }

    @Test
    fun testComputeCollisionTime2() {
        val playerCollisionRange = CollisionRange(1.0, 2.0)
        val shipCollisionRange = CollisionRange(-1.0, 0.0)

        run {
            val playerVelocity = -1.0
            assertEquals(
                1.0, CollisionRangec.computeCollisionTime(playerCollisionRange, shipCollisionRange, playerVelocity)
            )
        }

        run {
            val playerVelocity = -2.0
            assertEquals(
                0.5, CollisionRangec.computeCollisionTime(playerCollisionRange, shipCollisionRange, playerVelocity)
            )
        }

        run {
            val playerVelocity = 1.0
            assertEquals(
                Double.POSITIVE_INFINITY,
                CollisionRangec.computeCollisionTime(playerCollisionRange, shipCollisionRange, playerVelocity)
            )
        }

        run {
            val playerVelocity = -1e-30
            assertEquals(
                Double.POSITIVE_INFINITY,
                CollisionRangec.computeCollisionTime(playerCollisionRange, shipCollisionRange, playerVelocity)
            )
        }
    }

    @Test
    fun testComputeCollisionTime3() {
        val playerCollisionRange = CollisionRange(1.0, 2.0)
        val shipCollisionRange = CollisionRange(1.5, 2.5)

        run {
            val playerVelocity = 1.0
            assertEquals(
                0.0, CollisionRangec.computeCollisionTime(playerCollisionRange, shipCollisionRange, playerVelocity)
            )
        }

        run {
            val playerVelocity = 2.0
            assertEquals(
                0.0, CollisionRangec.computeCollisionTime(playerCollisionRange, shipCollisionRange, playerVelocity)
            )
        }

        run {
            val playerVelocity = -1.0
            assertEquals(
                0.0, CollisionRangec.computeCollisionTime(playerCollisionRange, shipCollisionRange, playerVelocity)
            )
        }

        run {
            val playerVelocity = 1e-30
            assertEquals(
                0.0, CollisionRangec.computeCollisionTime(playerCollisionRange, shipCollisionRange, playerVelocity)
            )
        }
    }

    @Test
    fun testComputeCollisionResponseGivenVelocity1() {
        val playerCollisionRange = CollisionRange(1.0, 2.0)
        val shipCollisionRange = CollisionRange(3.0, 4.0)

        run {
            val playerVelocity = 1.0
            // We can move right by 1.0, but after that we must stop, so the result should be (1.0 - 1.0 = 0.0)
            assertEquals(
                0.0,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }

        run {
            val playerVelocity = 1.5
            // We can move right by 1.0, but after that we must stop, so the result should be (1.0 - 1.5 = -0.5)
            assertEquals(
                -0.5,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }

        run {
            val playerVelocity = 10.0
            // We can move right by 1.0, but after that we must stop, so the result should be (1.0 - 10.0 = -9.0)
            assertEquals(
                -9.0,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }

        run {
            val playerVelocity = -10.0
            // We can move as far left as we want without colliding
            assertEquals(
                0.0,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }
    }

    @Test
    fun testComputeCollisionResponseGivenVelocity2() {
        val playerCollisionRange = CollisionRange(1.0, 2.0)
        val shipCollisionRange = CollisionRange(-1.0, 0.0)

        run {
            val playerVelocity = -1.0
            // We can move left by 1.0, but after that we must stop, so the result should be (1.0 - 1.0 = 0.0)
            assertEquals(
                0.0,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }

        run {
            val playerVelocity = -1.5
            // We can move left by 1.0, but after that we must stop, so the result should be (1.5 - 1.0 = 0.5)
            assertEquals(
                0.5,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }

        run {
            val playerVelocity = -10.0
            // We can move left by 1.0, but after that we must stop, so the result should be (10.0 - 1.0 = 9.0)
            assertEquals(
                9.0,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }

        run {
            val playerVelocity = 10.0
            // We can move as far right as we want without colliding
            assertEquals(
                0.0,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }
    }

    @Test
    fun testComputeCollisionResponseGivenVelocity3() {
        val playerCollisionRange = CollisionRange(1.0, 2.0)
        val shipCollisionRange = CollisionRange(1.5, 2.5)

        run {
            val playerVelocity = -0.5
            // We can move left by 0.5 to resolve the collision
            assertEquals(
                0.0,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }

        run {
            val playerVelocity = 0.0
            // If we don't have any velocity, then we must move left by 0.5 to resolve the collision
            assertEquals(
                -0.5,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }

        run {
            val playerVelocity = 1.0
            // We can't move right at all, so we must move left by (0.5 + 1.0 = 1.5) to resolve the collision
            assertEquals(
                -1.5,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }

        run {
            val playerVelocity = 10.0
            // We can't move right at all, so we must move left by (0.5 + 10.0 = 10.5) to resolve the collision
            assertEquals(
                -10.5,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }

        run {
            val playerVelocity = -10.0
            // We can move as far left as we want without colliding, as long as we move at least 0.5 to the left
            assertEquals(
                0.0,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }
    }

    @Test
    fun testComputeCollisionResponseGivenVelocity4() {
        val playerCollisionRange = CollisionRange(1.0, 2.0)
        val shipCollisionRange = CollisionRange(0.5, 1.5)

        run {
            val playerVelocity = 0.5
            // We can move right by 0.5 to resolve the collision
            assertEquals(
                0.0,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }

        run {
            val playerVelocity = 0.0
            // If we don't have any velocity, then we must move right by 0.5 to resolve the collision
            assertEquals(
                0.5,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }

        run {
            val playerVelocity = -1.0
            // We can't move left at all, so we must move left by (0.5 + 1.0 = 1.5) to resolve the collision
            assertEquals(
                1.5,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }

        run {
            val playerVelocity = -10.0
            // We can't move left at all, so we must move left by (0.5 + 10.0 = 10.5) to resolve the collision
            assertEquals(
                10.5,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }

        run {
            val playerVelocity = 10.0
            // We can move as far right as we want without colliding, as long as we move at least 0.5 to the right
            assertEquals(
                0.0,
                CollisionRangec.computeCollisionResponseGivenVelocity(
                    playerCollisionRange, shipCollisionRange, playerVelocity
                )
            )
        }
    }
}
