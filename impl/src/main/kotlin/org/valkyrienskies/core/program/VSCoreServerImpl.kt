package org.valkyrienskies.core.program

import org.valkyrienskies.core.api.VSCoreServer
import javax.inject.Inject

/**
 * An object that lives the entirety of the program.
 * Intended to be bound to DedicatedServer
 */
class VSCoreServerImpl @Inject constructor(
    private val base: VSCoreImpl
) : VSCoreInternal by base, VSCoreServer
