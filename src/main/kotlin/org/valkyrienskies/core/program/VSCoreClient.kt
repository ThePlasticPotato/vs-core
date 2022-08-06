package org.valkyrienskies.core.program

import javax.inject.Inject

/**
 * An object that lives the entirety of the program.
 * Intended to be bound to MinecraftClient
 */
class VSCoreClient @Inject constructor(
    private val base: VSCoreImpl
) : VSCore by base
