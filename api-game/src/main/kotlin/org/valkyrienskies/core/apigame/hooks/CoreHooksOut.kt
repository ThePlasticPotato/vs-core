package org.valkyrienskies.core.apigame.hooks

import io.netty.buffer.ByteBuf
import org.valkyrienskies.core.apigame.world.ShipWorld
import org.valkyrienskies.core.apigame.world.IPlayer
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
