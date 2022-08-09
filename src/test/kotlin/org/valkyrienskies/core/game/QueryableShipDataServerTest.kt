package org.valkyrienskies.core.game

import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.valkyrienskies.core.VSRandomUtils
import org.valkyrienskies.core.game.ships.QueryableShipDataImpl
import org.valkyrienskies.core.game.ships.QueryableShipDataServer
import org.valkyrienskies.core.game.ships.ShipData
import org.valkyrienskies.core.util.serialization.VSJacksonUtil
import org.valkyrienskies.test_utils.generators.queryableShipData
import org.valkyrienskies.test_utils.generators.shipData
import kotlin.random.Random

class QueryableShipDataServerTest : AnnotationSpec() {

    /**
     * Tests getting [ShipData] from [java.util.UUID].
     */
    @Test
    fun testGetShipFromUUID() {
        val queryableShipData = QueryableShipDataImpl<ShipData>()
        val shipData = VSRandomUtils.randomShipData()
        queryableShipData.addShipData(shipData)
        assertEquals(shipData, queryableShipData.getById(shipData.id))
    }

    /**
     * Tests getting [ShipData] from [ChunkClaim].
     */
    @Test
    fun testGetShipFromChunkClaim() {
        val queryableShipData = QueryableShipDataImpl<ShipData>()
        val shipData = VSRandomUtils.randomShipData()
        queryableShipData.addShipData(shipData)
        val shipChunkClaim = shipData.chunkClaim

        // Test chunks inside of the claim
        for (count in 1..1000) {
            val chunkX = Random.nextInt(shipChunkClaim.xStart, shipChunkClaim.xEnd + 1)
            val chunkZ = Random.nextInt(shipChunkClaim.zStart, shipChunkClaim.zEnd + 1)
            assertEquals(
                shipData,
                queryableShipData.getShipDataFromChunkPos(chunkX, chunkZ, shipData.chunkClaimDimension)
            )
        }

        // Test chunks outside of the claim
        for (count in 1..1000) {
            val chunkX = if (Random.nextBoolean()) {
                shipChunkClaim.xStart - Random.nextInt(1, 1000)
            } else {
                shipChunkClaim.xEnd + Random.nextInt(1, 1000)
            }
            val chunkZ = if (Random.nextBoolean()) {
                shipChunkClaim.zStart - Random.nextInt(1, 1000)
            } else {
                shipChunkClaim.zEnd + Random.nextInt(1, 1000)
            }
            assertEquals(null, queryableShipData.getShipDataFromChunkPos(chunkX, chunkZ, shipData.chunkClaimDimension))
        }

        // Test more chunks outside of the claim
        assertEquals(
            null,
            queryableShipData.getShipDataFromChunkPos(
                shipChunkClaim.xStart,
                shipChunkClaim.zStart - 1,
                shipData.chunkClaimDimension
            )
        )
        assertEquals(
            null,
            queryableShipData.getShipDataFromChunkPos(
                shipChunkClaim.xStart - 1,
                shipChunkClaim.zStart,
                shipData.chunkClaimDimension
            )
        )
        assertEquals(
            null,
            queryableShipData.getShipDataFromChunkPos(
                shipChunkClaim.xEnd,
                shipChunkClaim.zEnd + 1,
                shipData.chunkClaimDimension
            )
        )
        assertEquals(
            null,
            queryableShipData.getShipDataFromChunkPos(
                shipChunkClaim.xEnd + 1,
                shipChunkClaim.zEnd,
                shipData.chunkClaimDimension
            )
        )
    }

    /**
     * Test adding duplicate [ShipData].
     */
    @Test
    fun testAddDuplicateShip() {
        val queryableShipData = QueryableShipDataImpl<ShipData>()
        val shipData = VSRandomUtils.randomShipData()
        queryableShipData.addShipData(shipData)
        assertThrows(IllegalArgumentException::class.java) {
            queryableShipData.addShipData(shipData)
        }
    }

    /**
     * Test removing [ShipData] in [QueryableShipDataServer].
     */
    @Test
    suspend fun testRemovingShipNotInQueryableShipData() {
        checkAll(Arb.shipData(), Arb.shipData()) { shipData, otherShipData ->
            val queryableShipData = QueryableShipDataImpl<ShipData>()
            queryableShipData.addShipData(shipData)
            assertThrows(IllegalArgumentException::class.java) {
                queryableShipData.removeShipData(otherShipData)
            }
        }
    }

    /**
     * Test getting a [ShipData] by its [org.joml.primitives.AABBdc]
     */
    @Test
    suspend fun testGettingShipByBoundingBox() {
        checkAll(Arb.shipData()) { shipData ->
            val queryableShipData = QueryableShipDataImpl<ShipData>()
            queryableShipData.addShipData(shipData)
            val shipsIntersectingBB = queryableShipData.getShipDataIntersecting(shipData.shipAABB).iterator()
            assertTrue(shipsIntersectingBB.hasNext())
            assertEquals(shipsIntersectingBB.next(), shipData)
            assertFalse(shipsIntersectingBB.hasNext())
        }
    }

    /**
     * Tests the correctness of [QueryableShipDataServer] serialization and deserialization.
     */
    @Test
    suspend fun testSerializationAndDeSerialization() {
        checkAll(Arb.queryableShipData(Arb.int(0, 20))) { queryableShipData ->
            // Now serialize and deserialize and verify that they are the same
            val queryableShipDataSerialized = VSJacksonUtil.defaultMapper.writeValueAsBytes(queryableShipData.toList())
            val queryableShipDataDeserialized =
                QueryableShipDataImpl<ShipData>(VSJacksonUtil.defaultMapper.readValue(queryableShipDataSerialized))
            // Verify that both are equal
            assertEquals(queryableShipData, queryableShipDataDeserialized)
        }
    }
}
