package org.valkyrienskies.core.impl.pipelines

import org.joml.Vector3dc
import org.joml.Vector3ic
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.apigame.constraints.VSConstraint
import org.valkyrienskies.core.apigame.constraints.VSConstraintAndId
import org.valkyrienskies.core.apigame.constraints.VSConstraintId
import org.valkyrienskies.core.impl.api.ShipForcesInducer
import org.valkyrienskies.core.impl.game.ships.PhysInertia
import org.valkyrienskies.core.impl.game.ships.ShipPhysicsData
import org.valkyrienskies.physics_api.PoseVel
import org.valkyrienskies.physics_api.SegmentTracker
import org.valkyrienskies.physics_api.voxel_updates.IVoxelShapeUpdate

/**
 * A [VSGameFrame] represents the change of state of the game that occurred over 1 tick
 */
data class VSGameFrame(
    val newShips: List<NewShipInGameFrameData>, // Ships to be added to the Physics simulation
    val deletedShips: List<ShipId>, // Ships to be deleted from the Physics simulation
    val updatedShips: Map<ShipId, UpdateShipInGameFrameData>, // Map of ship updates
    val voxelUpdatesMap: Map<ShipId, List<IVoxelShapeUpdate>>, // Voxel updates applied by this frame
    val constraintsCreatedThisTick: List<VSConstraintAndId>,
    val constraintsUpdatedThisTick: List<VSConstraintAndId>,
    val constraintsDeletedThisTick: List<VSConstraintId>
)

/**
 * The data used to add a new ship to the physics engine
 */
data class NewShipInGameFrameData(
    val uuid: ShipId,
    val dimension: Int,
    val minDefined: Vector3ic,
    val maxDefined: Vector3ic,
    val totalVoxelRegion: AABBic,
    val inertiaData: PhysInertia,
    val physicsData: ShipPhysicsData,
    val poseVel: PoseVel,
    val segments: SegmentTracker,
    val voxelOffset: Vector3dc,
    val isStatic: Boolean,
    val shipVoxelsFullyLoaded: Boolean,
    val forcesInducers: List<ShipForcesInducer>
)

data class UpdateShipInGameFrameData(
    val uuid: ShipId,
    val newVoxelOffset: Vector3dc,
    val inertiaData: PhysInertia,
    val physicsData: ShipPhysicsData,
    val isStatic: Boolean,
    val shipVoxelsFullyLoaded: Boolean,
    val forcesInducers: List<ShipForcesInducer>
)

enum class ConstraintUpdateTypeInGameFrame {
    CREATE, UPDATE, DELETE
}
