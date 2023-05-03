package org.valkyrienskies.core.api.bodysegments

import org.valkyrienskies.core.api.bodies.properties.BodyInertiaData
import org.valkyrienskies.core.api.bodies.properties.BodyTransformVelocity
import org.valkyrienskies.core.api.bodies.shape.BodyShape
import org.valkyrienskies.core.api.util.HasId

interface BodySegment : HasId {
    /**
     * The owner of a BodySegment may be a [VSBody] or another [BodySegment]
     */
    val owner: Long

    /**
     * The shape of this segment
     */
    val shape: BodyShape

    /**
     * The transform of this segment
     *
     * position, velocity, omega are relative to the model-space of the body owning this segment
     */
    val transformInOwner: BodyTransformVelocity

    /**
     * The inertia data for this segment, in model-space
     */
    val inertia: BodyInertiaData
}