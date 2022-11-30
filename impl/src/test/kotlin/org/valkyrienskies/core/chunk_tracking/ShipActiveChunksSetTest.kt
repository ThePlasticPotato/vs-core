package org.valkyrienskies.core.chunk_tracking

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.valkyrienskies.core.VSRandomUtils
import org.valkyrienskies.core.util.serialization.VSJacksonUtil

class ShipActiveChunksSetTest {

    @Test
    fun addChunkPos() {
        val shipActiveChunksSet = ShipActiveChunksSet.create()
        assertTrue(shipActiveChunksSet.add(0, 0))
        assertTrue(shipActiveChunksSet.add(1, 1))
        assertFalse(shipActiveChunksSet.add(0, 0))
    }

    @RepeatedTest(25)
    fun removeChunkPos() {
        val shipActiveChunksSet = ShipActiveChunksSet.create()
        val chunkX = VSRandomUtils.randomIntegerNotCloseToLimit()
        val chunkZ = VSRandomUtils.randomIntegerNotCloseToLimit()
        assertTrue(shipActiveChunksSet.add(chunkX, chunkZ))
        assertTrue(shipActiveChunksSet.remove(chunkX, chunkZ))
        assertFalse(shipActiveChunksSet.remove(chunkX, chunkZ))
    }

    @Test
    fun iterateChunkPos() {
        val shipActiveChunksSet = ShipActiveChunksSet.create()
        assertTrue(shipActiveChunksSet.add(200, 300))

        val sum: (Int, Int) -> Unit = { chunkX: Int, chunkZ: Int ->
            assertEquals(chunkX, 200)
            assertEquals(chunkZ, 300)
        }

        shipActiveChunksSet.iterateChunkPos(sum)
    }

    /**
     * Tests the correctness of [ShipActiveChunksSet] serialization and deserialization.
     */
    @RepeatedTest(25)
    fun testSerializationAndDeSerialization() {
        val shipActiveChunksSet = VSRandomUtils.randomShipActiveChunkSet(size = 100)
        // Now serialize and deserialize and verify that they are the same
        val serializedToBytes = VSJacksonUtil.defaultMapper.writeValueAsBytes(shipActiveChunksSet)
        val shipActiveChunksSetDeserialized = VSJacksonUtil.defaultMapper.readValue(
            serializedToBytes,
            ShipActiveChunksSet::class.java
        )

        // Verify that both are equal
        assertEquals(shipActiveChunksSet, shipActiveChunksSetDeserialized)
    }

    @Test
    fun containsChunkPos() {
        val shipActiveChunksSet = ShipActiveChunksSet.create()
        assertTrue(shipActiveChunksSet.add(0, 0))
        assertTrue(shipActiveChunksSet.add(1, 1))

        assertTrue(shipActiveChunksSet.contains(0, 0))
        assertTrue(shipActiveChunksSet.contains(1, 1))
        assertFalse(shipActiveChunksSet.contains(2, 2))
    }
}
