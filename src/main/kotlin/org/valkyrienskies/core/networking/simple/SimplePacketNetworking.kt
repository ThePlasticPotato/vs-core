package org.valkyrienskies.core.networking.simple

import org.valkyrienskies.core.game.IPlayer
import org.valkyrienskies.core.networking.NetworkChannel
import org.valkyrienskies.core.networking.PacketType
import org.valkyrienskies.core.networking.RegisteredHandler
import org.valkyrienskies.core.networking.VSNetworking
import org.valkyrienskies.core.util.logger
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberFunctions

@Suppress("INAPPLICABLE_JVM_NAME")
interface SimplePacketNetworking {

    fun <T : SimplePacket> registerServerHandler(klass: KClass<T>, handler: (T, IPlayer) -> Unit): RegisteredHandler
    fun <T : SimplePacket> registerClientHandler(klass: KClass<T>, handler: (T) -> Unit): RegisteredHandler
    fun sendToServer(packet: SimplePacket)
    fun sendToClient(packet: SimplePacket, player: IPlayer)
    fun sendToClients(packet: SimplePacket, vararg players: IPlayer)
    fun sendToAllClients(packet: SimplePacket)
    fun register(
        klass: KClass<out SimplePacket>,
        channel: NetworkChannel? = null,
        name: String = "SimplePacket - ${klass.java}"
    )

    // region synthetic extension functions

    @JvmSynthetic
    @JvmName("registerServerHandler1")
    fun <T : SimplePacket> KClass<T>.registerServerHandler(handler: (T, IPlayer) -> Unit): RegisteredHandler =
        registerServerHandler(this, handler)

    @JvmSynthetic
    @JvmName("registerClientHandler1")
    fun <T : SimplePacket> KClass<T>.registerClientHandler(handler: (T) -> Unit): RegisteredHandler =
        registerClientHandler(this, handler)

    @JvmSynthetic
    @JvmName("sendToServer1")
    fun SimplePacket.sendToServer() = sendToServer(this)

    @JvmSynthetic
    @JvmName("sendToClient1")
    fun SimplePacket.sendToClient(player: IPlayer) = sendToClient(this, player)

    @JvmSynthetic
    @JvmName("sendToClients1")
    fun SimplePacket.sendToClients(vararg players: IPlayer) = sendToClients(this, *players)

    @JvmSynthetic
    @JvmName("sendToAllClients1")
    fun SimplePacket.sendToAllClients() = sendToAllClients(this)

    @JvmSynthetic
    @JvmName("register1")
    fun KClass<out SimplePacket>.register(
        channel: NetworkChannel = VSNetworking.TCP,
        name: String = "SimplePacket - ${this.java}"
    ) = register(this, channel, name)

    // endregion

}

class SimplePacketNetworkingImpl @Inject constructor(
    private val networking: VSNetworking
) : SimplePacketNetworking {
    companion object {
        private val logger by logger("Simple Packet")
    }

    private val classToPacket = HashMap<Class<out SimplePacket>, SimplePacketInfo>()

    private data class SimplePacketInfo(
        val type: PacketType,
        val serverHandlers: MutableList<(SimplePacket, IPlayer) -> Unit> = mutableListOf(),
        val clientHandlers: MutableList<(SimplePacket) -> Unit> = mutableListOf()
    )

    private fun Class<out SimplePacket>.getSimplePacketInfo(): SimplePacketInfo {
        return requireNotNull(classToPacket[this]) { "SimplePacket ($this) not registered" }
    }

    private fun Class<out SimplePacket>.getPacketType(): PacketType {
        return getSimplePacketInfo().type
    }

    override fun <T : SimplePacket> registerServerHandler(
        klass: KClass<T>, handler: (T, IPlayer) -> Unit
    ): RegisteredHandler {
        @Suppress("UNCHECKED_CAST")
        klass.java.getSimplePacketInfo().serverHandlers.add(handler as (SimplePacket, IPlayer) -> Unit)

        return RegisteredHandler { klass.java.getSimplePacketInfo().serverHandlers.remove(handler) }
    }

    override fun <T : SimplePacket> registerClientHandler(klass: KClass<T>, handler: (T) -> Unit): RegisteredHandler {
        @Suppress("UNCHECKED_CAST")
        klass.java.getSimplePacketInfo().clientHandlers.add(handler as (SimplePacket) -> Unit)

        return RegisteredHandler { klass.java.getSimplePacketInfo().clientHandlers.remove(handler) }
    }

    override fun sendToServer(packet: SimplePacket) {
        packet::class.java.getPacketType().sendToServer(packet.serialize())
    }

    override fun sendToClient(packet: SimplePacket, player: IPlayer) {
        packet::class.java.getPacketType().sendToClient(packet.serialize(), player)
    }

    override fun sendToClients(packet: SimplePacket, vararg players: IPlayer) {
        require(players.isNotEmpty())

        packet::class.java.getPacketType().sendToClients(packet.serialize(), *players)
    }

    override fun sendToAllClients(packet: SimplePacket) {
        packet::class.java.getPacketType().sendToAllClients(packet.serialize())
    }

    override fun register(
        klass: KClass<out SimplePacket>,
        channel: NetworkChannel?,
        name: String
    ) {
        check(klass.isData) { "SimplePacket (${klass.java}) must be a data class!" }

        val channel = channel ?: networking.TCP
        
        val packetType = channel.registerPacket(name)
        val packetInfo = SimplePacketInfo(packetType)
        classToPacket[klass.java] = packetInfo

        packetType.registerClientHandler { packet ->
            val data = klass.deserialize(packet.data)
            packetInfo.clientHandlers.forEach { it(data) }
            if (packetInfo.clientHandlers.isEmpty())
                logger.warn("No client handlers registered for the received SimplePacket ($packetType)")
        }

        packetType.registerServerHandler { packet, player ->
            val data = klass.deserialize(packet.data)
            packetInfo.serverHandlers.forEach { it(data, player) }
            if (packetInfo.serverHandlers.isEmpty())
                logger.warn("No server handlers registered for the received SimplePacket ($packetType)")
        }

        if (klass.declaredMemberFunctions.any { it.name == "receivedByClient" })
            klass.registerClientHandler(SimplePacket::receivedByClient)
        if (klass.declaredMemberFunctions.any { it.name == "receivedByServer" })
            klass.registerServerHandler(SimplePacket::receivedByServer)
    }
}
