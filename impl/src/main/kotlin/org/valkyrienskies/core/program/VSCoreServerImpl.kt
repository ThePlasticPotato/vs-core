package org.valkyrienskies.core.program

import javax.inject.Inject

/**
 * An object that lives the entirety of the program.
 * Intended to be bound to DedicatedServer
 */
class VSCoreServerImpl @Inject internal constructor(
    private val base: VSCoreImpl
) : VSCoreInternal by base