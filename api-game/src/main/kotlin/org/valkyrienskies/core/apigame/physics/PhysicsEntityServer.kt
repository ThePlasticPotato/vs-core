package org.valkyrienskies.core.apigame.physics

import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipInertiaData
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.apigame.world.properties.DimensionId

// TODO: Should I put PhysicsEntityData in here?
data class PhysicsEntityServer(
    val id: ShipId,
    val dimensionId: DimensionId,
    var linearVelocity: Vector3dc,
    var angularVelocity: Vector3dc,
    var inertiaData: ShipInertiaData,
    // TODO: Rename this to transform
    var shipTransform: ShipTransform,
    // TODO: Rename this to prevTickTransform
    var prevTickShipTransform: ShipTransform,
    var shipTeleportId: Int,
    var collisionShapeData: VSCollisionShapeData,
    var collisionMask: Int = RigidBodyDefaults.DEFAULT_COLLISION_MASK,
    var staticFrictionCoefficient: Double = RigidBodyDefaults.DEFAULT_STATIC_FRICTION_COEFFICIENT,
    var dynamicFrictionCoefficient: Double = RigidBodyDefaults.DEFAULT_DYNAMIC_FRICTION_COEFFICIENT,
    var restitutionCoefficient: Double = RigidBodyDefaults.DEFAULT_RESTITUTION_COEFFICIENT,
    var isStatic: Boolean = false,
    var needsUpdating: Boolean = false,
)
