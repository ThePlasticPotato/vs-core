package org.valkyrienskies.core.util

import org.joml.Vector3d
import org.joml.primitives.AABBd
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AABBdUtilKtTest {

    @Test
    fun testSignedDistanceTo() {
        val acceptableError = 1e-12
        val aabb = AABBd(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)

        run {
            val testPos = Vector3d(-1.0, 0.0, 0.0)
            assertEquals(1.0, aabb.signedDistanceTo(testPos), acceptableError)
        }

        run {
            val testPos = Vector3d(0.5, 0.5, 0.5)
            assertEquals(-0.5, aabb.signedDistanceTo(testPos), acceptableError)
        }

        run {
            val testPos = Vector3d(0.5, 0.9, 0.2)
            assertEquals(-0.1, aabb.signedDistanceTo(testPos), acceptableError)
        }

        run {
            val testPos = Vector3d(2.0, 2.0, 2.0)
            assertEquals(1.7320508075688772, aabb.signedDistanceTo(testPos), acceptableError)
        }
    }
}
