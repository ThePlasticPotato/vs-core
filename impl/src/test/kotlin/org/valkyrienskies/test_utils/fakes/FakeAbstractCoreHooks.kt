package org.valkyrienskies.test_utils.fakes

import org.valkyrienskies.core.game.ships.ShipObjectClientWorld
import org.valkyrienskies.core.game.ships.ShipObjectServerWorld
import org.valkyrienskies.core.hooks.AbstractCoreHooks
import org.valkyrienskies.core.hooks.PlayState
import org.valkyrienskies.core.hooks.PlayState.CLIENT_MULTIPLAYER
import java.nio.file.Path
import java.nio.file.Paths

class FakeAbstractCoreHooks(
    override val isPhysicalClient: Boolean = false,
    override val configDir: Path = Paths.get("./config"),
    override val playState: PlayState = CLIENT_MULTIPLAYER,
    override val currentShipServerWorld: ShipObjectServerWorld? = null,
) : AbstractCoreHooks() {
    override val currentShipClientWorld: ShipObjectClientWorld get() = TODO()
}
