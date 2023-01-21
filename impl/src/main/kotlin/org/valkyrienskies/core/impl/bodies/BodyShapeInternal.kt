package org.valkyrienskies.core.impl.bodies

import org.joml.Vector3dc
import org.joml.Vector3i
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBi
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.bodies.shape.BodyShape
import org.valkyrienskies.core.api.bodies.shape.VoxelUpdate
import org.valkyrienskies.core.impl.util.events.EventEmitter
import org.valkyrienskies.core.impl.util.set
import org.valkyrienskies.physics_api.*

sealed interface BodyShapeInternal : BodyShape {

    val ref: CollisionShapeReference
    fun createRef(world: PhysicsWorldReference)

    data class Sphere(override val radius: Double) : BodyShape.Sphere, BodyShapeInternal {
        override lateinit var ref: SphereShapeReference

        constructor(radius: Double, world: PhysicsWorldReference) : this(radius) {
            createRef(world)
        }

        override fun createRef(world: PhysicsWorldReference) {
            ref = world.makeSphereShapeReference(radius)
        }

        override val aabb: AABBdc = AABBd(-radius, -radius, -radius, radius, radius, radius)
    }

    data class Box(override val halfLengths: Vector3dc) : BodyShape.Box, BodyShapeInternal {
        override lateinit var ref: BoxShapeReference

        constructor(halfLengths: Vector3dc, world: PhysicsWorldReference) : this(halfLengths) {
            createRef(world)
        }

        override fun createRef(world: PhysicsWorldReference) {
            ref = world.makeBoxShapeReference(halfLengths)
        }

        override val aabb: AABBdc = AABBd(
            -halfLengths.x(), -halfLengths.y(), -halfLengths.z(),
            halfLengths.x(), halfLengths.y(), halfLengths.z()
        )
    }

    data class Wheel(override val radius: Double, override val halfThickness: Double) :
        BodyShape.Wheel, BodyShapeInternal {

        override lateinit var ref: WheelShapeReference

        constructor(radius: Double, halfThickness: Double, world: PhysicsWorldReference) : this(radius, halfThickness) {
            createRef(world)
        }

        override fun createRef(world: PhysicsWorldReference) {
            ref = world.makeWheelShapeReference(radius, halfThickness)
        }

        override val aabb: AABBdc = AABBd(-halfThickness, -radius, -radius, halfThickness, radius, radius)
    }

    data class Capsule(override val radius: Double, override val halfLength: Double) :
        BodyShape.Capsule, BodyShapeInternal {
        override lateinit var ref: CapsuleShapeReference

        constructor(radius: Double, halfLength: Double, world: PhysicsWorldReference) : this(radius, halfLength) {
            createRef(world)
        }

        override fun createRef(world: PhysicsWorldReference) {
            ref = world.makeCapsuleShapeReference(radius, halfLength)
        }

        override val aabb: AABBdc = AABBd(-halfLength, -radius, -radius, halfLength, radius, radius)
    }

    data class Voxel(override val definedArea: AABBic, val voxelRegion: AABBic) : BodyShape.Voxel, BodyShapeInternal {
        override lateinit var ref: VoxelShapeReference

        constructor(
            definedArea: AABBic,
            voxelRegion: AABBic,
            world: PhysicsWorldReference
        ) : this(definedArea, voxelRegion) {
            createRef(world)
        }

        override fun createRef(world: PhysicsWorldReference) {
            ref = world.makeVoxelShapeReference(
                Vector3i(definedArea.minX(), definedArea.minY(), definedArea.minZ()),
                Vector3i(definedArea.maxX(), definedArea.maxY(), definedArea.maxZ()),
                voxelRegion
            )
        }

        override fun applyUpdateAsync(update: VoxelUpdate) {
            TODO("Not yet implemented")
        }

        override fun applyUpdateSync(update: VoxelUpdate) {
            TODO("Not yet implemented")
        }

        private val tmp = AABBi()

        override val aabb = AABBd()
            get() {
                ref.getVoxelShapeAABB(tmp)
                return field.set(
                    tmp.minX.toDouble(),
                    tmp.minY.toDouble(),
                    tmp.minZ.toDouble(),
                    tmp.maxX.toDouble(),
                    tmp.maxY.toDouble(),
                    tmp.maxZ.toDouble()
                )
            }
    }
}

interface AABBUpdateNotifier {
    val aabbUpdateEvent: EventEmitter<AABBdc>
}