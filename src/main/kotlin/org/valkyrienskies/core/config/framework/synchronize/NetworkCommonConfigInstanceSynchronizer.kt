package org.valkyrienskies.core.config.framework.synchronize

import dagger.Lazy
import org.valkyrienskies.core.config.framework.ConfigInstance
import org.valkyrienskies.core.config.framework.scopes.single.SingleConfigRegistry
import org.valkyrienskies.core.config.framework.scopes.single.SingleScopedConfig
import org.valkyrienskies.core.networking.impl.PacketCommonConfigUpdate
import org.valkyrienskies.core.networking.simple.SimplePacketNetworking

class NetworkCommonConfigInstanceSynchronizer<C : SingleScopedConfig>(
    private val instance: ConfigInstance<C, Unit>,
    private val registry: Lazy<SingleConfigRegistry>,
    private val packets: SimplePacketNetworking
) : ConfigInstanceSynchronizer<C, Unit> {

    init {
        packets.registerClientHandler(PacketCommonConfigUpdate::class) {
            registry.get().getScope(it.mainClass).set(it.newConfig)
        }
    }

    override fun afterUpdate(config: ConfigInstance<C, Unit>, ctx: Unit) {
        packets.sendToAllClients(PacketCommonConfigUpdate(config.type.clazz, config.serialize()))
    }
}
