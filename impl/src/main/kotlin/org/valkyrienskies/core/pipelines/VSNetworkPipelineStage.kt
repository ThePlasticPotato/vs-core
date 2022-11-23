package org.valkyrienskies.core.pipelines

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.valkyrienskies.core.api.ServerShipInternal
import org.valkyrienskies.core.game.ships.ShipObjectServerWorld
import org.valkyrienskies.core.networking.Packets
import org.valkyrienskies.core.networking.VSNetworking
import org.valkyrienskies.core.util.logger
import org.valkyrienskies.core.util.writeNormQuatdAs3F
import org.valkyrienskies.core.util.writeVec3AsFloat
import org.valkyrienskies.core.util.writeVec3d
import javax.inject.Inject

internal class VSNetworkPipelineStage @Inject constructor(
    private val shipWorld: ShipObjectServerWorld,
    private val networking: VSNetworking,
    private val packets: Packets
) {

    var noSkip = true

    /**
     * Push a physics frame to the game stage
     *
     * were only sending this every other tick, cus client only uses it every mc tick
     */
    fun pushPhysicsFrame(physicsFrame: VSPhysicsFrame) {
        noSkip = !noSkip
        if (noSkip) return


        shipWorld.playersToTrackedShips.forEach { (player, trackedShips) ->
            val buf = Unpooled.buffer()

            fun send(shipDatas: List<ServerShipInternal>) {
                // Write ship transforms into a ByteBuf
                buf.clear()
                writePacket(buf, shipDatas, physicsFrame)

                // Send it to the player
                packets.UDP_SHIP_TRANSFORM.sendToClient(buf, player)
            }

            // Each transform is 80 bytes big so 6 transforms per packet
            // If not using udp we just send 1 big packet with all transforms
            if (networking.serverUsesUDP)
                trackedShips.chunked(504 / TRANSFORM_SIZE).forEach(::send)
            else
                send(trackedShips.asList())
        }
    }

    companion object {
        fun writePacket(buf: ByteBuf, shipDatas: List<ServerShipInternal>, frame: VSPhysicsFrame) {
            val ships = frame.shipDataMap
            buf.writeInt(frame.physTickNumber)
            shipDatas.forEach { shipData ->
                val physicsFrameData = ships[shipData.id] ?: return@forEach
                val transform =
                    VSGamePipelineStage.generateTransformFromPhysicsFrameData(physicsFrameData, shipData)

                buf.writeLong(shipData.id) // 8
                buf.writeVec3d(transform.positionInShip) // 8 * 3 = 24
                buf.writeVec3AsFloat(transform.shipToWorldScaling) // 4 * 3 = 12
                buf.writeNormQuatdAs3F(transform.shipToWorldRotation) // 4 * 3 = 12
                buf.writeVec3d(transform.positionInWorld) // 8 * 3 = 24
                // 8 + 24 + 12 + 12 + 24 = 80

                // TODO remove, this is wasted bytes
                buf.writeVec3AsFloat(shipData.velocity)
                buf.writeVec3AsFloat(shipData.omega)
                // 80 + 12 + 12 = 104
            }
        }

        const val TRANSFORM_SIZE = 104
        private val logger by logger()
    }
}
