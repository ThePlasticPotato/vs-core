package org.valkyrienskies.core.impl.hooks

import io.netty.buffer.ByteBuf
import org.valkyrienskies.core.apigame.hooks.CoreHooksOut
import org.valkyrienskies.core.apigame.world.IPlayer
import org.valkyrienskies.core.impl.config.VSConfigClass
import org.valkyrienskies.core.impl.networking.NetworkChannel
import org.valkyrienskies.core.impl.networking.VSNetworking
import javax.inject.Inject

class CoreHooksImpl @Inject constructor(
    hooksOut: CoreHooksOut,
    @VSNetworking.NetworkingModule.TCP val network: NetworkChannel
) : CoreHooksOut by hooksOut, org.valkyrienskies.core.apigame.hooks.CoreHooks {

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
