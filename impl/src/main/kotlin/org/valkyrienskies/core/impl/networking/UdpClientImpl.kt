package org.valkyrienskies.core.impl.networking

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.apache.logging.log4j.message.StringFormattedMessage
import org.valkyrienskies.core.impl.networking.UdpServerImpl.Companion.PACKET_SIZE
import org.valkyrienskies.core.impl.util.logger
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketAddress

class UdpClientImpl(
    val socket: DatagramSocket,
    val channel: NetworkChannel,
    val server: SocketAddress, id: Long,
    private val onConfirm: () -> Unit
) {
    private val thread = Thread(::run)

    private val recvBuffer = ByteArray(PACKET_SIZE)
    private val recvPacket = DatagramPacket(recvBuffer, PACKET_SIZE)
    private val sendPacket = DatagramPacket(ByteArray(PACKET_SIZE), PACKET_SIZE)

    init {
        channel.rawSendToServer = ::sendToServer
        sendPacket.socketAddress = server

        // Sending connection id
        sendToServer(Unpooled.buffer(8).writeLong(id))

        socket.sendBufferSize = 508 * 60
        socket.receiveBufferSize = 508 * 60

        thread.start()
    }

    private fun sendToServer(buf: ByteBuf) {
        sendPacket.data = buf.array()
        sendPacket.length = buf.writerIndex()
        socket.send(sendPacket)
    }

    private fun run() {
        // Initial confirmation packet
        socket.soTimeout = 1000
        socket.receive(recvPacket)
        // Check if the confirmation packet is 16 bytes long (UUID of player)
        if (recvPacket.length != 16) {
            throw IllegalStateException("Invalid confirmation packet")
        }

        socket.soTimeout = 0
        onConfirm()
        var packetCount = 0
        var lastPacketPrint = System.currentTimeMillis()

        while (!socket.isClosed) {
            try {
                socket.receive(recvPacket)
                packetCount++
                if (!recvPacket.socketAddress.equals(server)) {
                    logger.warn("Received packet from non server address: ${recvPacket.socketAddress}")
                    logger.warn("This is VERY SUSPICIOUS!")
                    continue
                }

                if (lastPacketPrint + 1000 < System.currentTimeMillis()) {
                    logger.trace("Received $packetCount UDP packets")
                    packetCount = 0
                    lastPacketPrint = System.currentTimeMillis()
                }

                logger.trace { StringFormattedMessage("Client received packet of size ${recvPacket.length}") }
                val buffer = Unpooled.wrappedBuffer(recvBuffer, 0, recvPacket.length)
                channel.onReceiveClient(buffer)
            } catch (e: Exception) {
                logger.error("Error in client network thread", e)
            }
        }
    }

    companion object {
        private val logger by logger()
    }
}
