package org.valkyrienskies.core.impl.game.ships.networking

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.launch
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.impl.game.ships.ShipDataCommon
import org.valkyrienskies.core.impl.game.ships.ShipObjectClient
import org.valkyrienskies.core.impl.game.ships.ShipObjectClientWorld
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl
import org.valkyrienskies.core.impl.networking.Packet
import org.valkyrienskies.core.impl.networking.Packets
import org.valkyrienskies.core.impl.networking.RegisteredHandler
import org.valkyrienskies.core.impl.networking.VSCryptUtils
import org.valkyrienskies.core.impl.networking.VSNetworking
import org.valkyrienskies.core.impl.networking.impl.PacketShipDataCreate
import org.valkyrienskies.core.impl.networking.impl.PacketShipRemove
import org.valkyrienskies.core.impl.networking.simple.SimplePacketNetworking
import org.valkyrienskies.core.impl.networking.unregisterAll
import org.valkyrienskies.core.impl.pipelines.VSNetworkPipelineStage
import org.valkyrienskies.core.impl.util.logger
import org.valkyrienskies.core.impl.util.read3FAsNormQuatd
import org.valkyrienskies.core.impl.util.readAABBi
import org.valkyrienskies.core.impl.util.readVec3d
import org.valkyrienskies.core.impl.util.readVec3fAsDouble
import org.valkyrienskies.core.impl.util.serialization.VSJacksonUtil
import java.net.SocketAddress
import javax.crypto.SecretKey

class ShipObjectNetworkManagerClient @AssistedInject constructor(
    @Assisted private val parent: ShipObjectClientWorld,
    private val networking: VSNetworking,
    private val spNetwork: SimplePacketNetworking,
    private val packets: Packets
) {

    @AssistedFactory
    interface Factory {
        fun make(parent: ShipObjectClientWorld): ShipObjectNetworkManagerClient
    }

    private val worldScope get() = parent.coroutineScope

    private lateinit var handlers: List<RegisteredHandler>

    private var secretKey: SecretKey? = null

    /**
     * Set to true once the player has received their first [PacketShipDataCreate]
     */
    @Volatile
    var hasReceivedInitialShips = false

    fun registerPacketListeners() {
        handlers = listOf(
            packets.UDP_SHIP_TRANSFORM.registerClientHandler(this::onShipTransform),
            packets.TCP_SHIP_DATA_DELTA.registerClientHandler(this::onShipDataDelta),
            spNetwork.registerClientHandler(PacketShipDataCreate::class, this::onShipDataCreate),
            spNetwork.registerClientHandler(PacketShipRemove::class, this::onShipDataRemove)
        )

        networking.TCP.clientIsReady()
        networking.UDP.clientIsReady()
    }

    fun onDestroy() {
        handlers.unregisterAll()
        secretKey = null
        networking.TCP.disable()
        networking.UDP.disable()
    }

    private fun onShipDataRemove(packet: PacketShipRemove) = worldScope.launch {
        packet.toRemove.forEach(parent::removeShip)
    }

    private fun onShipDataCreate(packet: PacketShipDataCreate) = worldScope.launch {
        for (ship in packet.toCreate) {
            if (parent.allShips.getById(ship.id) == null) {
                parent.addShip(ship)
            } else {
                // Update the next ship transform
                parent.shipObjects[ship.id]?.nextShipTransform = ship.transform

                throw logger.throwing(
                    IllegalArgumentException("Received ship create packet for already loaded ship?!")
                )
            }
        }
        hasReceivedInitialShips = true
    }

    private fun onShipDataDelta(packet: Packet) = worldScope.launch {
        val buf = packet.data

        while (buf.isReadable) {
            val shipId = buf.readLong()

            val ship = parent.loadedShips.getById(shipId)
            if (ship == null) {
                logger.warn("Received ship data delta for ship with unknown ID!")
                buf.release()
                return@launch
            }
            val shipDataJson = ship.shipDataChannel.decode(buf)

            VSJacksonUtil.deltaMapper
                .readerForUpdating(ship.shipData)
                .readValue<ShipDataCommon>(shipDataJson)
        }
        buf.release()
    }.also { packet.data.retain() }

    private fun onShipTransform(packet: Packet) {
        val buf = packet.data
        readShipTransform(buf, parent.shipObjects)
    }

    private var serverNoUdp = false
    private var tryConnectIn = 100
    fun tick(server: SocketAddress) {
        if (!networking.clientUsesUDP && !serverNoUdp) {
            tryConnectIn--
            if (tryConnectIn <= 0) {
                secretKey = VSCryptUtils.generateAES128Key()
                networking.tryUdpClient(server, secretKey!!) { supports: Boolean ->
                    if (!supports) {
                        serverNoUdp = true
                    }
                }
                tryConnectIn = 100
            }
        }
    }

    companion object {
        private val logger by logger()

        // Reads all ship transforms in a buffer and places them in the latestReceived map.
        fun readShipTransform(buf: ByteBuf, shipObjects: Map<ShipId, ShipObjectClient>) {
            val tickNum = buf.readInt()
            try {
                while (buf.isReadable) {
                    val shipId = buf.readLong()
                    val ship = shipObjects[shipId]
                    if (ship == null) {
                        logger.debug("Received ship transform for ship with unknown id: $shipId")
                        buf.skipBytes(VSNetworkPipelineStage.TRANSFORM_SIZE - 8)
                    } else if (ship.latestNetworkTTick >= tickNum) {
                        // Skip the transform if we already have it
                        buf.skipBytes(VSNetworkPipelineStage.TRANSFORM_SIZE - 8)
                    } else {
                        val centerOfMass = buf.readVec3d()
                        val scaling = buf.readVec3fAsDouble()
                        val rotation = buf.read3FAsNormQuatd()
                        val position = buf.readVec3d()
                        val velocity = buf.readVec3fAsDouble()
                        val omega = buf.readVec3fAsDouble()
                        val shipAABB = buf.readAABBi()

                        ship.latestNetworkTransform = ShipTransformImpl.create(
                            position, centerOfMass, rotation, scaling
                        )
                        ship.latestNetworkTTick = tickNum
                        ship.shipData.physicsData.angularVelocity = omega
                        ship.shipData.physicsData.linearVelocity = velocity

                        // Ignore invalid shipAABB
                        if (shipAABB.isValid) {
                            ship.shipData.shipAABB = shipAABB
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Something went wrong when reading ship transform packets", e)
            }
        }
    }
}
