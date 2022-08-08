package org.valkyrienskies.core.networking

import dagger.Module
import dagger.Provides
import it.unimi.dsi.fastutil.booleans.BooleanConsumer
import org.valkyrienskies.core.config.VSCoreConfig.Server
import org.valkyrienskies.core.config.framework.scopes.single.SingleConfig
import org.valkyrienskies.core.networking.VSNetworking.NetworkingModule.TCP
import org.valkyrienskies.core.networking.VSNetworking.NetworkingModule.UDP
import org.valkyrienskies.core.networking.impl.PacketRequestUdp
import org.valkyrienskies.core.networking.impl.PacketUdpState
import org.valkyrienskies.core.networking.simple.SimplePacketNetworking
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
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.annotation.AnnotationRetention.BINARY

@Singleton
class VSNetworking @Inject constructor(
    /**
     * Valkyrien Skies UDP channel
     */
    @UDP val UDP: NetworkChannel,

    /**
     * Valkyrien Skies TCP channel
     *
     * Should be initialized by Forge or Fabric (see [NetworkChannel])
     */
    @TCP val TCP: NetworkChannel,

    packets: Packets, // don't actually need this, but for now force it to init for VSConfigClass

    config: SingleConfig<Server>
) {

    val config by config

    @Module
    abstract class NetworkingModule {
        companion object {

            @Provides
            @Singleton
            fun simplePacketNetworking(packets: Packets): SimplePacketNetworking = packets.simplePackets

            @Provides
            @Singleton
            @TCP
            fun tcp(): NetworkChannel = NetworkChannel()

            @Provides
            @Singleton
            @UDP
            fun udp(): NetworkChannel = NetworkChannel()
        }

        @Qualifier
        @MustBeDocumented
        @Retention(BINARY)
        annotation class TCP

        @Qualifier
        @MustBeDocumented
        @Retention(BINARY)
        annotation class UDP
    }

    /**
     * TCP Packet used as fallback when no UDP channel available
     */
    private val TCP_UDP_FALLBACK = TCP.registerPacket("UDP fallback")

    var clientUsesUDP = false
    var serverUsesUDP = false

    fun init() {
        setupFallback()
    }

    /**
     * Try to setup udp server
     *
     * @return null if failed, otherwise the udp server
     */
    fun tryUdpServer(): UdpServerImpl? {

        try {
            val udpSocket = DatagramSocket(config.udpPort)

            val udpServer = UdpServerImpl(udpSocket, UDP, TCP_UDP_FALLBACK)
            serverUsesUDP = true

            PacketRequestUdp::class.registerServerHandler { packet, player ->
                udpServer.prepareIdentifier(player, packet)?.let {
                    PacketUdpState(udpSocket.localPort, serverUsesUDP, it)
                        .sendToClient(player)
                }
            }
            return udpServer
        } catch (e: SocketException) {
            logger.error("Tried to bind to ${config.udpPort} but failed!", e)
        } catch (e: Exception) {
            logger.error("Tried to setup udp with port: ${config.udpPort} but failed!", e)
        }

        tcp4udpFallback()
        return null
    }

    private var prevStateHandler: RegisteredHandler? = null

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
            UdpClientImpl(udpSocket, UDP, socketAddress, id, onConfirm = { this.clientUsesUDP = true })
            return true
        } catch (e: Exception) {
            logger.error("Tried to setup udp client with socket address: $socketAddress but failed!", e)
            return false
        }
    }

    private fun tcp4udpFallback() {
        logger.warn("Failed to create UDP socket, falling back to TCP")
        clientUsesUDP = false
        serverUsesUDP = false

        PacketRequestUdp::class.registerServerHandler { packet, player ->
            PacketUdpState(-1, serverUsesUDP, -1).sendToClient(player)
        }

        UDP.rawSendToClient = { data, player ->
            TCP_UDP_FALLBACK.sendToClient(data, player)
        }

        UDP.rawSendToServer = { data ->
            TCP_UDP_FALLBACK.sendToServer(data)
        }
    }

    private fun setupFallback() {
        TCP.registerClientHandler(TCP_UDP_FALLBACK) { packet ->
            UDP.onReceiveClient(packet.data)
        }

        TCP.registerServerHandler(TCP_UDP_FALLBACK) { packet, player ->
            UDP.onReceiveServer(packet.data, player)
        }
    }

    private val logger by logger()
}
