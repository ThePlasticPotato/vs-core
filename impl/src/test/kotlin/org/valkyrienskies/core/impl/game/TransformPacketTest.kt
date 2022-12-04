package org.valkyrienskies.core.impl.game

import io.netty.buffer.Unpooled
import org.valkyrienskies.core.impl.VSRandomUtils
import org.valkyrienskies.core.impl.game.ships.QueryableShipDataImpl
import org.valkyrienskies.core.impl.game.ships.ShipData
import org.valkyrienskies.core.impl.pipelines.VSNetworkPipelineStage
import org.valkyrienskies.core.impl.pipelines.VSPhysicsFrame
import kotlin.random.Random

class TransformPacketTest {

    // @RepeatedTest(25) TODO fix this test
    fun testSerializationAndDeSerialization() {
        val queryableShipData = QueryableShipDataImpl<ShipData>()
        val buf = Unpooled.buffer(508)
        val shipDatas = queryableShipData.idToShipData.values.chunked(504 / VSNetworkPipelineStage.TRANSFORM_SIZE)

        shipDatas.forEach { ships ->
            val fakeFrame = VSPhysicsFrame(
                ships.map { it.id }.associateWith { VSRandomUtils.randomShipInPhysicsFrame(it) },
                emptyMap(),
                Random.nextInt()
            )

            buf.clear()
            VSNetworkPipelineStage.writePacket(buf, ships, fakeFrame)
            // ShipObjectNetworkManagerClient.readShipTransform(buf, queryableShipData.idTo)
            // ShipObjectNetworkManagerClient.latestReceived.forEach {
            //    Assertions.assertEquals(it.value.lastTransform, fakeFrame.shipDataMap[it.key]!!.shipTransform)
            // }
        }
    }
}
