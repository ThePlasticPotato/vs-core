package org.valkyrienskies.test_utils.fakes

import io.netty.buffer.ByteBuf
import org.valkyrienskies.core.apigame.hooks.CoreHooksOut
import org.valkyrienskies.core.apigame.hooks.PlayState
import org.valkyrienskies.core.apigame.world.IPlayer
import org.valkyrienskies.core.impl.game.ships.ShipObjectClientWorld
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld
import java.nio.file.Path
import java.nio.file.Paths

class FakeAbstractCoreHooks(
    override val isPhysicalClient: Boolean = false,
    override val configDir: Path = Paths.get("./config"),
    override val playState: PlayState = PlayState.CLIENT_MULTIPLAYER,
    override val currentShipServerWorld: ShipObjectServerWorld? = null,
) : CoreHooksOut {
    override val currentShipClientWorld: ShipObjectClientWorld get() = TODO()
    override fun sendToServer(buf: ByteBuf) {
        TODO("Not yet implemented")
    }

    override fun sendToClient(buf: ByteBuf, player: IPlayer) {
        TODO("Not yet implemented")
    }
}
