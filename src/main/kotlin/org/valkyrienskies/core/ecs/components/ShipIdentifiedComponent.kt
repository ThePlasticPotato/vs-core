package org.valkyrienskies.core.ecs.components

import org.valkyrienskies.core.ecs.Component

/**
 * Ship identified component
 *
 *  A unique id for a ships that are presistent across sessions.
 */
@JvmInline
value class ShipIdentifiedComponent(val id: Long) : Component
