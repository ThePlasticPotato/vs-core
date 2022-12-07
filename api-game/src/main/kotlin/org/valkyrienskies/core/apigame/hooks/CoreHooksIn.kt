package org.valkyrienskies.core.apigame.hooks

import io.netty.buffer.ByteBuf
import org.valkyrienskies.core.apigame.world.IPlayer

interface CoreHooksIn {

    fun onReceiveServer(buf: ByteBuf, sender: IPlayer)
    fun onReceiveClient(buf: ByteBuf)

    fun afterDisconnect()

    fun afterClientJoinServer(player: IPlayer)
}
