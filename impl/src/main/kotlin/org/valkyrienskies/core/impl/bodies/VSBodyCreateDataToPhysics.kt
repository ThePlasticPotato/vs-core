package org.valkyrienskies.core.impl.bodies

import org.joml.*
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyTransform
import org.valkyrienskies.core.api.bodies.shape.BodyShape
import org.valkyrienskies.core.api.util.HasId
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl

class VSBodyUpdateToPhysics(
    override val id: BodyId,
    val clock: Long,
    val position: Vector3dc? = null,
    val positionInModel: Vector3dc? = null,
    val rotation: Quaterniondc? = null,
    val scaling: Vector3dc? = null,
    val impulse: Vector3dc? = null,
    val angularImpulse: Vector3dc? = null,
    val velocity: Vector3dc? = null,
    val omega: Vector3dc? = null,
    val mass: Double? = null,
    val momentOfInertia: Matrix3dc? = null,
    val isStatic: Boolean? = null,
    val buoyantFactor: Double? = null,
    val doFluidDrag: Boolean? = null,
) : HasId {
    fun combine(newUpdate: VSBodyUpdateToPhysics): VSBodyUpdateToPhysics = VSBodyUpdateToPhysics(
        id,
        newUpdate.clock,
        newUpdate.position ?: position,
        newUpdate.positionInModel ?: positionInModel,
        newUpdate.rotation ?: rotation,
        newUpdate.scaling ?: scaling,
        newUpdate.impulse?.add(impulse, Vector3d()) ?: impulse,
        newUpdate.angularImpulse?.add(angularImpulse, Vector3d()) ?: angularImpulse,
        newUpdate.velocity ?: velocity,
        newUpdate.omega ?: omega,
        newUpdate.mass ?: mass,
        newUpdate.momentOfInertia ?: momentOfInertia,
        newUpdate.isStatic ?: isStatic,
        newUpdate.buoyantFactor ?: buoyantFactor,
        newUpdate.doFluidDrag ?: doFluidDrag
    )
}


class VSBodyUpdateToServer(
    override val id: Long,
    val clock: Long,
    val transform: BodyTransform,
    val velocity: Vector3dc,
    val omega: Vector3dc,
    val mass: Double,
    val isStatic: Boolean,
    val buoyantFactor: Double,
    val doFluidDrag: Boolean
) : HasId

class VSBodyCreateDataToServer(
    override val id: BodyId,
    val dimension: DimensionId,
    val transform: BodyTransform,
    val velocity: Vector3dc,
    val omega: Vector3dc,
    val shape: BodyShape,
    val mass: Double,
    val momentOfInertia: Matrix3dc,
    val isStatic: Boolean,
    val buoyantFactor: Double,
    val doFluidDrag: Boolean
) : HasId

/**
 * The data required to create a VSBody - used to send data between server and physics threads
 */
class VSBodyCreateDataToPhysics(
    override val id: BodyId,
    val dimension: DimensionId,
    val transform: BodyTransform,
    val velocity: Vector3dc,
    val omega: Vector3dc,
    val shape: BodyShapeInternal,
    val mass: Double,
    val momentOfInertia: Matrix3dc,
    val isStatic: Boolean,
    val buoyantFactor: Double,
    val doFluidDrag: Boolean
) : HasId {

    fun toServer(): VSBodyCreateDataToServer = VSBodyCreateDataToServer(
        id,
        dimension,
        transform,
        velocity,
        omega,
        shape.snapshot(),
        mass,
        momentOfInertia,
        isStatic,
        buoyantFactor,
        doFluidDrag
    )

    companion object {
        fun createEmpty(id: BodyId, dimension: DimensionId, shape: BodyShapeInternal) = VSBodyCreateDataToPhysics(
            id,
            dimension,
            ShipTransformImpl.createEmpty(),
            Vector3d(),
            Vector3d(),
            shape,
            100.0,
            Matrix3d(),
            true,
            1.0,
            true
        )
    }
}