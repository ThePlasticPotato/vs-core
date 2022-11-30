package org.valkyrienskies.test_utils.fakes

import io.netty.buffer.ByteBuf
import org.valkyrienskies.core.api.hooks.CoreHooksOut
import org.valkyrienskies.core.api.hooks.PlayState
import org.valkyrienskies.core.api.hooks.PlayState.CLIENT_MULTIPLAYER
import org.valkyrienskies.core.api.world.IPlayer
import org.valkyrienskies.core.game.ships.ShipObjectClientWorld
import org.valkyrienskies.core.game.ships.ShipObjectServerWorld
import java.nio.file.Path
import java.nio.file.Paths

class FakeAbstractCoreHooks(
    override val isPhysicalClient: Boolean = false,
    override val configDir: Path = Paths.get("./config"),
    override val playState: PlayState = CLIENT_MULTIPLAYER,
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
