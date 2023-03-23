package org.valkyrienskies.core.impl.entities

import org.valkyrienskies.core.api.bodies.PhysicsVSBody
import org.valkyrienskies.core.api.bodies.ServerVSBody

class NewPipeline {

    val allocator = IdAllocator()
    val bodyPhysicsWorld: EntityWorld<PhysicsVSBody> = EntityWorld(allocator)
    val bodyServerWorld: EntityWorld<ServerVSBody> = EntityWorld(allocator)
}