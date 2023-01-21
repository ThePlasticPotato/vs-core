package org.valkyrienskies.core.impl.bodies

import org.joml.*
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyTransform
import org.valkyrienskies.core.api.util.HasId
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl

class VSBodyUpdateToPhysics(
    override val id: BodyId,
    val serverTickNumber: Long,
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
) : HasId
/**
 * The data required to create a VSBody - used to send data between server and physics threads
 */
class ServerBaseVSBodyData(
    override val id: BodyId,
    val dimension: DimensionId,
    val transform: BodyTransform,
    var velocity: Vector3dc,
    var omega: Vector3dc,
    val shape: BodyShapeInternal,
    var mass: Double,
    var centerOfMass: Vector3dc,
    var momentOfInertia: Matrix3dc,
    var isStatic: Boolean,
    var buoyantFactor: Double,
    var doFluidDrag: Boolean
) : HasId {
    companion object {
        fun createEmpty(id: BodyId, dimension: DimensionId, shape: BodyShapeInternal) = ServerBaseVSBodyData(
            id,
            dimension,
            ShipTransformImpl.createEmpty(),
            Vector3d(),
            Vector3d(),
            shape,
            100.0,
            Vector3d(),
            Matrix3d(),
            true,
            1.0,
            true
        )
    }
}