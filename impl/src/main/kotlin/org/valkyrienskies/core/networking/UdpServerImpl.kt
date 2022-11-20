package org.valkyrienskies.core.networking

import com.google.common.collect.HashBiMap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap
import org.valkyrienskies.core.api.world.IPlayer
import org.valkyrienskies.core.networking.impl.PacketRequestUdp
import org.valkyrienskies.core.util.logger
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketAddress
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

internal class UdpServerImpl(
    val socket: DatagramSocket,
    val channel: NetworkChannel,
    val fallback: PacketType,
) {
    private val thread = Thread(::run)

    private val recvBuffer = ByteArray(PACKET_SIZE)
    private val recvPacket = DatagramPacket(recvBuffer, PACKET_SIZE)
    private val sendPacket = DatagramPacket(ByteArray(PACKET_SIZE), PACKET_SIZE)

    private val connections = HashBiMap.create<SocketAddress, IPlayer>()
    private var failedConnectionsInRow = 0

    // TODO remove them after a while
    private val identification = Long2ObjectArrayMap<IPlayer>()
    private val playerSecrets = HashMap<IPlayer, SecretKey>()

    private var packetCount = 0
    private var lastPacketPrint = System.currentTimeMillis()

    init {
        channel.rawSendToClient = ::sendToClient
        thread.start()
        socket.sendBufferSize = 508 * 60
        socket.receiveBufferSize = 508 * 60
    }

    private fun sendToClient(buf: ByteBuf, player: IPlayer) {
        if (connections.inverse()[player] == null) {
            fallback.sendToClient(buf, player)
        } else {
            packetCount++
            if (lastPacketPrint + 1000 < System.currentTimeMillis()) {
                logger.trace("Sended $packetCount UDP packets")
                packetCount = 0
                lastPacketPrint = System.currentTimeMillis()
            }

            sendPacket.socketAddress = connections.inverse()[player]
            buf.readBytes(sendPacket.data, 0, buf.writerIndex())
            sendPacket.length = buf.writerIndex()
            socket.send(sendPacket)
        }
    }

    private fun run() {
        while (!socket.isClosed) {
            try {
                socket.receive(recvPacket)
                // Skip if no player was found
                val sender = connections[recvPacket.socketAddress]
                val buffer = Unpooled.wrappedBuffer(recvBuffer, 0, recvPacket.length)

                if (sender == null) {
                    // If no player was found, try to identify the player
                    if (buffer.capacity() != 8) continue
                    // TODO make this more secure aka encryption
                    val newConnection = identification.remove(buffer.readLong()) ?: continue

                    connections[recvPacket.socketAddress] = newConnection
                    failedConnectionsInRow = 0

                    sendToClient(
                        Unpooled.buffer(16)
                            .writeLong(newConnection.uuid.leastSignificantBits)
                            .writeLong(newConnection.uuid.mostSignificantBits),
                        newConnection
                    )
                } else channel.onReceiveServer(buffer, sender)
            } catch (e: Exception) {
                logger.error("Error in server network thread", e)
            }
        }
    }

    fun prepareIdentifier(player: IPlayer, packet: PacketRequestUdp): Long? =
        Random.nextLong().apply {
            val secretKeyBytes = packet.secretKeyBytes
            try {
                playerSecrets[player] = SecretKeySpec(secretKeyBytes, "AES")
            } catch (e: Exception) {
                logger.warn("Failed to parse secret from player ${player.uuid}", e)
                return null
            }
            identification.put(this, player)
            if (failedConnectionsInRow == 5) {
                logger.warn("There were 5 cases of failed connections in a row, is the UDP port accessible?")
                logger.info("By disabling UDP this warning shall not be printed anymore.")
            }
            failedConnectionsInRow++
        }

    fun disconnect(player: IPlayer) {
        connections.inverse().remove(player)
        identification.values.remove(player)
        playerSecrets.remove(player)
    }

    companion object {
        const val PACKET_SIZE = 508
        private val logger by logger()
    }
}
