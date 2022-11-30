package org.valkyrienskies.core.util

import javax.inject.Scope
import kotlin.annotation.AnnotationRetention.BINARY

/**
 * A scope meant to the lifetime of a Minecraft server/client world. Used for things meant to live the same length
 * as ShipObjectClientWorld/VSPipeline
 */
@Scope
@MustBeDocumented
@Retention(BINARY)
annotation class WorldScoped
