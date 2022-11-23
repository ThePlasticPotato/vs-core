package org.valkyrienskies.core.program

import org.valkyrienskies.core.game.ships.ShipObjectClientWorldComponent
import javax.inject.Inject

/**
 * An object that lives the entirety of the program.
 * Intended to be bound to MinecraftClient
 */
class VSCoreClientImpl @Inject internal constructor(
    private val base: VSCoreImpl,
    val shipWorldComponentFactory: ShipObjectClientWorldComponent.Factory
) : VSCoreInternal by base
