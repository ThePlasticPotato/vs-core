package org.valkyrienskies.core.impl.pipelines

import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.physics_api.PoseVel
import org.valkyrienskies.physics_api.RigidBodyInertiaData
import org.valkyrienskies.physics_api.SegmentTracker
import org.valkyrienskies.physics_api.voxel_updates.IVoxelShapeUpdate

/**
 * A [VSPhysicsFrame] represents the state of all the bodies in the physics engine. It also has [voxelUpdatesMap] which
 * describes any changes the physics engine made to the voxels.
 */
data class VSPhysicsFrame(
    val shipDataMap: Map<ShipId, ShipInPhysicsFrameData>,
    val voxelUpdatesMap: Map<ShipId, List<IVoxelShapeUpdate>>,
    val physTickNumber: Int
)

data class ShipInPhysicsFrameData(
    val uuid: ShipId,
    val inertiaData: RigidBodyInertiaData,
    val poseVel: PoseVel,
    val segments: SegmentTracker,
    val shipVoxelOffset: Vector3dc, // The voxel offset of the ship at this physics frame
    val aabb: AABBdc,
    // The last teleport id consumed by the physics pipeline. Used to avoid overwriting ship teleport commands.
    val lastShipTeleportId: Int,
)
