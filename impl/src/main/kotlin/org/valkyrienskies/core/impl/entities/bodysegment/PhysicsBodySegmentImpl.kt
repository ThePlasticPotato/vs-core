package org.valkyrienskies.core.impl.entities.bodysegment

import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyInertiaData
import org.valkyrienskies.core.api.bodies.properties.BodyTransformVelocity
import org.valkyrienskies.core.api.bodies.properties.PoseVelocity
import org.valkyrienskies.core.api.bodies.shape.BodyShape
import org.valkyrienskies.core.api.bodies.shape.PhysicsBodySegment
import java.util.function.DoubleFunction

class PhysicsBodySegmentImpl(
    override val owner: BodyId,
    override val id: Long,
    override var shape: BodyShape,
    override val transform: BodyTransformVelocity,
    override val transformInOwner: BodyTransformVelocity,
    override var inertia: BodyInertiaData,
    override var poseVelocitySupplier: DoubleFunction<PoseVelocity> = DoubleFunction { transform }
) : PhysicsBodySegment {

}