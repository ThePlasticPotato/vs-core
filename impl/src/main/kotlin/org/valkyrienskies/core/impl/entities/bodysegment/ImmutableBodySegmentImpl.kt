package org.valkyrienskies.core.impl.entities.bodysegment

import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyInertiaData
import org.valkyrienskies.core.api.bodies.properties.BodyTransformVelocity
import org.valkyrienskies.core.api.bodies.shape.BodySegment
import org.valkyrienskies.core.api.bodies.shape.BodyShape

class ImmutableBodySegmentImpl(
    override val id: Long,
    override val owner: BodyId,
    override val shape: BodyShape,
    override val transform: BodyTransformVelocity,
    override val transformInOwner: BodyTransformVelocity,
    override val inertia: BodyInertiaData,
) : BodySegment