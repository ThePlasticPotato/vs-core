package org.valkyrienskies.core.api.bodies.shape

import org.joml.Vector3dc
import org.valkyrienskies.core.api.bodies.properties.BodyTransform
import org.valkyrienskies.core.api.bodies.properties.PoseVelocity
import java.util.function.DoubleFunction

interface PhysicsBodySegment : BodySegment {
    override var shape: BodyShape
    override var transform: BodyTransform
    override var velocity: Vector3dc
    override var omega: Vector3dc

    /**
     * Takes a partial physics tick in the interval [0, 1] and returns the pose/velocity of this segment at that
     * partial tick
     *
     * This will be called during the physics tick, but not necessarily on the physics thread.
     */
    var poseVelocitySupplier: DoubleFunction<PoseVelocity>
}
interface BodySegment {
    val shape: BodyShape
    val transform: BodyTransform
    val velocity: Vector3dc
    val omega: Vector3dc
}