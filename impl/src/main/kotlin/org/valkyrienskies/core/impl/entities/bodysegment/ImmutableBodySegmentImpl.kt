package org.valkyrienskies.core.impl.entities.bodysegment

import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyInertiaData
import org.valkyrienskies.core.api.bodies.properties.BodyTransformVelocity
import org.valkyrienskies.core.api.bodies.shape.BodyShape
import org.valkyrienskies.core.api.bodysegments.BodySegment

class ImmutableBodySegmentImpl(
    override val id: Long,
    override val owner: BodyId,
    override val shape: BodyShape,
    override val transformInOwner: BodyTransformVelocity,
    override val inertia: BodyInertiaData,
) : BodySegment {
    companion object {
        fun create(segment: BodySegment): ImmutableBodySegmentImpl = ImmutableBodySegmentImpl(
            segment.id,
            segment.owner,
            segment.shape,
            segment.transformInOwner,
            segment.inertia,
        )
    }

}