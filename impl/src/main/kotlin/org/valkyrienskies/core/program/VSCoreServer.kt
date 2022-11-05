package org.valkyrienskies.core.program

import javax.inject.Inject

/**
 * An object that lives the entirety of the program.
 * Intended to be bound to DedicatedServer
 */
class VSCoreServer @Inject internal constructor(
    private val base: VSCoreImpl
) : VSCore by base
