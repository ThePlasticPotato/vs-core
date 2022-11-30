package org.valkyrienskies.core.hooks

import io.netty.buffer.ByteBuf
import org.valkyrienskies.core.api.hooks.CoreHooks
import org.valkyrienskies.core.api.hooks.CoreHooksOut
import org.valkyrienskies.core.api.world.IPlayer
import org.valkyrienskies.core.config.VSConfigClass
import org.valkyrienskies.core.networking.NetworkChannel
import org.valkyrienskies.core.networking.VSNetworking.NetworkingModule.TCP
import javax.inject.Inject

class CoreHooksImpl @Inject constructor(
    hooksOut: CoreHooksOut,
    @TCP val network: NetworkChannel
) : CoreHooksOut by hooksOut, CoreHooks {

    override fun onReceiveServer(buf: ByteBuf, sender: IPlayer) {
        network.onReceiveServer(buf, sender)
    }

    override fun onReceiveClient(buf: ByteBuf) {
        network.onReceiveClient(buf)
    }

    /**
     * Called when client disconnects from a world
     */
    override fun afterDisconnect() {
        VSConfigClass.afterDisconnect()
    }

    /**
     * Called when a client joins a server
     */
    override fun afterClientJoinServer(player: IPlayer) {
        VSConfigClass.afterClientJoinServer(player)
    }
}

@Deprecated("huge yikes")
lateinit var CoreHooks: CoreHooksImpl
