package org.valkyrienskies.core.api.ships

import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.physics.PhysicsVoxelShape
import org.valkyrienskies.core.api.physics.RigidBody
import org.valkyrienskies.core.api.ships.properties.ShipId

@VSBeta
interface PhysShip : RigidBody<PhysicsVoxelShape> {

    val id: ShipId
}
