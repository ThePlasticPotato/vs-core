package org.valkyrienskies.core.impl.game.ships.types

import org.joml.Vector3ic
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.physics_api.voxel_updates.IVoxelShapeUpdate

typealias MutableShipVoxelUpdates = MutableMap<ShipId, MutableMap<Vector3ic, IVoxelShapeUpdate>>
typealias ShipVoxelUpdates = Map<ShipId, Map<Vector3ic, IVoxelShapeUpdate>>
