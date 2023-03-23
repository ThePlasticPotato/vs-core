package org.valkyrienskies.core.impl.bodies

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import org.joml.Quaterniondc
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.bodies.ServerVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.properties.BodyInertiaData
import org.valkyrienskies.core.api.bodies.properties.BodyTransformVelocity
import org.valkyrienskies.core.api.bodies.shape.BodySegment
import org.valkyrienskies.core.api.bodies.shape.BodyShape
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.impl.entities.IdAllocator
import org.valkyrienskies.core.impl.entities.ObservableProperty
import org.valkyrienskies.core.impl.entities.bodies.BodySettings
import java.util.*

class ServerVSBodyImpl(
    override val id: BodyId,
    override val dimension: DimensionId,
    transform: BodyTransformVelocity,
    shape: BodyShape,
    inertia: BodyInertiaData,
    var settings: BodySettings,
    idAllocator: IdAllocator
) : ServerVSBody {

    private val _segments = Long2ObjectOpenHashMap<BodySegment>()
    override val segments: Collection<BodySegment> = Collections.unmodifiableCollection(_segments.values)

    override val shape: BodyShape
        get() = primarySegment.shape

    override fun getSegment(id: Long): BodySegment? {
        return _segments.get(id)
    }

    override fun setTransform(position: Vector3dc, rotation: Quaterniondc, scaling: Vector3dc) {
        TODO("Not yet implemented")
    }

    val transformProperty = ObservableProperty(transform)

    private val inertia get() = primarySegment.inertia
    override val mass: Double get() = inertia.mass
    override val momentOfInertia get() = inertia.momentOfInertia

    override val transform: BodyTransformVelocity by transformProperty

    override val velocity: Vector3dc
        get() = transform.velocity

    override val omega: Vector3dc
        get() = transform.omega

    override var isStatic: Boolean
        get() = settings.isStatic
        set(value) {
            settings = settings.copy(isStatic = value)
        }


    override var buoyantFactor: Double = TODO()

    @property:VSBeta
    override var doFluidDrag: Boolean = TODO()


    override var prevTickTransform = transform
    override val aabb: AABBd = AABBd(shape.aabb).transform(transform.toWorld)

    private fun updateAABB(aabb: AABBdc) {
        this.aabb.set(aabb).transform(transform.toWorld)
    }
}