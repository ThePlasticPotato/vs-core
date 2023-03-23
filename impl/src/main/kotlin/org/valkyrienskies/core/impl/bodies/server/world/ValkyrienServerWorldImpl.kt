package org.valkyrienskies.core.impl.bodies.server.world

import org.joml.Vector3dc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.attachment.AttachmentHolder
import org.valkyrienskies.core.api.bodies.ServerVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.physics.constraints.VSConstraint
import org.valkyrienskies.core.api.physics.constraints.VSConstraintId
import org.valkyrienskies.core.api.reference.VSRef
import org.valkyrienskies.core.api.world.ValkyrienServerWorld
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.impl.bodies.BodyShapeInternal
import org.valkyrienskies.core.impl.bodies.ServerVSBodyImpl
import org.valkyrienskies.core.impl.bodies.VSBodyCreateDataToPhysics
import org.valkyrienskies.core.impl.bodies.VSBodyUpdateToServer
import org.valkyrienskies.core.impl.bodies.physics.world.PhysicsIdAllocator
import org.valkyrienskies.core.impl.bodies.physics.world.PipelineQueues
import org.valkyrienskies.core.impl.bodies.storage.QueryableBodiesImpl
import org.valkyrienskies.core.impl.util.assertions.assertIsGameThread
import java.util.function.Supplier
import javax.inject.Inject

class ValkyrienServerWorldImpl @Inject constructor(
    private val queues: PipelineQueues,
    private val attachments: AttachmentHolder,
    private val idAllocator: PhysicsIdAllocator
) : ValkyrienServerWorld {

    private val bodies = QueryableBodiesImpl<ServerVSBody>()

    fun tick() {

    }

    override fun createSphereBody(radius: Double, dimension: DimensionId): ServerVSBody {
        assertIsGameThread()

        return createBodyFromGame(BodyShapeInternal.Sphere(radius), dimension)
    }

    override fun createBoxBody(lengths: Vector3dc, dimension: DimensionId): ServerVSBody {
        assertIsGameThread()

        return createBodyFromGame(BodyShapeInternal.Box(lengths), dimension)
    }

    override fun createWheelBody(radius: Double, halfThickness: Double, dimension: DimensionId): ServerVSBody {
        assertIsGameThread()

        return createBodyFromGame(BodyShapeInternal.Wheel(radius, halfThickness), dimension)
    }

    override fun createCapsuleBody(radius: Double, halfLength: Double, dimension: DimensionId): ServerVSBody {
        assertIsGameThread()

        return createBodyFromGame(BodyShapeInternal.Capsule(radius, halfLength), dimension)
    }

    override fun createVoxelBody(definedArea: AABBic, totalVoxelRegion: AABBic, dimension: DimensionId): ServerVSBody {
        assertIsGameThread()

        return createBodyFromGame(BodyShapeInternal.Voxel(definedArea, totalVoxelRegion), dimension)
    }

    private fun createBodyInternal(data: VSBodyCreateDataToPhysics): ServerVSBodyImpl {
        val body = TODO()
        bodies.add(body)
        return body
    }

    private fun removeBodyInternal(id: BodyId) {
        bodies.remove(id)
    }

    private fun updateBodyInternal(update: VSBodyUpdateToServer) {

    }

    private fun createBodyFromGame(shape: BodyShapeInternal, dimension: DimensionId): ServerVSBodyImpl {
        val id = idAllocator.nextBodyId.getAndIncrement()
        val data = VSBodyCreateDataToPhysics.createEmpty(id, dimension, shape)
        val body = createBodyInternal(data)
        queues.bodiesToPhysics.create(data)

        return body
    }

    override fun removeBody(id: BodyId) {
        removeBodyInternal(id)
        queues.bodiesToPhysics.delete(id)
    }

    override fun getBody(id: BodyId): ServerVSBody? {
        return bodies.getById(id)
    }

    override fun getBodyReference(id: BodyId): VSRef<ServerVSBody> {
        TODO("Not yet implemented")
    }

    override fun createConstraint(constraint: VSConstraint): VSConstraintId? {
        TODO("Not yet implemented")
    }

    override fun updateConstraint(constraintId: VSConstraintId, updatedVSConstraint: VSConstraint): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeConstraint(constraintId: VSConstraintId): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T : Any> getAttachment(clazz: Class<T>): T? {
        TODO("Not yet implemented")
    }

    override fun <T : Any> getOrPutAttachment(clazz: Class<T>, supplier: Supplier<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any> setAttachment(value: T, clazz: Class<T>): T? {
        TODO("Not yet implemented")
    }

    override fun <T : Any> removeAttachment(clazz: Class<T>): T? {
        TODO("Not yet implemented")
    }

}