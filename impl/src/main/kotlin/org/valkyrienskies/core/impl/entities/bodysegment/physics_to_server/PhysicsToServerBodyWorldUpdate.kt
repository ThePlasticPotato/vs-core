package org.valkyrienskies.core.impl.entities.bodysegment.physics_to_server

import it.unimi.dsi.fastutil.longs.LongList
import org.valkyrienskies.core.impl.entities.bodysegment.ImmutableBodySegmentImpl

/**
 * Update frame sent from the physics to the server thread every tick
 */
class PhysicsToServerBodySegmentsUpdate(
    val newAndUpdated: List<ImmutableBodySegmentImpl>,
    val removed: LongList
)