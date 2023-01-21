package org.valkyrienskies.core.api.world

import org.joml.Vector3dc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.bodies.ServerVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.reference.VSRef
import org.valkyrienskies.core.api.world.properties.DimensionId

interface ValkyrienServerWorld : ValkyrienServerBaseWorld {

    override fun createSphereBody(radius: Double, dimension: DimensionId): ServerVSBody

    override fun createBoxBody(lengths: Vector3dc, dimension: DimensionId): ServerVSBody

    override fun createWheelBody(radius: Double, halfThickness: Double, dimension: DimensionId): ServerVSBody

    override fun createCapsuleBody(radius: Double, halfLength: Double, dimension: DimensionId): ServerVSBody

    override fun createVoxelBody(definedArea: AABBic, totalVoxelRegion: AABBic, dimension: DimensionId): ServerVSBody

    override fun removeBody(id: BodyId)

    override fun getBody(id: BodyId): ServerVSBody?

    override fun getBodyReference(id: BodyId): VSRef<ServerVSBody>

}