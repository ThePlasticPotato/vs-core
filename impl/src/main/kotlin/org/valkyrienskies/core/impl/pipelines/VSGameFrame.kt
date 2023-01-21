package org.valkyrienskies.core.impl.pipelines

import org.joml.Vector3dc
import org.joml.Vector3ic
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.bodies.VSBody
import org.valkyrienskies.core.api.physics.constraints.VSConstraintAndId
import org.valkyrienskies.core.api.physics.constraints.VSConstraintId
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.impl.api.ShipForcesInducer
import org.valkyrienskies.core.impl.game.ships.PhysInertia
import org.valkyrienskies.core.impl.game.ships.ShipPhysicsData
import org.valkyrienskies.physics_api.PoseVel
import org.valkyrienskies.physics_api.voxel.updates.IVoxelShapeUpdate


data class NGameFrame(
    val newBodies: List<VSBody>
)

/**
 * A [VSGameFrame] represents the change of state of the game that occurred over 1 tick
 */
data class VSGameFrame(
    val newBodies: List<NewVoxelRigidBodyFrameData>, // Ships to be added to the Physics simulation
    val deletedBodies: List<ShipId>, // Ships to be deleted from the Physics simulation
    val updatedBodies: Map<ShipId, UpdateRigidBodyFrameData>, // Map of ship updates
    val voxelUpdatesMap: Map<ShipId, List<IVoxelShapeUpdate>>, // Voxel updates applied by this frame
    val constraintsCreatedThisTick: List<VSConstraintAndId>,
    val constraintsUpdatedThisTick: List<VSConstraintAndId>,
    val constraintsDeletedThisTick: List<VSConstraintId>
)

/**
 * The data used to add a new ship to the physics engine
 */
data class NewVoxelRigidBodyFrameData(
    val uuid: ShipId,
    val dimension: Int,
    val minDefined: Vector3ic,
    val maxDefined: Vector3ic,
    val totalVoxelRegion: AABBic,
    val inertiaData: PhysInertia,
    val physicsData: ShipPhysicsData,
    val poseVel: PoseVel,
    val shipScaling: Double,
    val voxelOffset: Vector3dc,
    val isStatic: Boolean,
    val shipVoxelsFullyLoaded: Boolean,
    val forcesInducers: List<ShipForcesInducer>
)

data class UpdateRigidBodyFrameData(
    val uuid: ShipId,
    val newVoxelOffset: Vector3dc,
    val newScaling: Double,
    val inertiaData: PhysInertia,
    val physicsData: ShipPhysicsData,
    val isStatic: Boolean,
    val shipVoxelsFullyLoaded: Boolean,
    val forcesInducers: List<ShipForcesInducer>
)

enum class ConstraintUpdateTypeInGameFrame {
    CREATE, UPDATE, DELETE
}
