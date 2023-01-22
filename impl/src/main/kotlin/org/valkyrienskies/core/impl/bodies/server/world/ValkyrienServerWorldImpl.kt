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
import org.valkyrienskies.core.impl.bodies.physics.world.PipelineQueues
import java.util.function.Supplier
import javax.inject.Inject

class ValkyrienServerWorldImpl @Inject constructor(
    private val queues: PipelineQueues,
    private val attachments: AttachmentHolder
) : ValkyrienServerWorld {


    override fun createSphereBody(radius: Double, dimension: DimensionId): ServerVSBody {
        TODO("Not yet implemented")
    }

    override fun createBoxBody(lengths: Vector3dc, dimension: DimensionId): ServerVSBody {
        TODO("Not yet implemented")
    }

    override fun createWheelBody(radius: Double, halfThickness: Double, dimension: DimensionId): ServerVSBody {
        TODO("Not yet implemented")
    }

    override fun createCapsuleBody(radius: Double, halfLength: Double, dimension: DimensionId): ServerVSBody {
        TODO("Not yet implemented")
    }

    override fun createVoxelBody(definedArea: AABBic, totalVoxelRegion: AABBic, dimension: DimensionId): ServerVSBody {
        TODO("Not yet implemented")
    }

    private fun createBodyFromGame() {

    }

    override fun removeBody(id: BodyId) {
        TODO("Not yet implemented")
    }

    override fun getBody(id: BodyId): ServerVSBody? {
        TODO("Not yet implemented")
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