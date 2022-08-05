package org.valkyrienskies.core.networking

import it.unimi.dsi.fastutil.booleans.BooleanConsumer
import org.valkyrienskies.core.config.VSConfigClass
import org.valkyrienskies.core.config.VSCoreConfig
import org.valkyrienskies.core.networking.impl.PacketRequestUdp
import org.valkyrienskies.core.networking.impl.PacketUdpState
import org.valkyrienskies.core.networking.simple.registerClientHandler
import org.valkyrienskies.core.networking.simple.registerServerHandler
import org.valkyrienskies.core.networking.simple.sendToClient
import org.valkyrienskies.core.networking.simple.sendToServer
import org.valkyrienskies.core.util.logger
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.SocketException
import javax.crypto.SecretKey

object VSNetworking {
    /**
     * Valkyrien Skies UDP channel
     */
    val UDP = NetworkChannel()

    /**
     * Valkyrien Skies TCP channel
     *
     * Should be initialized by Forge or Fabric (see [NetworkChannel])
     */
    val TCP = NetworkChannel()

    var clientUsesUDP = false
    var serverUsesUDP = false

    fun init() {
        Packets.init()
        VSConfigClass.registerNetworkHandlers()
        setupFallback()
    }

    /**
     * Try to setup udp server
     *
     * @return null if failed, otherwise the udp server
     */
    fun tryUdpServer(): UdpServerImpl? {

        try {
            val udpSocket = DatagramSocket(VSCoreConfig.SERVER.udpPort)

            val udpServer = UdpServerImpl(udpSocket, UDP)
            PacketRequestUdp::class.registerServerHandler { packet, player ->
                udpServer.prepareIdentifier(player, packet)?.let {
                    PacketUdpState(udpSocket.localPort, serverUsesUDP, it)
                        .sendToClient(player)
                }
            }

            return udpServer
        } catch (e: SocketException) {
            logger.visError("Tried to bind to ${VSCoreConfig.SERVER.udpPort} but failed!", e)
        } catch (e: Exception) {
            logger.error("Tried to setup udp with port: ${VSCoreConfig.SERVER.udpPort} but failed!", e)
        }

        tcp4udpFallback()
        return null
    }

    var prevStateHandler: RegisteredHandler? = null

    /**
     * Try to setup udp client
     *
     * @param supportsUdp get called with true if server udp is supported,
     *  false otherwise
     */
    fun tryUdpClient(server: SocketAddress, secretKey: SecretKey, supportsUdp: BooleanConsumer) {
        prevStateHandler?.unregister()
        prevStateHandler = PacketUdpState::class.registerClientHandler {
            supportsUdp.accept(it.state)
            if (it.state) {
                var server = server

                // If server is not an InetSocketAddress, just use the same 'thing' as tcp
                if (server is InetSocketAddress) {
                    if (server.port != it.port) {
                        server = InetSocketAddress(server.address, it.port)
                    }
                }

                if (!setupUdpClient(server, it.id)) {
                    tcp4udpFallback()
                }
            }
        }
        PacketRequestUdp(0, secretKey.encoded).sendToServer()
    }

    private fun setupUdpClient(socketAddress: SocketAddress, id: Long): Boolean {
        try {
            val udpSocket = DatagramSocket()
            UdpClientImpl(udpSocket, UDP, socketAddress, id)
            return true
        } catch (e: Exception) {
            logger.visError("Tried to setup udp client with socket address: $socketAddress but failed!", e)
            return false
        }
    }

    private fun tcp4udpFallback() {
        logger.visWarn("Failed to create UDP socket, falling back to TCP")
        clientUsesUDP = false
        serverUsesUDP = false

        PacketRequestUdp::class.registerServerHandler { packet, player ->
            PacketUdpState(-1, serverUsesUDP, -1).sendToClient(player)
        }

        UDP.rawSendToClient = { data, player ->
            Packets.TCP_UDP_FALLBACK.sendToClient(data, player)
        }

        UDP.rawSendToServer = { data ->
            Packets.TCP_UDP_FALLBACK.sendToServer(data)
        }
    }

    private fun setupFallback() {
        TCP.registerClientHandler(Packets.TCP_UDP_FALLBACK) { packet ->
            UDP.onReceiveClient(packet.data)
        }

        TCP.registerServerHandler(Packets.TCP_UDP_FALLBACK) { packet, player ->
            UDP.onReceiveServer(packet.data, player)
        }
    }

    private val logger by logger()
}
