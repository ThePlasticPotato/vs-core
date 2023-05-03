package org.valkyrienskies.core.impl.entities.bodies

import org.joml.Quaterniondc
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.bodies.ServerVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyInertiaData
import org.valkyrienskies.core.api.bodies.properties.BodyTransformVelocity
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.impl.entities.ObservableProperty

class ServerVSBodyImpl(
    override val id: BodyId,
    override val dimension: DimensionId,
    transform: BodyTransformVelocity,
    var settings: BodySettings
) : ServerVSBody {

    override fun setTransform(position: Vector3dc, rotation: Quaterniondc, scaling: Vector3dc) {
        TODO("Not yet implemented")
    }

    val transformProperty = ObservableProperty(transform)

    private val inertia: BodyInertiaData = TODO()
    override val mass: Double get() = inertia.mass
    override val momentOfInertia get() = inertia.momentOfInertia

    override val transform: BodyTransformVelocity by transformProperty

    override var isStatic: Boolean
        get() = settings.isStatic
        set(value) {
            settings = settings.copy(isStatic = value)
        }


    override var buoyantFactor: Double = TODO()

    @property:VSBeta
    override var doFluidDrag: Boolean = TODO()


    override var prevTickTransform = transform
    override val aabb: AABBd = TODO()
}