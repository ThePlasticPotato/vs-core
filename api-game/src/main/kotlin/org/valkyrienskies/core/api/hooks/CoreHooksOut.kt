package org.valkyrienskies.core.api.hooks

import io.netty.buffer.ByteBuf
import org.valkyrienskies.core.api.world.IPlayer
import org.valkyrienskies.core.api.world.ShipWorld
import java.nio.file.Path

interface CoreHooksOut {
    val isPhysicalClient: Boolean
    val configDir: Path
    val playState: PlayState

    val currentShipServerWorld: ShipWorld?
    val currentShipClientWorld: ShipWorld

    fun sendToServer(buf: ByteBuf)
    fun sendToClient(buf: ByteBuf, player: IPlayer)
}