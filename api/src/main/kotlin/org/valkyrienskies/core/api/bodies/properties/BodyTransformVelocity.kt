package org.valkyrienskies.core.api.bodies.properties

import org.joml.Quaterniondc
import org.joml.Vector3dc

interface BodyTransformVelocity : BodyTransform, BodyVelocities, PoseVelocity {
    fun copy(
        position: Vector3dc = this.position,
        positionInModel: Vector3dc = this.positionInModel,
        rotation: Quaterniondc = this.rotation,
        scaling: Vector3dc = this.scaling,
        velocity: Vector3dc = this.velocity,
        omega: Vector3dc = this.omega
    ): BodyTransformVelocity
}