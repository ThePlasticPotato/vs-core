package org.valkyrienskies.core.api.world

import org.joml.Vector3dc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.bodies.PhysicsVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.reference.VSRef
import org.valkyrienskies.core.api.world.properties.DimensionId
import java.util.concurrent.Executor

interface ValkyrienPhysicsWorld : ValkyrienServerBaseWorld {

    val preTickExecutor: Executor
    val dumbForceExecutor: Executor
    val smartForceExecutor: Executor
    val postTickExecutor: Executor

    override fun createSphereBody(radius: Double, dimension: DimensionId): PhysicsVSBody

    override fun createBoxBody(lengths: Vector3dc, dimension: DimensionId): PhysicsVSBody

    override fun createWheelBody(radius: Double, halfThickness: Double, dimension: DimensionId): PhysicsVSBody

    override fun createCapsuleBody(radius: Double, halfLength: Double, dimension: DimensionId): PhysicsVSBody

    override fun createVoxelBody(definedArea: AABBic, totalVoxelRegion: AABBic, dimension: DimensionId): PhysicsVSBody

    override fun getBody(id: BodyId): PhysicsVSBody?

    override fun getBodyReference(id: BodyId): VSRef<PhysicsVSBody>
}
