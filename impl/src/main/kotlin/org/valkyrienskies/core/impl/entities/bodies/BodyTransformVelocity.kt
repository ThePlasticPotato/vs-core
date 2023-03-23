package org.valkyrienskies.core.impl.entities.bodies

import org.joml.Quaterniond
import org.joml.Quaterniondc
import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.bodies.properties.BodyTransformVelocity
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl

class BodyTransformVelocityImpl(
    position: Vector3dc,
    positionInModel: Vector3dc,
    rotation: Quaterniondc,
    scaling: Vector3dc,
    override val velocity: Vector3dc,
    override val omega: Vector3dc
) : ShipTransformImpl(position, positionInModel, rotation, scaling), BodyTransformVelocity {
    companion object {
        fun createEmpty() = BodyTransformVelocityImpl(
            Vector3d(),
            Vector3d(),
            Quaterniond(),
            Vector3d(1.0, 1.0, 1.0),
            Vector3d(),
            Vector3d()
        )
    }

    override fun copy(
        position: Vector3dc,
        positionInModel: Vector3dc,
        rotation: Quaterniondc,
        scaling: Vector3dc,
        velocity: Vector3dc,
        omega: Vector3dc
    ): BodyTransformVelocity {
        return BodyTransformVelocityImpl(
            position,
            positionInModel,
            rotation,
            scaling,
            velocity,
            omega
        )
    }
}