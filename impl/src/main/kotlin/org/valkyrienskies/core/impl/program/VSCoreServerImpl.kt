package org.valkyrienskies.core.impl.program

import org.valkyrienskies.core.apigame.VSCoreServer
import javax.inject.Inject

/**
 * An object that lives the entirety of the program.
 * Intended to be bound to DedicatedServer
 */
class VSCoreServerImpl @Inject constructor(
    private val base: VSCoreImpl
) : VSCoreInternal by base, VSCoreServer
