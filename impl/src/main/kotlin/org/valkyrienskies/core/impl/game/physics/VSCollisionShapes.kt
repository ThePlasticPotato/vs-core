package org.valkyrienskies.core.impl.game.physics

import org.joml.Vector3ic
import org.joml.primitives.AABBic

interface VSCollisionShapeData

data class VSVoxelCollisionShapeData(
    val minDefined: Vector3ic,
    val maxDefined: Vector3ic,
    val totalVoxelRegion: AABBic,
    val shipVoxelsFullyLoaded: Boolean,
) : VSCollisionShapeData

data class VSSphereCollisionShapeData(
    val radius: Double
) : VSCollisionShapeData

data class VSWheelCollisionShapeData(
    val wheelRadius: Double,
    val halfThickness: Double,
) : VSCollisionShapeData

data class VSBoxCollisionShapeData(
    val lengthX: Double,
    val lengthY: Double,
    val lengthZ: Double,
) : VSCollisionShapeData

data class VSCapsuleCollisionShapeData(
    val radius: Double,
    val length: Double,
) : VSCollisionShapeData
