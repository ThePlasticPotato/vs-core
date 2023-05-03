package org.valkyrienskies.core.api.bodysegments

import org.valkyrienskies.core.api.bodies.properties.BodyInertiaData
import org.valkyrienskies.core.api.bodies.properties.BodyTransformVelocity
import org.valkyrienskies.core.api.bodies.shape.BodyShape

interface PhysicsBodySegment : BodySegment {
    override var shape: BodyShape
    override var inertia: BodyInertiaData
    override var transformInOwner: BodyTransformVelocity
//    /**
//     * Given a physics partial tick between 0 and 1, returns a [PoseVelocity] in world-space
//     */
//    var poseVelocitySupplier: DoubleFunction<PoseVelocity>
}