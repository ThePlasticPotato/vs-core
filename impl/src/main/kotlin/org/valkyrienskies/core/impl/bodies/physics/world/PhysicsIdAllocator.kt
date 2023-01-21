package org.valkyrienskies.core.impl.bodies.physics.world

import java.util.concurrent.atomic.AtomicLong

/**
 * Used by both the physics and server threads to allocate body and constraint IDs
 */
class PhysicsIdAllocator(
    val nextBodyId: AtomicLong,
    val nextConstraintId: AtomicLong
)