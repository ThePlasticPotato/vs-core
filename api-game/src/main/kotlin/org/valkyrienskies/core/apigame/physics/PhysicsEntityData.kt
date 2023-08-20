package org.valkyrienskies.core.apigame.physics

import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipInertiaData
import org.valkyrienskies.core.api.ships.properties.ShipTransform

data class PhysicsEntityData(
    val shipId: ShipId,
    var transform: ShipTransform,
    var inertiaData: ShipInertiaData,
    var linearVelocity: Vector3dc,
    var angularVelocity: Vector3dc,
    var collisionShapeData: VSCollisionShapeData,
    var collisionMask: Int = RigidBodyDefaults.DEFAULT_COLLISION_MASK,
    var staticFrictionCoefficient: Double = RigidBodyDefaults.DEFAULT_STATIC_FRICTION_COEFFICIENT,
    var dynamicFrictionCoefficient: Double = RigidBodyDefaults.DEFAULT_DYNAMIC_FRICTION_COEFFICIENT,
    var restitutionCoefficient: Double = RigidBodyDefaults.DEFAULT_RESTITUTION_COEFFICIENT,
    var isStatic: Boolean = false,
) {
    fun copyPhysicsEntityDataWithNewId(newId: ShipId): PhysicsEntityData {
        return PhysicsEntityData(
            shipId = newId,
            transform = transform,
            inertiaData = inertiaData,
            linearVelocity = linearVelocity,
            angularVelocity = angularVelocity,
            collisionShapeData = collisionShapeData,
            collisionMask = collisionMask,
            staticFrictionCoefficient = staticFrictionCoefficient,
            dynamicFrictionCoefficient = dynamicFrictionCoefficient,
            restitutionCoefficient = restitutionCoefficient,
            isStatic = isStatic,
        )
    }
}
