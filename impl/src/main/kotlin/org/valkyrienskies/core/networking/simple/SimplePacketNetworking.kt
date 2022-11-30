package org.valkyrienskies.core.networking.simple

import org.valkyrienskies.core.api.world.IPlayer
import org.valkyrienskies.core.networking.NetworkChannel
import org.valkyrienskies.core.networking.RegisteredHandler
import kotlin.reflect.KClass

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
        channel: NetworkChannel? = null,
        name: String = "SimplePacket - ${this.java}"
    ) = register(this, channel, name)

    // endregion

}

