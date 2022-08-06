@file:JvmName("SimplePackets")

package org.valkyrienskies.core.networking.simple

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.valkyrienskies.core.game.IPlayer
import org.valkyrienskies.core.networking.NetworkChannel
import org.valkyrienskies.core.networking.RegisteredHandler
import org.valkyrienskies.core.networking.VSNetworking
import org.valkyrienskies.core.util.serialization.VSJacksonUtil
import org.valkyrienskies.core.util.serialization.readValue
import kotlin.reflect.KClass

private val global = SimplePacketNetworkingImpl(VSNetworking)

fun SimplePacket.serialize(): ByteBuf {
    return Unpooled.wrappedBuffer(VSJacksonUtil.packetMapper.writeValueAsBytes(this))
}

fun <T : SimplePacket> KClass<T>.deserialize(buf: ByteBuf): T {
    return VSJacksonUtil.packetMapper.readValue(buf.duplicate(), this.java)
}

@Deprecated(message = "This is global state; please inject a SimplePacketNetworking and use that instead")
fun <T : SimplePacket> KClass<T>.registerServerHandler(handler: (T, IPlayer) -> Unit): RegisteredHandler =
    with(global) { registerServerHandler(handler) }

@Deprecated(message = "This is global state; please inject a SimplePacketNetworking and use that instead")
fun <T : SimplePacket> KClass<T>.registerClientHandler(handler: (T) -> Unit): RegisteredHandler =
    with(global) { registerClientHandler(handler) }

@Deprecated(message = "This is global state; please inject a SimplePacketNetworking and use that instead")
fun SimplePacket.sendToServer() =
    with(global) { sendToServer() }

@Deprecated(message = "This is global state; please inject a SimplePacketNetworking and use that instead")
fun SimplePacket.sendToClient(player: IPlayer) =
    with(global) { sendToClient(player) }

@Deprecated(message = "This is global state; please inject a SimplePacketNetworking and use that instead")
fun SimplePacket.sendToClients(vararg players: IPlayer) =
    with(global) { sendToClients(*players) }

@Deprecated(message = "This is global state; please inject a SimplePacketNetworking and use that instead")
fun SimplePacket.sendToAllClients() =
    with(global) { sendToAllClients() }

@Deprecated(message = "This is global state; please inject a SimplePacketNetworking and use that instead")
fun KClass<out SimplePacket>.register(
    channel: NetworkChannel = VSNetworking.TCP,
    name: String = "SimplePacket - ${this.java}"
) = with(global) { register(channel, name) }

