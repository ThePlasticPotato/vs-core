package org.valkyrienskies.core.impl.entities.bodysegment

import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyInertiaData
import org.valkyrienskies.core.api.bodies.properties.BodyTransformVelocity
import org.valkyrienskies.core.api.bodies.shape.BodyShape
import org.valkyrienskies.core.api.bodysegments.PhysicsBodySegment

class PhysicsBodySegmentImpl(
    override val owner: BodyId,
    override val id: Long,
    override var shape: BodyShape,
    override var transformInOwner: BodyTransformVelocity,
    override var inertia: BodyInertiaData,
) : PhysicsBodySegment