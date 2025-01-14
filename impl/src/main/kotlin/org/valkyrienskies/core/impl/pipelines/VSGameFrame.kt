package org.valkyrienskies.core.impl.pipelines

import org.joml.Quaterniondc
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.WingManagerChanges
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.apigame.constraints.VSConstraintAndId
import org.valkyrienskies.core.apigame.constraints.VSConstraintId
import org.valkyrienskies.core.apigame.physics.VSCollisionShapeData
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.impl.api.ShipForcesInducer
import org.valkyrienskies.core.impl.game.ships.PhysInertia
import org.valkyrienskies.core.impl.game.ships.ShipPhysicsData
import org.valkyrienskies.physics_api.PoseVel
import org.valkyrienskies.physics_api.voxel.updates.IVoxelShapeUpdate

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
    val dimension: DimensionId,
    val collisionShapeData: VSCollisionShapeData,
    val collisionShapeOffset: Vector3dc,
    val collisionShapeScaling: Double,
    val inertiaData: PhysInertia,
    val physicsData: ShipPhysicsData,
    val poseVel: PoseVel,
    val isStatic: Boolean,
    val forcesInducers: List<ShipForcesInducer>,
    val wingManagerChanges: WingManagerChanges?,
    val shipTeleportId: Int,
    val collisionMask: Int,
    val staticFrictionCoefficient: Double,
    val dynamicFrictionCoefficient: Double,
    val restitutionCoefficient: Double,
)

data class UpdateShipInGameFrameData(
    val uuid: ShipId,
    val collisionShapeOffset: Vector3dc,
    val collisionShapeScaling: Double,
    val inertiaData: PhysInertia,
    val physicsData: ShipPhysicsData,
    val isStatic: Boolean,
    val shipVoxelsFullyLoaded: Boolean,
    val forcesInducers: List<ShipForcesInducer>,
    val wingManagerChanges: WingManagerChanges?,
    val shipTeleportId: Int,
    val currentShipPos: Vector3dc,
    val currentShipRot: Quaterniondc,
    // TODO: Redundant? (physicsData)
    val currentShipVel: Vector3dc,
    val currentShipOmega: Vector3dc,
)

enum class ConstraintUpdateTypeInGameFrame {
    CREATE, UPDATE, DELETE
}
