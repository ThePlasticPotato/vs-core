package org.valkyrienskies.core.impl.entities

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import org.valkyrienskies.core.api.bodies.PhysicsVSBody
import org.valkyrienskies.core.impl.entities.bodies.ServerVSBodyImpl
import org.valkyrienskies.core.impl.entities.bodies.physics_to_server.PhysicsToServerBodyReceiver
import org.valkyrienskies.core.impl.entities.bodies.physics_to_server.PhysicsToServerBodyWorldUpdate
import org.valkyrienskies.core.impl.entities.bodysegment.ImmutableBodySegmentImpl
import org.valkyrienskies.core.impl.entities.bodysegment.PhysicsBodySegmentImpl
import org.valkyrienskies.core.impl.entities.world.EntityWorldImpl
import org.valkyrienskies.core.impl.util.cud.UpdateQueue

class NewPipeline {

    val allocator = IdAllocator()

    val bodyPhysicsWorld: EntityWorldImpl<PhysicsVSBody> = EntityWorldImpl()
    val bodyServerWorld: EntityWorldImpl<ServerVSBodyImpl> = EntityWorldImpl()

    val physicsToServerBodyUpdateQueue = UpdateQueue<PhysicsToServerBodyWorldUpdate>(TODO())
    val lastTransformUpdate = Long2LongOpenHashMap()

    val physicsToServerBodyReceiver = PhysicsToServerBodyReceiver(physicsToServerBodyUpdateQueue, bodyServerWorld, lastTransformUpdate)


    val segmentPhysicsWorld: EntityWorldImpl<PhysicsBodySegmentImpl> = EntityWorldImpl()
    val segmentServerWorld: EntityWorldImpl<ImmutableBodySegmentImpl> = EntityWorldImpl()

    init {

    }

}