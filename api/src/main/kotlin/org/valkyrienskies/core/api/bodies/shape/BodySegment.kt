package org.valkyrienskies.core.api.bodies.shape

import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyInertiaData
import org.valkyrienskies.core.api.bodies.properties.BodyTransformVelocity
import org.valkyrienskies.core.api.bodies.properties.PoseVelocity
import org.valkyrienskies.core.api.util.HasId
import java.util.function.DoubleFunction

interface PhysicsBodySegment : BodySegment {
    override var shape: BodyShape
    override var inertia: BodyInertiaData
    /**
     * Given a physics partial tick between 0 and 1, returns a [PoseVelocity] in world-space
     */
    var poseVelocitySupplier: DoubleFunction<PoseVelocity>
}

interface BodySegment : HasId {
    val owner: BodyId

    val shape: BodyShape

    /**
     * The transform of this segment
     *
     * position, velocity, omega are in world-space, NOT relative to the body owning this segment
     */
    val transform: BodyTransformVelocity

    /**
     * The transform of this segment
     *
     * position, velocity, omega are relative to the body owning this segment
     */
    val transformInOwner: BodyTransformVelocity

    /**
     * The inertia data for this segment, in model-space
     */
    val inertia: BodyInertiaData
}