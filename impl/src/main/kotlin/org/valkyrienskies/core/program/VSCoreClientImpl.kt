package org.valkyrienskies.core.program

import org.valkyrienskies.core.api.VSCoreClient
import org.valkyrienskies.core.api.world.ClientShipWorldCore
import org.valkyrienskies.core.game.ships.ShipObjectClientWorldComponent
import javax.inject.Inject

/**
 * An object that lives the entirety of the program.
 * Intended to be bound to MinecraftClient
 */
class VSCoreClientImpl @Inject constructor(
    private val base: VSCoreImpl,
    private val shipWorldComponentFactory: ShipObjectClientWorldComponent.Factory
) : VSCoreInternal by base, VSCoreClient {
    override fun newShipWorldClient(): ClientShipWorldCore {
        return shipWorldComponentFactory.newShipObjectClientWorldComponent().newWorld()
    }
}
