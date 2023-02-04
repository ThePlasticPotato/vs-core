package org.valkyrienskies.core.api.world

import org.joml.Vector3dc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.bodies.ServerBaseVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.physics.constraints.VSConstraint
import org.valkyrienskies.core.api.physics.constraints.VSConstraintId
import org.valkyrienskies.core.api.world.properties.DimensionId

interface ValkyrienServerBaseWorld<out B: ServerBaseVSBody> : ValkyrienBaseWorld<B> {


    fun createSphereBody(radius: Double, dimension: DimensionId): B

    fun createBoxBody(lengths: Vector3dc, dimension: DimensionId): B

    fun createWheelBody(radius: Double, halfThickness: Double, dimension: DimensionId): B

    fun createCapsuleBody(radius: Double, halfLength: Double, dimension: DimensionId): B

    fun createVoxelBody(definedArea: AABBic, totalVoxelRegion: AABBic, dimension: DimensionId): B

    fun removeBody(id: BodyId)

    /**
     * @return True non-null if [vsConstraint] was created successfully.
     */
    fun createConstraint(constraint: VSConstraint): VSConstraintId?

    /**
     * @return True iff the constraint with id [constraintId] was successfully updated.
     */
    fun updateConstraint(constraintId: VSConstraintId, updatedConstraint: VSConstraint): Boolean

    /**
     * @return True if a constraint with [constraintId] was removed successfully.
     */
    fun removeConstraint(constraintId: VSConstraintId): Boolean
}