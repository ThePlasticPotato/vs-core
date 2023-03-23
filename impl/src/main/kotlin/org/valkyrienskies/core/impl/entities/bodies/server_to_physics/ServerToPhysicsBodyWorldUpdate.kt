package org.valkyrienskies.core.impl.entities.bodies.server_to_physics

import it.unimi.dsi.fastutil.longs.LongCollection
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyInertiaData
import org.valkyrienskies.core.api.bodies.properties.BodyTransformVelocity
import org.valkyrienskies.core.api.util.HasId
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.impl.bodies.BodyShapeInternal
import org.valkyrienskies.core.impl.entities.bodies.BodySettings

class ServerToPhysicsBodyWorldUpdate(
    val serverTickNum: Long,
    val newBodies: List<ServerToPhysicsBodyNew>,
    val updatedBodies: List<ServerToPhysicsBodyUpdate>,
    val deletedBodies: LongCollection
)

class ServerToPhysicsBodyNew(
    override val id: BodyId,
    val dimension: DimensionId,
    val transform: BodyTransformVelocity,
    val shape: BodyShapeInternal,
    val settings: BodySettings,
    val inertia: BodyInertiaData,
) : HasId

class ServerToPhysicsBodyUpdate(
    override val id: BodyId,
    val transform: BodyTransformVelocity? = null,
    val shape: BodyShapeInternal? = null,
    val settings: BodySettings? = null,
    val inertia: BodyInertiaData? = null,
) : HasId